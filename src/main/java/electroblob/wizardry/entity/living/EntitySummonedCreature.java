package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityFlying;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

import java.util.UUID;

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
public abstract class EntitySummonedCreature extends EntityCreature implements ISummonedCreature {

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
	public EntitySummonedCreature(Level world){
		super(world);
		this.experienceValue = 0;
	}

	// Implementations

	@Override
	public void setRevengeTarget(LivingEntity entity){
		if(this.shouldRevengeTarget(entity)) super.setRevengeTarget(entity);
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
	protected boolean processInteract(Player player, InteractionHand hand){
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's short-circuiting method of evaluating OR statements.
		return this.interactDelegate(player, hand) || super.processInteract(player, hand);
	}

	@Override
	public void writeEntityToNBT(CompoundTag nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		this.writeNBTDelegate(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(CompoundTag nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		this.readNBTDelegate(nbttagcompound);
	}

	// Recommended overrides

	@Override protected int getExperiencePoints(Player player){ return 0; }
	@Override protected boolean canDropLoot(){ return false; }
	@Override protected Item getDropItem(){ return null; }
	@Override protected ResourceLocation getLootTable(){ return null; }
	@Override public boolean canPickUpLoot(){ return false; }

	// This vanilla method has nothing to do with the custom despawn() method.
	@Override protected boolean canDespawn(){
		return getCaster() == null && getOwnerUUID() == null;
	}

	@Override
	public boolean getCanSpawnHere(){
		return this.level.getDifficulty() != Difficulty.PEACEFUL;
	}

	@Override
	public boolean canAttackClass(Class<? extends LivingEntity> entityType){
		// Returns true unless the given entity type is a flying entity and this entity only has melee attacks.
		return !EntityFlying.class.isAssignableFrom(entityType) || this.hasRangedAttack();
	}

	@Override
	public Component getDisplayName(){
		if(getCaster() != null){
			return Component.translatable(NAMEPLATE_TRANSLATION_KEY, getCaster().getName(),
					Component.translatable("entity." + this.getEntityString() + ".name"));
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