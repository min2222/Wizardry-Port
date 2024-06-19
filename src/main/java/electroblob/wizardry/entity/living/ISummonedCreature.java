package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.IndirectMinionDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.MinionDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Interface for all summoned creatures. The code for summoned creatures has been overhauled in Wizardry 2.1, and this
 * interface allows summoned creatures to extend vanilla (or indeed modded) entity classes, so
 * <code>EntitySummonedZombie</code> now extends <code>EntityZombie</code>, for example. This change has two major
 * benefits:
 * <p></p>
 * - There is no longer any need for separate render classes, because summoned creatures are now instances of vanilla
 * types. <i>You don't even need to assign a render class</i> because the supertype should already be assigned the
 * correct one.<br>
 * - Summoned creature classes are now much more robust when it comes to changes between Minecraft versions, since none
 * of the vanilla code needs to be copied.
 * <p></p>
 * <b>Summoned creatures that do not emulate vanilla entities do not directly implement this interface</b>. Instead,
 * they should extend the abstract base implementation, {@link EntitySummonedCreature}.
 * <p></p>
 * All damage dealt by ISummonedCreature instances is redirected via
 * {@link ISummonedCreature#onLivingAttackEvent(LivingAttackEvent)
 * ISummonedCreature.onLivingAttackEvent(LivingAttackEvent)} and replaced by an instance of
 * {@link IElementalDamage IElementalDamage} with the summoner of that creature as the source
 * rather than the creature itself. This means that kills by summoned creatures register as kills for their owner,
 * dropping xp and rare loot if that owner is a player.
 * <p></p>
 * Though this system is a lot better than the previous system, <i>it is not a perfect solution</i>. The old
 * EntitySummonedCreature class overrode some methods from Entity in order to add shared functionality, but this cannot
 * be done with an interface. To get around this problem, this interface contains 5 delegate methods that do the same
 * things, with the aim of centralising as much code as possible, even though it is not automatically applied.
 * <b>Implementing classes must override the corresponding methods from Entity, and within them, call the appropriate
 * delegate method in this interface.</b> It is impossible to enforce this condition, but the summoned creature will not
 * work properly unless it is adhered to. <i>The position of the delegate method call is unimportant, but by convention
 * it is usually at the start of the calling method, which avoids it being unintentionally skipped by a return
 * statement (except for methods where the result of the delegate method should itself be returned).</i>
 * <p></p>
 * It is recommended that when implementing this interface, you begin by copying {@link EntitySummonedCreature} to
 * ensure all the relevant methods are duplicated. You can then change the superclass, override any additional methods
 * and add functionality to any that are already overridden. You will always want to override the AI methods at the very
 * least.
 * <p></p>
 * Due to the limitations of interfaces, some methods that really ought to be protected are public. These are clearly
 * marked as 'Internal, DO NOT CALL'. <b>Don't call them, only implement them.</b>
 * 
 * @since Wizardry 2.1
 * @author Electroblob
 */
/* Quite honestly, this is not what default methods are really for. However, this is modding, and in modding some
 * sacrifices have to be made when it comes to Java style - because adding on to a pre-existing program is not a good
 * way of doing this sort of thing anyway, but we have no choice about that! */
@Mod.EventBusSubscriber
public interface ISummonedCreature extends IEntityAdditionalSpawnData, OwnableEntity {

	// Remember that ALL fields are static and final in interfaces, even if they don't explicitly state that.
	String NAMEPLATE_TRANSLATION_KEY = "entity." + Wizardry.MODID + ":summonedcreature.nameplate";

	// Setters and getters. The subclass fields that these access should be private.

	/** Sets the lifetime of the summoned creature in ticks. */
	void setLifetime(int ticks);

	/**
	 * Returns the lifetime of the summoned creature in ticks. Allows primarily for duration multiplier support, but
	 * also for example the skeleton legion spell which lasts for 60 seconds instead of the usual 30. Syncing and saving
	 * is done automatically. As of Wizardry 2.1, despawning is handled in ISummonedCreature; see
	 * {@link ISummonedCreature#onDespawn()} for details.
	 */
	int getLifetime();

