package electroblob.wizardry.entity.living;

import java.util.UUID;

import electroblob.wizardry.Wizardry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Abstract base implementation of {@link ISummonedCreature} which is the superclass to all custom summoned entities
 * (i.e. entities that don't extend vanilla/mod creatures). Also serves as an example of how to correctly implement the
 * above interface, and includes some non-critical method overrides which should be used for best results (xp, drops,
 * and such like). <i>Not to be confused with the old version of EntitySummonedCreature; that system has been
 * replaced.</i>
 * 
 * @since Wizardry 1.2
 * @author Electroblob
 */
public abstract class EntitySummonedCreature extends PathfinderMob implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override
	public int getLifetime(){
		return lifetime;
	}

	@Override
	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@Override
	public UUID getOwnerUUID(){
		return casterUUID;
	}

	@Override
	public void setOwnerId(UUID uuid){
		this.casterUUID = uuid;
	}

	/** Creates a new summoned creature in the given world. */
	public EntitySummonedCreature(EntityType<? extends EntitySummonedCreature> type, Level world){
		super(type, world);
		this.xpReward = 0;
	}

	// Implementations

	@Override
	public void setLastHurtByMob(LivingEntity entity){
		if(this.shouldRevengeTarget(entity)) super.setLastHurtByMob(entity);
	}

	@Override
	public void tick(){
		super.tick();
		this.updateDelegate();
	}

	@Override
	public void onSpawn(){
	}

	@Override
	public void onDespawn(){
	}

	@Override
	public boolean hasParticleEffect(){
		return false;
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand){
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's short-circuiting method of evaluating OR statements.
        return this.interactDelegate(player, hand) == InteractionResult.FAIL ? super.mobInteract(player, hand) : this.interactDelegate(player, hand);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		this.writeNBTDelegate(nbttagcompound);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		this.readNBTDelegate(nbttagcompound);
	}

	// Recommended overrides

	@Override public int getExperienceReward(){ return 0; }
	@Override protected boolean shouldDropLoot(){ return false; }
	@Override protected ResourceLocation getDefaultLootTable(){ return null; }
	@Override public boolean canPickUpLoot(){ return false; }

	// This vanilla method has nothing to do with the custom despawn() method.
	@Override public boolean removeWhenFarAway(double distance){
		return getCaster() == null && getOwnerUUID() == null;
	}

	@Override
	public boolean canAttack(LivingEntity entityType){
		// Returns true unless the given entity type is a flying entity and this entity only has melee attacks.
		return !(entityType instanceof FlyingMob) || this.hasRangedAttack();
	}

	@Override
	public Component getDisplayName(){
		if(getCaster() != null){
			return Component.translatable(NAMEPLATE_TRANSLATION_KEY, getCaster().getName(),
					Component.translatable("entity." + this.getEncodeId() + ".name"));
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		// If this returns true, the renderer will show the nameplate when looking directly at the entity
		return Wizardry.settings.summonedCreatureNames && getCaster() != null;
	}

	// Specific to EntitySummonedCreature, remove if copying

	/** Whether this summoned creature has a ranged attack. Used to test whether it should attack flying creatures. */
	public abstract boolean hasRangedAttack();
}