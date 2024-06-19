package electroblob.wizardry.entity.living;

import com.google.common.base.Predicate;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.monster.EntityMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
public class EntityEvilWizard extends Mob implements ISpellCaster, IEntityAdditionalSpawnData {

	private EntityAIAttackSpell<EntityEvilWizard> spellCastingAI = new EntityAIAttackSpell<>(this, 0.5D, 14.0F, 30, 50);

	public int textureIndex = 0;

	/** True if this evil wizard was spawned as part of a structure (tower or shrine), false if it spawned naturally. */
	public boolean hasStructure = false;

	/** Stores the UUIDs of the other evil wizards spawned in the same group, if any. The wizard will not revenge-target
	 * entities whose UUIDs are in this set. This is currently used only for shrines. */
	public final Set<UUID> groupUUIDs = new HashSet<>();

	/** The entity selector passed into the new AI methods. */
	protected Predicate<Entity> targetSelector;

	/** Data parameter for the cooldown time for wizards healing themselves. */
	private static final EntityDataSerializer<Integer> HEAL_COOLDOWN = SynchedEntityData.createKey(EntityEvilWizard.class,
			EntityDataSerializers.VARINT);
	/** Data parameter for the wizard's element. */
	private static final EntityDataSerializer<Integer> ELEMENT = SynchedEntityData.createKey(EntityEvilWizard.class,
			EntityDataSerializers.VARINT);
	/** Data parameters for the wizard's current continuous spell. */
	private static final EntityDataSerializer<String> CONTINUOUS_SPELL = SynchedEntityData.createKey(EntityEvilWizard.class, EntityDataSerializers.STRING);
	private static final EntityDataSerializer<Integer> SPELL_COUNTER = SynchedEntityData.createKey(EntityEvilWizard.class, EntityDataSerializers.VARINT);

	/** The resource location for the evil wizard's loot table. */
	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(Wizardry.MODID, "entities/evil_wizard");

	// Field implementations
	private List<Spell> spells = new ArrayList<Spell>(4);

	public EntityEvilWizard(Level world){

		super(world);
		this.setSize(0.6F, 1.8F);
		((PathNavigateGround)this.getNavigator()).setBreakDoors(true);

		// For some reason this can't be done in initEntityAI
		this.tasks.addTask(3, this.spellCastingAI);

		this.detachHome();
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(HEAL_COOLDOWN, -1);
		this.dataManager.register(ELEMENT, -1);
		this.dataManager.register(CONTINUOUS_SPELL, "ebwizardry:none");
		this.dataManager.register(SPELL_COUNTER, 0);
	}

