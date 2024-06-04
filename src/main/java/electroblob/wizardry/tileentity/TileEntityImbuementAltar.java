package electroblob.wizardry.tileentity;

import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.event.ImbuementActivateEvent;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TileEntityImbuementAltar extends BlockEntity implements ITickable {

	private static final int IMBUEMENT_DURATION = 140;

	private ItemStack stack;
	private int imbuementTimer;
	private Element displayElement;
	private Player lastUser;
	/** For loading purposes only. This does not get updated once loaded! */
	private UUID lastUserUUID;

	public TileEntityImbuementAltar(){
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

		world.notifyBlockUpdate(pos, level.getBlockState(pos), level.getBlockState(pos), 3); // Sync
	}

	public ItemStack getStack(){
		return stack;
	}

	@Override
	public void update(){

		if(lastUserUUID != null && lastUser == null) lastUser = level.getPlayerEntityByUUID(lastUserUUID);

		if(imbuementTimer > 0){

			if(imbuementTimer == 1){ // Has to be done here because of syncing
				world.playSound(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
						WizardrySounds.BLOCK_IMBUEMENT_ALTAR_IMBUE, SoundSource.BLOCKS, 1, 1, false);
			}

			ItemStack result = getResult();

			if(result.isEmpty()){
				imbuementTimer = 0;

			}else{

				if(imbuementTimer++ >= IMBUEMENT_DURATION){
					this.stack = result;
					consumeReceptacleContents();
					imbuementTimer = 0;
					displayElement = null;
					if(lastUser instanceof ServerPlayer){
						WizardryAdvancementTriggers.imbuement_altar.trigger((ServerPlayer)lastUser, this.stack);
					}
				}

				if(level.isClientSide && world.random.nextInt(2) == 0){

					Element[] elements = getReceptacleElements();

					Vec3 centre = GeometryUtils.getCentre(this.pos.up());

					for(int i = 0; i < elements.length; i++){

						if(elements[i] == null) continue;

						Vec3 offset = new Vec3(Direction.byHorizontalIndex(i).getDirectionVec());
						Vec3 vec = GeometryUtils.getCentre(this.pos).add(0, 0.3, 0).add(offset.scale(0.7));

						int[] colours = BlockReceptacle.PARTICLE_COLOURS.get(elements[i]);

						ParticleBuilder.create(Type.DUST, world.rand, vec.x, vec.y, vec.z, 0.1, false)
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

		boolean actuallyCrafting = imbuementTimer >= IMBUEMENT_DURATION - 1 && world instanceof ServerLevel;
		Element[] elements = getReceptacleElements();

		ItemStack result = getImbuementResult(stack, elements, actuallyCrafting, world, lastUser);

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

		for(Direction side : Direction.HORIZONTALS){

			BlockEntity tileEntity = level.getTileEntity(pos.relative(side));

			if(tileEntity instanceof TileEntityReceptacle){
				elements[side.getHorizontalIndex()] = ((TileEntityReceptacle)tileEntity).getElement();
			}else{
				elements[side.getHorizontalIndex()] = null;
			}
		}

		return elements;
	}

	/** Empties the 4 adjacent receptacles. */
	private void consumeReceptacleContents(){

		for(Direction side : Direction.HORIZONTALS){

			BlockEntity tileEntity = level.getTileEntity(pos.relative(side));

			if(tileEntity instanceof TileEntityReceptacle){
				((TileEntityReceptacle)tileEntity).setElement(null);
			}
		}
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag nbt){
		super.writeToNBT(nbt);
		CompoundTag itemTag = new CompoundTag();
		stack.writeToNBT(itemTag);
		nbt.setTag("item", itemTag);
		nbt.putInt("imbuementTimer", imbuementTimer);
		if(lastUser != null) nbt.setUniqueId("lastUser", lastUser.getUniqueID());
		return nbt;
	}

	@Override
	public void readFromNBT(CompoundTag nbt){
		super.readFromNBT(nbt);
		CompoundTag itemTag = nbt.getCompoundTag("item");
		this.stack = new ItemStack(itemTag);
		this.imbuementTimer = nbt.getInt("imbuementTimer");
		this.lastUserUUID = nbt.getUniqueId("lastUser");
	}

	@Override
	public CompoundTag getUpdateTag(){
		return this.writeToNBT(new CompoundTag());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
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

				ItemStack result = new ItemStack(ItemWizardArmour.getArmour(receptacleElements[0], ((ItemWizardArmour)input.getItem()).armourClass, ((ItemWizardArmour)input.getItem()).armorType));

				result.setTag(input.getTag());
				((IManaStoringItem)result.getItem()).setMana(result, ((ItemWizardArmour)input.getItem()).getMana(input));

				return result;
			}
		}

		if((input.getItem() == WizardryItems.magic_crystal || input.getItem() == Item.getItemFromBlock(WizardryBlocks.crystal_block))
				&& input.getMetadata() == 0){

			if(Arrays.stream(receptacleElements).distinct().count() == 1 && receptacleElements[0] != null){ // All the same element
				return new ItemStack(input.getItem(), input.getCount(), receptacleElements[0].ordinal());
			}
		}

		if(input.getItem() == WizardryItems.ruined_spell_book){

			if(!ArrayUtils.contains(receptacleElements, null)){ // All receptacles filled (any element)

				if(fullLootGen){

					// Pick a random element out of the receptacles and use its loot table
					// This is an elegant way of having the dust elements affect the spell outcome without hardcoding
					// any actual numbers, thus allowing packmakers as much control as possible over the weighting
					// The probabilities are a little complicated but work out quite nicely at 57% chance with 4 of the
					// same element of spectral dust
					Element element = receptacleElements[world.random.nextInt(receptacleElements.length)];
					LootTable table = level.getLootTableManager().getLootTableFromLocation(
							WizardryLoot.RUINED_SPELL_BOOK_LOOT_TABLES[element.ordinal() - 1]);
					LootContext context = new LootContext.Builder((ServerLevel)world).withPlayer(lastUser)
							.withLuck(lastUser == null ? 0 : lastUser.getLuck()).build();

					List<ItemStack> stacks = table.generateLootForPools(world.rand, context);
					return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
				}

				return new ItemStack(WizardryItems.spell_book); // No point generating loot every tick just to check the recipe
			}
		}

		return ItemStack.EMPTY;
	}

}