	/** Internal, do not use. Implementing classes should implement this to set their owner UUID field. */
	void setOwnerId(UUID uuid);

	/** Returns the UUID of the owner of this summoned creature, or null if it does not have an owner.
	 * Implementing classes should implement this to return their owner UUID field. */
	@Nullable
	@Override
	UUID getOwnerUUID(); // Only overridden because I wanted to add javadoc!

	@Nullable
	@Override
	default Entity getOwner(){
		return getCaster(); // Delegate to getCaster
	}

	/**
	 * Returns the EntityLivingBase that summoned this creature, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this creature simply had no caster in the first place.
	 */
	@Nullable
	default LivingEntity getCaster(){ // Kept despite the above method because it returns an EntityLivingBase

		if(this instanceof Entity){ // Bit of a cheat but it saves having yet another method just to get the world

			Entity entity = EntityUtils.getEntityByUUID(((Entity)this).level, getOwnerUUID());

			if(entity != null && !(entity instanceof LivingEntity)){ // Should never happen
				Wizardry.logger.warn("{} has a non-living owner!", this);
				return null;
			}

			return (LivingEntity)entity;

		}else{
			Wizardry.logger.warn("{} implements ISummonedCreature but is not an SoundLoopSpellEntity!", this.getClass());
			return null;
		}
	}
	
	/**
	 * Sets the EntityLivingBase that summoned this creature.
	 */
	default void setCaster(@Nullable LivingEntity caster){
		setOwnerId(caster == null ? null : caster.getUUID());
	}

	// Miscellaneous

	/**
	 * Called by the server when constructing the spawn packet. Data should be added to the provided stream.
	 * <b>Implementors must call super when overriding.</b>
	 *
	 * @param buffer The packet data stream
	 */
	@Override
	default void writeSpawnData(FriendlyByteBuf buffer){
		buffer.writeInt(getCaster() != null ? getCaster().getId() : -1);
		buffer.writeInt(getLifetime());
	}

	/**
	 * Called by the client when it receives a Entity spawn packet. Data should be read out of the stream in the same
	 * way as it was written. <b>Implementors must call super when overriding.</b>
	 *
	 * @param buffer The packet data stream
	 */
	@Override
	default void readSpawnData(FriendlyByteBuf buffer){
		int id = buffer.readInt();
		// We're on the client side here, so we can safely use Minecraft.getInstance().world via proxies.
		if(id > -1){
			Entity entity = Wizardry.proxy.getTheWorld().getEntity(id);
			if(entity instanceof LivingEntity) setCaster((LivingEntity)entity);
			else Wizardry.logger.warn("Received a spawn packet for entity {}, but no living entity matched the supplied ID", this);
		}
		setLifetime(buffer.readInt());
	}