	@Override
	protected void initEntityAI(){

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.6D));
		this.tasks.addTask(7, new EntityAIWatchClosest2(this, Player.class, 3.0F, 1.0F));
		this.tasks.addTask(7, new EntityAIWander(this, 0.6D));

		this.targetSelector = entity -> {

			// If the target is valid and not invisible...
			if(entity != null && !entity.isInvisible()
					&& AllyDesignationSystem.isValidTarget(EntityEvilWizard.this, entity)){

				// ... and is a player, a summoned creature, another (non-evil) wizard ...
				if(entity instanceof Player
						|| (entity instanceof ISummonedCreature || entity instanceof EntityWizard
				// ... or in the whitelist ...
								|| Arrays.asList(Wizardry.settings.summonedCreatureTargetsWhitelist)
										.contains(EntityList.getKey(entity.getClass())))
								// ... and isn't in the blacklist ...
								&& !Arrays.asList(Wizardry.settings.summonedCreatureTargetsBlacklist)
										.contains(EntityList.getKey(entity.getClass()))){
					// ... it can be attacked.
					return true;
				}
			}

			return false;
		};

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, LivingEntity.class,
				0, false, true, this.targetSelector));
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.5D);
		this.getEntityAttribute(Attributes.MAX_HEALTH).setBaseValue(30);
	}

	private int getHealCooldown(){
		return this.dataManager.get(HEAL_COOLDOWN);
	}

	private void setHealCooldown(int cooldown){
		this.dataManager.set(HEAL_COOLDOWN, cooldown);
	}

	public Element getElement(){
		int n = this.dataManager.get(ELEMENT);
		return n == -1 ? null : Element.values()[n];
	}

	public void setElement(Element element){
		this.dataManager.set(ELEMENT, element.ordinal());
	}

	@Override
	public List<Spell> getSpells(){
		return this.spells;
	}

	@Override
	public SpellModifiers getModifiers(){
		return new SpellModifiers();
	}

	@Override
	public void setContinuousSpell(Spell spell) {
		this.dataManager.set(CONTINUOUS_SPELL, spell.getRegistryName().toString());
	}

	@Override
	public Spell getContinuousSpell() {
		return Spell.get(this.dataManager.get(CONTINUOUS_SPELL));
	}

	@Override
	public void setSpellCounter(int count) {
		this.dataManager.set(SPELL_COUNTER, count);
	}

	@Override
	public int getSpellCounter() {
		return this.dataManager.get(SPELL_COUNTER);
	}
	
	@Override
	public int getAimingError(Difficulty difficulty){
		// Being more intelligent than skeletons, wizards are a little more accurate.
		switch(difficulty){
		case EASY: return 7;
		case NORMAL: return 4;
		case HARD: return 1;
		default: return 7; // Peaceful counts as easy
		}
	}

	@Override
	public void setRevengeTarget(@Nullable LivingEntity target){
		if(target == null || !groupUUIDs.contains(target.getUUID())) super.setRevengeTarget(target);
	}

	@Override
	public void onLivingUpdate(){

		super.onLivingUpdate();

		int healCooldown = this.getHealCooldown();

		// This is now done slightly differently because hasEffect doesn't work on client here, meaning that when
		// affected with arcane jammer and healCooldown == 0, whilst the wizard didn't actually heal or play the sound,
		// the particles still spawned, and since healCooldown wasn't reset they spawned every tick until the arcane
		// jammer wore off.
		if(healCooldown == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0
				&& !this.hasEffect(WizardryPotions.arcane_jammer)){

			// Healer wizards use greater heal.
			this.heal(this.getElement() == Element.HEALING ? 8 : 4);
			this.setHealCooldown(-1);

			// deathTime == 0 checks the wizard isn't currently dying
		}else if(healCooldown == -1 && this.deathTime == 0){

			// Heal particles TODO: Change this so it uses the heal spell directly
			if(level.isClientSide){
				for(int i=0; i<10; i++){
					double x = (double)((float)this.getX() + random.nextFloat() * 2 - 1.0F);
					// Apparently the client side spawns the particles 1 block higher than it should... hence the -
					// 0.5F.
					double y = (double)((float)this.getY() - 0.5F + random.nextFloat());
					double z = (double)((float)this.getZ() + random.nextFloat() * 2 - 1.0F);
					ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1F, 0).clr(1, 1, 0.3f).spawn(world);
				}
			}else{
				if(this.getHealth() < 10){
					// Wizards heal themselves more often if they have low health
					this.setHealCooldown(150);
				}else{
					this.setHealCooldown(400);
				}

				this.playSound(Spells.heal.getSounds()[0], 0.7F, random.nextFloat() * 0.4F + 1.0F);
			}
		}
		if(healCooldown > 0){
			this.setHealCooldown(healCooldown - 1);
		}
	}

	@Override
	protected boolean processInteract(Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		// Debugging
		// player.addChatComponentMessage(Component.translatable("wizard.debug",
		// Spell.get(spells[1]).getDisplayName(), Spell.get(spells[2]).getDisplayName(),
		// Spell.get(spells[3]).getDisplayName()));

		// When right-clicked with a spell book in creative, sets one of the spells to that spell
		if(player.isCreative() && stack.getItem() instanceof ItemSpellBook){
			Spell spell = Spell.byMetadata(stack.getItemDamage());
			if(this.spells.size() >= 4 && spell.canBeCastBy(this, true)){
				// The set(...) method returns the element that was replaced - neat!
				player.sendMessage(Component.translatable("item." + Wizardry.MODID + ":spell_book.apply_to_wizard",
						this.getDisplayName(), this.spells.set(random.nextInt(3) + 1, spell).getNameForTranslationFormatted(),
						spell.getNameForTranslationFormatted()));
				return true;
			}
		}

		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt){
		super.addAdditionalSaveData(nbt);
		Element element = this.getElement();
		nbt.putInt("element", element == null ? 0 : element.ordinal());
		nbt.putInt("skin", this.textureIndex);
		NBTExtras.storeTagSafely(nbt, "spells", NBTExtras.listToNBT(spells, spell -> new NBTTagInt(spell.metadata())));
		nbt.setBoolean("hasStructure", this.hasStructure);
		NBTExtras.storeTagSafely(nbt, "groupUUIDs", NBTExtras.listToNBT(groupUUIDs, NbtUtils::createUUIDTag));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt){
		super.readAdditionalSaveData(nbt);
		this.setElement(Element.values()[nbt.getInt("element")]);
		this.textureIndex = nbt.getInt("skin");
		this.spells = (List<Spell>)NBTExtras.NBTToList(nbt.getTagList("spells", NBT.TAG_INT),
				(NBTTagInt tag) -> Spell.byMetadata(tag.getInt()));
		this.hasStructure = nbt.getBoolean("hasStructure");
		this.groupUUIDs.addAll(NBTExtras.NBTToList(nbt.getTagList("groupUUIDs", NBT.TAG_COMPOUND), NbtUtils::getUUIDFromTag));
	}
	
	@Override
	public int getMaxSpawnedInChunk(){
		return 1;
	}

	@Override
	protected boolean canDespawn(){
		// Evil wizards can only despawn if they don't have a tower (i.e. if they spawned naturally at night)
		return !this.hasStructure;
	}

