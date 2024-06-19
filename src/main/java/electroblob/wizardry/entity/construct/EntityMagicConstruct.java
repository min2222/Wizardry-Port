package electroblob.wizardry.entity.construct;

import java.util.UUID;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/**
 * This class is for all inanimate magical constructs which are not projectiles. Generally speaking, subclasses of this
 * class are areas of effect which deal damage or apply effects over time, including black hole, blizzard, tornado and
 * a few others. The caster UUID, lifetime and damage multiplier are stored here, and lifetime is also synced here.
 *
 * @since Wizardry 1.0
 */
public abstract class EntityMagicConstruct extends Entity implements OwnableEntity, IEntityAdditionalSpawnData {

	/** The UUID of the caster. As of Wizardry 4.3, this <b>is</b> synced, and rather than storing the caster
	 * instance via a weak reference, it is fetched from the UUID each time it is needed in
	 * {@link EntityMagicConstruct#getCaster()}. */
	private UUID casterUUID;

	/** The time in ticks this magical construct lasts for; defaults to 600 (30 seconds). If this is -1 the construct
	 * doesn't despawn. */
	public int lifetime = 600;

	/** The damage multiplier for this construct, determined by the wand with which it was cast. */
	public float damageMultiplier = 1.0f;

	public EntityMagicConstruct(EntityType<? extends Entity> type, Level world){
		super(type, world);
		this.getBbHeight() = 1.0f;
		this.width = 1.0f;
		this.noPhysics = true;
	}

	// Overrides the original to stop the entity moving when it intersects stuff. The default arrow does this to allow
	// it to stick in blocks.
	@Override
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport){
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}

	public void tick(){

		if(this.tickCount > lifetime && lifetime != -1){
			this.despawn();
		}

		super.tick();

	}

	@Override
	public InteractionResult applyPlayerInteraction(Player player, Vec3 vec, InteractionHand hand){

		// Permanent constructs can now be dispelled by sneak-right-clicking
		if(lifetime == -1 && getCaster() == player && player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof ISpellCastingItem){
			this.despawn();
			return InteractionResult.SUCCESS;
		}

		return super.applyPlayerInteraction(player, vec, hand);
	}

	/**
	 * Defaults to just discard() in EntityMagicConstruct, but is provided to allow subclasses to override this e.g.
	 * bubble uses it to dismount the entity inside it and play the 'pop' sound before calling super(). You should
	 * always call super() when overriding this method, in case it changes. There is no need, therefore, to call
	 * discard() when overriding.
	 */
	public void despawn(){
		this.discard();
	}

	@Override
	protected void defineSynchedData(){
		// We could leave this unimplemented, but since the majority of subclasses don't use it, let's make it optional
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbttagcompound){
		if(nbttagcompound.hasUUID("casterUUID")) casterUUID = nbttagcompound.getUUID("casterUUID");
		lifetime = nbttagcompound.getInt("lifetime");
		damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbttagcompound){
		if(casterUUID != null){
			nbttagcompound.putUUID("casterUUID", casterUUID);
		}
		nbttagcompound.putInt("lifetime", lifetime);
		nbttagcompound.putFloat("damageMultiplier", damageMultiplier);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		data.writeInt(lifetime);
		data.writeInt(getCaster() == null ? -1 : getCaster().getId());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){

		lifetime = data.readInt();

		int id = data.readInt();

		if(id == -1){
			setCaster(null);
		}else{
			Entity entity = level.getEntity(id);
			if(entity instanceof LivingEntity){
				setCaster((LivingEntity)entity);
			}else{
				Wizardry.logger.warn("Construct caster with ID in spawn data not found");
			}
		}
	}

	@Nullable
	@Override
	public UUID getOwnerUUID(){
		return casterUUID;
	}

	@Nullable
	@Override
	public Entity getOwner(){
		return getCaster(); // Delegate to getCaster
	}

	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this construct simply had no caster in the first place.
	 */
	@Nullable
	public LivingEntity getCaster(){ // Kept despite the above method because it returns an EntityLivingBase

		Entity entity = EntityUtils.getEntityByUUID(level, getOwnerUUID());

		if(entity != null && !(entity instanceof LivingEntity)){ // Should never happen
			Wizardry.logger.warn("{} has a non-living owner!", this);
			entity = null;
		}

		return (LivingEntity)entity;
	}
	
	public void setCaster(@Nullable LivingEntity caster){
		this.casterUUID = caster == null ? null : caster.getUUID();
	}

	/**
	 * Shorthand for {@link AllyDesignationSystem#isValidTarget(Entity, Entity)}, with the owner of this construct as the
	 * attacker. Also allows subclasses to override it if they wish to do so.
	 */
	public boolean isValidTarget(Entity target){
		return AllyDesignationSystem.isValidTarget(this.getCaster(), target);
	}
	
	@Override
	public SoundSource getSoundSource(){
		return WizardrySounds.SPELLS;
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}

	@Override
	public boolean isPushedByWater(){
		return false;
	}
	
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