	/**
	 * Determines whether the given target is valid. Used by the default target selector (see
	 * {@link ISummonedCreature#getTargetSelector()}) and revenge targeting checks. This method is responsible for the
	 * ally designation system, default classes that may be targeted and the config whitelist/blacklist.
	 * Implementors may override this if they want to do something different or add their own checks.
	 * @see AllyDesignationSystem#isValidTarget(Entity, Entity)
	 */
	default boolean isValidTarget(Entity target){
		// If the target is valid based on the ADS...
		if(AllyDesignationSystem.isValidTarget(this.getCaster(), target)){

			// ...and is a player, they can be attacked, since players can't be in the whitelist or the
			// blacklist...
			if(target instanceof Player){
				// ...unless the creature was summoned by a good wizard who the player has not angered.
				if(getCaster() instanceof EntityWizard){
					if(getCaster().getLastHurtByMob() != target
							&& ((EntityWizard)getCaster()).getTarget() != target) {
						return false;
					}
				}

				return true;
			}

			// ...and is a mob, a summoned creature, a wizard...
			if((target instanceof Enemy || target instanceof ISummonedCreature
					|| (target instanceof EntityWizard && !(getCaster() instanceof EntityWizard))
					// ...or something that's attacking the owner...
					|| (target instanceof Mob && ((Mob)target).getTarget() == getCaster())
					// ...or in the whitelist...
					|| Arrays.asList(Wizardry.settings.summonedCreatureTargetsWhitelist)
					.contains(ForgeRegistries.ENTITY_TYPES.getKey(target.getType())))
					// ...and isn't in the blacklist...
					&& !Arrays.asList(Wizardry.settings.summonedCreatureTargetsBlacklist)
					.contains(ForgeRegistries.ENTITY_TYPES.getKey(target.getType()))){
				// ...it can be attacked.
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a entity selector to be passed into AI methods. Normally, this should not be overridden, but it is
	 * possible for implementors to override this in order to do something special when selecting a target.
	 */
	default Predicate<LivingEntity> getTargetSelector(){
		return entity -> !entity.isInvisible() && (getCaster() == null ? entity instanceof Player &&
				!((Player)entity).isCreative() : isValidTarget(entity));
	}

	/**
	 * Called when this creature has existed for 1 tick, effectively when it has just been spawned. Normally used to add
	 * particles, sounds, etc.
	 */
	void onSpawn();

	/**
	 * Called when this summoned creature vanishes. Normally used to add particles, sounds, etc.
	 */
	void onDespawn();

	/** Whether this creature should spawn a subtle black swirl particle effect while alive. */
	boolean hasParticleEffect();

	/** Returns whether this creature has an animation when appearing and disappearing. */
	default boolean hasAnimation(){
		return true;
	}

	/** Returns the colour of this creature's appear/disappear animation. */
	default int getAnimationColour(float animationProgress){
		return 0x000000;
	}

	/**
	 * Called from the event handler after the damage change is applied. Does nothing by default, but can be overridden
	 * to do something when a successful attack is made. This was added because the event-based damage source system can
	 * cause parts of attackEntityAsMob not to fire, since hurt is intercepted and canceled.
	 * <p></p>
	 * Usage examples: {@link EntitySilverfishMinion} uses this to summon more silverfish if the target is killed,
	 * {@link EntitySkeletonMinion} and {@link EntitySpiderMinion} use this to add potion effects to the target.
	 */
	default void onSuccessfulAttack(LivingEntity target){
	}

	// Delegates

	/**
	 * Implementors should call this from addAdditionalSaveData. Can be overridden as long as super is called, but there's
	 * very little point in doing that since anything extra could just be added to addAdditionalSaveData anyway.
	 */
	default void writeNBTDelegate(CompoundTag tagcompound){
		if(this.getCaster() != null){
			tagcompound.putUUID("casterUUID", this.getCaster().getUUID());
		}
		tagcompound.putInt("lifetime", getLifetime());
	}

	/**
	 * Implementors should call this from readAdditionalSaveData. Can be overridden as long as super is called, but there's
	 * very little point in doing that since anything extra could just be added to readAdditionalSaveData anyway.
	 */
	default void readNBTDelegate(CompoundTag tagcompound){
		this.setOwnerId(tagcompound.getUUID("casterUUID"));
		this.setLifetime(tagcompound.getInt("lifetime"));
	}

	/**
	 * Implementors should call this from setRevengeTarget, and call super.setRevengeTarget if and only if this method
	 * returns <b>true</b>.
	 */
	default boolean shouldRevengeTarget(LivingEntity entity){
		// Allows the config to prevent minions from revenge-targeting their owners (or anything else, for that matter)
		return Wizardry.settings.minionRevengeTargeting || isValidTarget(entity);
	}

	/**
	 * Implementors should call this from tick. Can be overridden as long as super is called, but there's very
	 * little point in doing that since anything extra could just be added to tick anyway.
	 */
	default void updateDelegate(){

		if(!(this instanceof Entity))
			throw new ClassCastException("Implementations of ISummonedCreature must extend SoundLoopSpellEntity!");

		Entity thisEntity = ((Entity)this);

		if(thisEntity.tickCount == 1){
			this.onSpawn();
		}

		// For some reason Minecraft reads the entity from NBT just after the entity is created, so setting -1 as a
		// default lifetime doesn't work. The easiest way around this is to use 0 - nobody's going to need it!
		if(thisEntity.tickCount > this.getLifetime() && this.getLifetime() > 0){
			this.onDespawn();
			thisEntity.discard();
		}

		if(this.hasParticleEffect() && thisEntity.level.isClientSide && thisEntity.level.random.nextInt(8) == 0)
			ParticleBuilder.create(Type.DARK_MAGIC)
			.pos(thisEntity.getX(), thisEntity.getY() + thisEntity.level.random.nextDouble() * 1.5, thisEntity.getZ())
			.clr(0.1f, 0.0f, 0.0f)
			.spawn(thisEntity.level);

	}

	/**
	 * Implementors should call this from processInteract, and call super.processInteract if and only if this method
	 * returns <b>false</b>.
	 */
	default boolean interactDelegate(Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		WizardData data = WizardData.get(player);
		// Selects one of the player's minions.
		if(player.isShiftKeyDown() && stack.getItem() instanceof ISpellCastingItem){

			if(!player.level.isClientSide && data != null && this.getCaster() == player){

				if(data.selectedMinion != null && data.selectedMinion.get() == this){
					// Deselects the selected minion if right-clicked again
					data.selectedMinion = null;
				}else{
					// Selects this minion
					data.selectedMinion = new WeakReference<>(this);
				}
				data.sync();
			}
			return true;
		}

		return false;
	}

	// Damage system

	@SubscribeEvent(priority = EventPriority.HIGHEST) // Needs to be first because we're replacing damage entirely
	static void onLivingAttackEvent(LivingAttackEvent event){

		// Rather than bother overriding entire attack methods in ISummonedCreature implementations, it's easier (and
		// more robust) to use LivingAttackEvent to modify the damage source.
		if(event.getSource().getEntity() instanceof ISummonedCreature){

			LivingEntity summoner = ((ISummonedCreature)event.getSource().getEntity()).getCaster();

			if(summoner != null){

				event.setCanceled(true);
				DamageSource newSource = event.getSource();
				// Copies over the original DamageType if appropriate.
				DamageType type = event.getSource() instanceof IElementalDamage
						? ((IElementalDamage)event.getSource()).getType()
						: DamageType.MAGIC;
				// Copies over the original isRetaliatory flag if appropriate.
				boolean isRetaliatory = event.getSource() instanceof IElementalDamage
						&& ((IElementalDamage)event.getSource()).isRetaliatory();

				// All summoned creatures are classified as magic, so it makes sense to do it this way.
				if(event.getSource() instanceof IndirectEntityDamageSource){
					newSource = new IndirectMinionDamage(event.getSource().msgId,
							event.getSource().getDirectEntity(), event.getSource().getEntity(), summoner, type,
							isRetaliatory);
				}else if(event.getSource() instanceof EntityDamageSource){
					// Name is copied over so it uses the appropriate vanilla death message
					newSource = new MinionDamage(event.getSource().msgId, event.getSource().getEntity(), summoner,
							type, isRetaliatory);
				}

				// Copy over any relevant 'attributes' the original DamageSource might have had.
				if(event.getSource().isExplosion()) newSource.setExplosion();
				if(event.getSource().isFire()) newSource.setIsFire();
				if(event.getSource().isProjectile()) newSource.setProjectile();

				// For some reason Minecraft calculates knockback relative to DamageSource#getEntity. In vanilla this
				// is unnoticeable, but it looks a bit weird with summoned creatures involved - so this fixes that.
				// Damage safety checker falls back to the original damage source, so it behaves as if the creature has
				// no summoner.
				if(DamageSafetyChecker.attackEntitySafely(event.getEntity(), newSource, event.getAmount(), event.getSource(), false)){
					// Uses event.getSource().getEntity() as this means the target is knocked back from the minion
					EntityUtils.applyStandardKnockback(event.getSource().getEntity(), event.getEntity());
					((ISummonedCreature)event.getSource().getEntity()).onSuccessfulAttack(event.getEntity());
					// If the target revenge-targeted the summoner, make it revenge-target the minion instead
					// (if it didn't revenge-target, do nothing)
					if(event.getEntity().getLastHurtByMob() == summoner
							&& event.getSource().getEntity() instanceof LivingEntity){
						event.getEntity().setLastHurtByMob((LivingEntity)event.getSource().getEntity());
					}
				}

			}
		}
	}

}