//	@Override
//	protected float getSoundPitch(){
//		return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.6F;
//	}

	@Override
	protected SoundEvent getAmbientSound(){
		return WizardrySounds.ENTITY_EVIL_WIZARD_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_EVIL_WIZARD_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_EVIL_WIZARD_DEATH;
	}

	// Although it *looks* like this is still called, in actual fact the only method that calls it is overridden in
	// EntityLiving to use the loot table system instead. This has been kept as a fallback in case the loot table is
	// not found.
	@Override
	protected void dropFewItems(boolean hitByPlayer, int lootingLevel){
		// Drops 3-5 crystals without looting bonuses
		int j = 3 + this.random.nextInt(3) + this.random.nextInt(1 + lootingLevel);

		for(int k = 0; k < j; k++){
			this.dropItem(WizardryItems.magic_crystal, 1);
		}

		// Evil wizards occasionally drop one of their spells as a spell book, but not magic missile. This isn't in
		// the dropRareDrop method because that would be just as rare as normal mobs; instead this is half as rare.
		if(this.spells.size() > 0 && random.nextInt(100) - lootingLevel < 5)
			this.entityDropItem(new ItemStack(WizardryItems.spell_book, 1,
					this.spells.get(1 + random.nextInt(this.spells.size() - 1)).metadata()), 0);
	}

	@Override
	protected ResourceLocation getLootTable(){
		return LOOT_TABLE;
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data){

		data = super.onInitialSpawn(difficulty, data);

		textureIndex = this.random.nextInt(6);

		if(getElement() == null){
			if(random.nextBoolean()){
				this.setElement(Element.values()[random.nextInt(Element.values().length - 1) + 1]);
			}else{
				this.setElement(Element.MAGIC);
			}
		}

		Element element = this.getElement();

		// Adds armour.
		for(EquipmentSlot slot : InventoryUtils.ARMOUR_SLOTS){
			this.setItemStackToSlot(slot, new ItemStack(WizardryItems.getArmour(element, slot)));
		}

		// Default chance is 0.085f, for reference.
		for(EquipmentSlot slot : EquipmentSlot.values())
			this.setDropChance(slot, 0.0f);

		// All wizards know magic missile, even if it is disabled.
		spells.add(Spells.magic_missile);

		Tier maxTier = EntityWizard.populateSpells(this, spells, element, hasStructure, 3, rand);

		// Now done after the spells so it can take the tier into account. For evil wizards this is slightly different;
		// it picks a random wand which is at least a high enough tier for the spells the wizard has.
		Tier tier = Tier.values()[maxTier.ordinal() + random.nextInt(Tier.values().length - maxTier.ordinal())];
		this.setItemStackToSlot(EquipmentSlot.MAINHAND, new ItemStack(WizardryItems.getWand(tier, element)));

		return data;
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(textureIndex);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		textureIndex = data.readInt();
	}

	@SubscribeEvent
	public static void onCheckSpawnEvent(LivingSpawnEvent.CheckSpawn event){
		// We have no way of checking if it's a spawner in getCanSpawnHere() so this has to be done here instead
		if(event.getEntity() instanceof EntityEvilWizard && !event.isSpawner()){
			if(!ArrayUtils.contains(Wizardry.settings.mobSpawnDimensions, event.getWorld().provider.getDimension()))
				event.setResult(Event.Result.DENY);
		}
	}

}
