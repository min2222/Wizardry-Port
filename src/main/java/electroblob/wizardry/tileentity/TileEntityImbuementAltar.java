package electroblob.wizardry.tileentity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.event.ImbuementActivateEvent;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryLoot;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class TileEntityImbuementAltar extends BlockEntity {

	private static final int IMBUEMENT_DURATION = 140;

	private ItemStack stack;
	private int imbuementTimer;
	private Element displayElement;
	private Player lastUser;
	/** For loading purposes only. This does not get updated once loaded! */
	private UUID lastUserUUID;
	
	private static final Direction[] HORIZONTALS = ObfuscationReflectionHelper.getPrivateValue(Direction.class, null, "f_122349_");

	public TileEntityImbuementAltar(BlockPos pos, BlockState state) {
		super(WizardryBlocks.IMBUEMENT_ALTAR_BLOCK_ENTITY.get(), pos, state);
		stack = ItemStack.EMPTY;
	}

	public void setStack(ItemStack stack){
		this.stack = stack;
		checkRecipe();
	}

	public void setLastUser(Player player){
		this.lastUser = player;
	}

	public void checkRecipe(){

		if(getResult().isEmpty()){
			imbuementTimer = 0;
		}else if(imbuementTimer == 0){
			imbuementTimer = 1;
		}else{
			return; // Don't sync if nothing changed
		}

		level.markAndNotifyBlock(worldPosition, level.getChunkAt(worldPosition), level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3, 512); // Sync
	}

	public ItemStack getStack(){
		return stack;
	}

    public static void update(Level world, BlockPos pos, BlockState state, TileEntityImbuementAltar tileEntity) {

		if(tileEntity.lastUserUUID != null && tileEntity.lastUser == null) tileEntity.lastUser = world.getPlayerByUUID(tileEntity.lastUserUUID);

		if(tileEntity.imbuementTimer > 0){

			if(tileEntity.imbuementTimer == 1){ // Has to be done here because of syncing
				world.playLocalSound(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
						WizardrySounds.BLOCK_IMBUEMENT_ALTAR_IMBUE, SoundSource.BLOCKS, 1, 1, false);
			}

			ItemStack result = tileEntity.getResult();

			if(result.isEmpty()){
				tileEntity.imbuementTimer = 0;

			}else{

				if(tileEntity.imbuementTimer++ >= IMBUEMENT_DURATION){
					tileEntity.stack = result;
					tileEntity.consumeReceptacleContents();
					tileEntity.imbuementTimer = 0;
					tileEntity.displayElement = null;
					if(tileEntity.lastUser instanceof ServerPlayer){
						WizardryAdvancementTriggers.imbuement_altar.trigger((ServerPlayer)tileEntity.lastUser, tileEntity.stack);
					}
				}

				if(world.isClientSide && world.random.nextInt(2) == 0){

					Element[] elements = tileEntity.getReceptacleElements();

					Vec3 centre = GeometryUtils.getCentre(pos.above());

					for(int i = 0; i < elements.length; i++){

						if(elements[i] == null) continue;

						Vec3 offset = new Vec3(Direction.from2DDataValue(i).step());
						Vec3 vec = GeometryUtils.getCentre(pos).add(0, 0.3, 0).add(offset.scale(0.7));

						int[] colours = BlockReceptacle.PARTICLE_COLOURS.get(elements[i]);

						ParticleBuilder.create(Type.DUST, world.random, vec.x, vec.y, vec.z, 0.1, false)
								.vel(centre.subtract(vec).scale(0.02)).clr(colours[1]).fade(colours[2]).time(50).spawn(world);
					}
				}
			}
		}
	}

	/** Returns the element to use for the visual ray effect colours, or null if they should not be displayed. */
	public Element getDisplayElement(){
		return displayElement;
	}

	/** Returns how complete the current action is (from 0 to 1), or 0 if no action is being performed. */
	public float getImbuementProgress(){
		return (float)imbuementTimer / IMBUEMENT_DURATION;
	}

	private ItemStack getResult(){

		boolean actuallyCrafting = imbuementTimer >= IMBUEMENT_DURATION - 1 && level instanceof ServerLevel;
		Element[] elements = getReceptacleElements();

		ItemStack result = getImbuementResult(stack, elements, actuallyCrafting, level, lastUser);

		if(result.isEmpty()){
			displayElement = null;
		}else if(Arrays.stream(elements).distinct().count() == 1){ // All the same element
			displayElement = elements[0];
		}else{
			displayElement = Element.MAGIC;
		}

		return result;
	}

	/** Returns the elements of the 4 adjacent receptacles, in SWNE order. Null means an empty or missing receptacle. */
	private Element[] getReceptacleElements(){

		Element[] elements = new Element[4];

		for(Direction side : HORIZONTALS){

			BlockEntity tileEntity = level.getBlockEntity(worldPosition.relative(side));

			if(tileEntity instanceof TileEntityReceptacle){
				elements[side.get2DDataValue()] = ((TileEntityReceptacle)tileEntity).getElement();
			}else{
				elements[side.get2DDataValue()] = null;
			}
		}

		return elements;
	}

	/** Empties the 4 adjacent receptacles. */
	private void consumeReceptacleContents(){

		for(Direction side : HORIZONTALS){

			BlockEntity tileEntity = level.getBlockEntity(worldPosition.relative(side));

			if(tileEntity instanceof TileEntityReceptacle){
				((TileEntityReceptacle)tileEntity).setElement(null);
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		CompoundTag itemTag = new CompoundTag();
		stack.save(itemTag);
		nbt.put("item", itemTag);
		nbt.putInt("imbuementTimer", imbuementTimer);
		if(lastUser != null) nbt.putUUID("lastUser", lastUser.getUUID());
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		CompoundTag itemTag = nbt.getCompound("item");
		this.stack = ItemStack.of(itemTag);
		this.imbuementTimer = nbt.getInt("imbuementTimer");
		this.lastUserUUID = nbt.getUUID("lastUser");
	}

	/**
	 * Returns the stack that results from imbuing the given input with the given elements.
	 * @param input The item stack being imbued
	 * @param receptacleElements The elements of the four receptacles
	 * @param fullLootGen True to perform full loot generation from loot tables, false if this is just being queried for
	 *                    visuals or other purposes.
	 * @param world A reference to the current world object (may be null if {@code fullLootGen} is false)
	 * @param lastUser The player that last interacted with the imbuement altar, or null if there isn't one (or if this
	 *                 is being queried for other reasons, e.g. JEI)
	 * @return The resulting item stack, or an empty stack if the given combination is not a valid imbuement
	 */
	public static ItemStack getImbuementResult(ItemStack input, Element[] receptacleElements, boolean fullLootGen, Level world, Player lastUser){

		ItemStack eventResult = ItemStack.EMPTY;

		if (world != null && MinecraftForge.EVENT_BUS.post(new ImbuementActivateEvent(input, receptacleElements, world, lastUser, eventResult))) {
			// return the stack if something changes the result from an empty stack.
			//noinspection ConstantConditions
			if (eventResult != ItemStack.EMPTY) return eventResult;
		}

		if(input.getItem() instanceof ItemWizardArmour && ((ItemWizardArmour)input.getItem()).element == null){

			if(Arrays.stream(receptacleElements).distinct().count() == 1 && receptacleElements[0] != null){ // All the same element

				ItemStack result = new ItemStack(ItemWizardArmour.getArmour(receptacleElements[0], ((ItemWizardArmour)input.getItem()).armourClass, ((ItemWizardArmour)input.getItem()).getSlot()));

				result.setTag(input.getTag());
				((IManaStoringItem)result.getItem()).setMana(result, ((ItemWizardArmour)input.getItem()).getMana(input));

				return result;
			}
		}

		if((input.getItem() == WizardryItems.MAGIC_CRYSTAL.get() || input.getItem() == WizardryItems.MAGIC_CRYSTAL_BLOCK.get())){

			if(Arrays.stream(receptacleElements).distinct().count() == 1 && receptacleElements[0] != null){ // All the same element
				if(receptacleElements[0].equals(Element.FIRE)) {
					return new ItemStack(WizardryItems.FIRE_CRYSTAL.get(), input.getCount());
				}
				else if(receptacleElements[0].equals(Element.ICE)) {
					return new ItemStack(WizardryItems.ICE_CRYSTAL.get(), input.getCount());
				}
				else if(receptacleElements[0].equals(Element.LIGHTNING)) {
					return new ItemStack(WizardryItems.LIGHTNING_CRYSTAL.get(), input.getCount());
				}
				else if(receptacleElements[0].equals(Element.NECROMANCY)) {
					return new ItemStack(WizardryItems.NECROMANCY_CRYSTAL.get(), input.getCount());
				}
				else if(receptacleElements[0].equals(Element.EARTH)) {
					return new ItemStack(WizardryItems.EARTH_CRYSTAL.get(), input.getCount());
				}
				else if(receptacleElements[0].equals(Element.SORCERY)) {
					return new ItemStack(WizardryItems.SORCERY_CRYSTAL.get(), input.getCount());
				}
				else if(receptacleElements[0].equals(Element.HEALING)) {
					return new ItemStack(WizardryItems.HEALING_CRSYTAL.get(), input.getCount());
				}
				return new ItemStack(input.getItem(), input.getCount());
			}
		}

		if(input.getItem() == WizardryItems.RUINED_SPELL_BOOK.get()){

			if(!ArrayUtils.contains(receptacleElements, null)){ // All receptacles filled (any element)

				if(fullLootGen){

					// Pick a random element out of the receptacles and use its loot table
					// This is an elegant way of having the dust elements affect the spell outcome without hardcoding
					// any actual numbers, thus allowing packmakers as much control as possible over the weighting
					// The probabilities are a little complicated but work out quite nicely at 57% chance with 4 of the
					// same element of spectral dust
					Element element = receptacleElements[world.random.nextInt(receptacleElements.length)];
					LootTable table = world.getServer().getLootTables().get(
							WizardryLoot.RUINED_SPELL_BOOK_LOOT_TABLES[element.ordinal() - 1]);
					LootContext.Builder context = new LootContext.Builder((ServerLevel)world).withParameter(LootContextParams.THIS_ENTITY, lastUser)
							.withLuck(lastUser == null ? 0 : lastUser.getLuck());

					List<ItemStack> stacks = table.getRandomItems(context.create(LootContextParamSets.BLOCK));
					return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
				}

				return new ItemStack(WizardryItems.SPELL_BOOK.get()); // No point generating loot every tick just to check the recipe
			}
		}

		return ItemStack.EMPTY;
	}

}
