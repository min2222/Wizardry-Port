package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.RayTracer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.IProjectile;
import net.minecraft.world.entity.monster.EntityEnderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.math.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

/**
 * This class was copied from EntityArrow in the 1.7.10 update as part of the overhaul and major cleanup of the code for
 * the projectiles. It provides a unifying superclass for all <b>directed</b> projectiles (i.e. not spherical stuff like
 * snowballs), namely magic missile, ice shard, force arrow, lightning arrow and dart. All spherical projectiles should
 * extend {@link EntityMagicProjectile}.
 * <p></p>
 * This class handles saving of the damage multiplier and all shared logic. Methods are provided which are triggered at
 * useful points during the entity update cycle as well as a few getters for various properties. Override any of these
 * to change the behaviour (no need to call super for any of them).
 * 
 * @since Wizardry 1.0
 * @author Electroblob
 */
// TODO: Might be a good idea to have this implement OwnableEntity as well
public abstract class EntityMagicArrow extends Entity implements IProjectile, IEntityAdditionalSpawnData {

	public static final double LAUNCH_Y_OFFSET = 0.1;
	public static final int SEEKING_TIME = 15;

	private int blockX = -1;
	private int blockY = -1;
	private int blockZ = -1;
	/** The block the arrow is stuck in */
	private BlockState stuckInBlock;
	/** The metadata of the block the arrow is stuck in */
	private int inData;
	private boolean inGround;
	/** Seems to be some sort of timer for animating an arrow. */
	public int arrowShake;
	/** The owner of this arrow. */
	private WeakReference<LivingEntity> caster;
	/**
	 * The UUID of the caster. Note that this is only for loading purposes; during normal updates the actual entity
	 * instance is stored (so that getEntityByUUID is not called constantly), so this will not always be synced (this is
	 * why it is private).
	 */
	private UUID casterUUID;
	int ticksInGround;
	int ticksInAir;
	/** The amount of knockback an arrow applies when it hits a mob. */
	private int knockbackStrength;
	/** The damage multiplier for the projectile. */
	public float damageMultiplier = 1.0f;

	/** Creates a new projectile in the given world. */
	public EntityMagicArrow(Level world){
		super(world);
		this.setSize(0.5F, 0.5F);
	}
	
	// Initialiser methods
	
	/** Sets the shooter of the projectile to the given caster, positions the projectile at the given caster's eyes and
	 * aims it in the direction they are looking with the given speed. */
	public void aim(LivingEntity caster, float speed){
		
		this.setCaster(caster);
		
		this.setLocationAndAngles(caster.getX(), caster.getY() + (double)caster.getEyeHeight() - LAUNCH_Y_OFFSET,
				caster.getZ(), caster.rotationYaw, caster.rotationPitch);
		
		this.getX() -= (double)(Mth.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		this.getY() -= 0.10000000149011612D;
		this.getZ() -= (double)(Mth.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
		
		this.setPosition(this.getX(), this.getY(), this.getZ());
		
		// yOffset was set to 0 here, but that has been replaced by getYOffset(), which returns 0 in Entity anyway.
		this.motionX = (double)(-Mth.sin(this.rotationYaw / 180.0F * (float)Math.PI)
				* Mth.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionY = (double)(-Mth.sin(this.rotationPitch / 180.0F * (float)Math.PI));
		this.motionZ = (double)(Mth.cos(this.rotationYaw / 180.0F * (float)Math.PI)
				* Mth.cos(this.rotationPitch / 180.0F * (float)Math.PI));
		
		this.shoot(this.motionX, this.motionY, this.motionZ, speed * 1.5F, 1.0F);
	}

	/** Sets the shooter of the projectile to the given caster, positions the projectile at the given caster's eyes and
	 * aims it at the given target with the given speed. The trajectory will be altered slightly by a random amount
	 * determined by the aimingError parameter. For reference, skeletons set this to 10 on easy, 6 on normal and 2 on hard
	 * difficulty. */
	public void aim(LivingEntity caster, Entity target, float speed, float aimingError){
		
		this.setCaster(caster);

		this.getY() = caster.getY() + (double)caster.getEyeHeight() - LAUNCH_Y_OFFSET;
		double dx = target.getX() - caster.getX();
		double dy = this.doGravity() ? target.getY() + (double)(target.getBbHeight() / 3.0f) - this.getY()
				: target.getY() + (double)(target.getBbHeight() / 2.0f) - this.getY();
		double dz = target.getZ() - caster.getZ();
		double horizontalDistance = (double) Math.sqrt(dx * dx + dz * dz);

		if(horizontalDistance >= 1.0E-7D){
			float yaw = (float)(Math.atan2(dz, dx) * 180.0d / Math.PI) - 90.0f;
			float pitch = (float)(-(Math.atan2(dy, horizontalDistance) * 180.0d / Math.PI));
			double dxNormalised = dx / horizontalDistance;
			double dzNormalised = dz / horizontalDistance;
			this.setLocationAndAngles(caster.getX() + dxNormalised, this.getY(), caster.getZ() + dzNormalised, yaw, pitch);
			// yOffset was set to 0 here, but that has been replaced by getYOffset(), which returns 0 in Entity anyway.

			// Depends on the horizontal distance between the two entities and accounts for bullet drop,
			// but of course if gravity is ignored this should be 0 since there is no bullet drop.
			float bulletDropCompensation = this.doGravity() ? (float)horizontalDistance * 0.2f : 0;
			this.shoot(dx, dy + (double)bulletDropCompensation, dz, speed, aimingError);
		}
	}
	
	// Property getters (to be overridden by subclasses)

	/** Subclasses must override this to set their own base damage. */
	public abstract double getDamage();

	/** Returns the maximum flight time in ticks before this projectile disappears, or -1 if it can continue
	 * indefinitely until it hits something. This should be constant. */
	public abstract int getLifetime();

	/** Override this to specify the damage type dealt. Defaults to {@link DamageType#MAGIC}. */
	public DamageType getDamageType(){
		return DamageType.MAGIC;
	}

	/** Override this to disable gravity. Returns true by default. */
	public boolean doGravity(){
		return true;
	}

	/**
	 * Override this to disable deceleration (generally speaking, this isn't noticeable unless gravity is turned off).
	 * Returns true by default.
	 */
	public boolean doDeceleration(){
		return true;
	}

	/**
	 * Override this to allow the projectile to pass through mobs intact (the onEntityHit method will still be called
	 * and damage will still be applied). Returns false by default.
	 */
	public boolean doOverpenetration(){
		return false;
	}

	/**
	 * Returns the seeking strength of this projectile, or the maximum distance from a target the projectile can be
	 * heading for that will make it curve towards that target. By default, this is 2 if the caster is wearing a ring
	 * of attraction, otherwise it is 0.
	 */
	public float getSeekingStrength(){
		return getCaster() instanceof Player && ItemArtefact.isArtefactActive((Player)getCaster(),
				WizardryItems.ring_seeking) ? 2 : 0;
	}

	// Setters and getters

	/** Sets the amount of knockback the projectile applies when it hits a mob. */
	public void setKnockbackStrength(int knockback){
		this.knockbackStrength = knockback;
	}
	
	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this construct simply had no caster in the first place.
	 */
	public LivingEntity getCaster(){
		return caster == null ? null : caster.get();
	}

	public void setCaster(LivingEntity entity){
		caster = new WeakReference<>(entity);
	}
	
	// Methods triggered during the update cycle

	/** Called each tick when the projectile is in a block. Defaults to discard(), but can be overridden to change the
	 * behaviour. */
	protected void tickInGround(){
		this.discard();
	}

	/** Called each tick when the projectile is in the air. Override to add particles and such like. */
	protected void tickInAir(){}

	/** Called when the projectile hits an entity. Override to add potion effects and such like. */
	protected void onEntityHit(LivingEntity entityHit){}

	/** Called when the projectile hits a block. Override to add sound effects and such like. 
	 * @param hit A vector representing the exact coordinates of the hit; use this to centre particle effects, for
	 * example. */
	protected void onBlockHit(HitResult hit){}

	@Override
	public void tick(){

		super.tick();

		// Projectile disappears after its lifetime (if it has one) has elapsed
		if(getLifetime() >=0 && this.tickCount > getLifetime()){
			this.discard();
		}

		if(this.getCaster() == null && this.casterUUID != null){
			Entity entity = EntityUtils.getEntityByUUID(world, casterUUID);
			if(entity instanceof LivingEntity){
				this.caster = new WeakReference<>((LivingEntity)entity);
			}
		}

		if(this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F){
			float f = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D
					/ Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f) * 180.0D
					/ Math.PI);
		}

		BlockPos blockpos = new BlockPos(this.blockX, this.blockY, this.blockZ);
		BlockState iblockstate = this.level.getBlockState(blockpos);

		if(iblockstate.getMaterial() != Material.AIR){
			AABB axisalignedbb = iblockstate.getCollisionBoundingBox(this.world, blockpos);

			if(axisalignedbb != Block.NULL_AABB
					&& axisalignedbb.offset(blockpos).contains(new Vec3(this.getX(), this.getY(), this.getZ()))){
				this.inGround = true;
			}
		}

		if(this.arrowShake > 0){
			--this.arrowShake;
		}

		// When the arrow is in the ground
		if(this.inGround){
			++this.ticksInGround;
			this.tickInGround();
		}
		// When the arrow is in the air
		else{

			this.tickInAir();

			this.ticksInGround = 0;
			++this.ticksInAir;
			
			// Does a ray trace to determine whether the projectile will hit a block in the next tick
			
			Vec3 vec3d1 = new Vec3(this.getX(), this.getY(), this.getZ());
			Vec3 vec3d = new Vec3(this.getX() + this.motionX, this.getY() + this.motionY, this.getZ() + this.motionZ);
			HitResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);
			vec3d1 = new Vec3(this.getX(), this.getY(), this.getZ());
			vec3d = new Vec3(this.getX() + this.motionX, this.getY() + this.motionY, this.getZ() + this.motionZ);

			if(raytraceresult != null){
				vec3d = new Vec3(raytraceresult.hitVec.x, raytraceresult.hitVec.y,
						raytraceresult.hitVec.z);
			}
			
			// Uses bounding boxes to determine whether the projectile will hit an entity in the next tick, and if so
			// overwrites the block hit with an entity

			Entity entity = null;
			List<?> list = this.level.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox()
					.expand(this.motionX, this.motionY, this.motionZ).grow(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;
			int i;
			float f1;

			for(i = 0; i < list.size(); ++i){
				Entity entity1 = (Entity)list.get(i);

				if(entity1.canBeCollidedWith() && (entity1 != this.getCaster() || this.ticksInAir >= 5)){
					f1 = 0.3F;
					AABB axisalignedbb1 = entity1.getBoundingBox().grow((double)f1, (double)f1,
							(double)f1);
					HitResult RayTraceResult1 = axisalignedbb1.calculateIntercept(vec3d1, vec3d);

					if(RayTraceResult1 != null){
						double d1 = vec3d1.distanceTo(RayTraceResult1.hitVec);

						if(d1 < d0 || d0 == 0.0D){
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if(entity != null){
				raytraceresult = new HitResult(entity);
			}

			// Players that are considered invulnerable to the caster allow the projectile to pass straight through
			// them.
			if(raytraceresult != null && raytraceresult.entityHit != null
					&& raytraceresult.entityHit instanceof Player){
				Player entityplayer = (Player)raytraceresult.entityHit;

				if(entityplayer.capabilities.disableDamage || this.getCaster() instanceof Player
						&& !((Player)this.getCaster()).canAttackPlayer(entityplayer)){
					raytraceresult = null;
				}
			}

			// If the arrow hits something
			if(raytraceresult != null){
				// If the arrow hits an entity
				if(raytraceresult.entityHit != null){
					DamageSource damagesource = null;

					if(this.getCaster() == null){
						damagesource = DamageSource.causeThrownDamage(this, this);
					}else{
						damagesource = MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), this.getDamageType()).setProjectile();
					}

					if(raytraceresult.entityHit.hurt(damagesource,
							(float)(this.getDamage() * this.damageMultiplier))){
						if(raytraceresult.entityHit instanceof LivingEntity){
							LivingEntity entityHit = (LivingEntity)raytraceresult.entityHit;

							this.onEntityHit(entityHit);

							if(this.knockbackStrength > 0){
								float f4 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

								if(f4 > 0.0F){
									raytraceresult.entityHit.addVelocity(
											this.motionX * (double)this.knockbackStrength * 0.6000000238418579D
													/ (double)f4,
											0.1D, this.motionZ * (double)this.knockbackStrength * 0.6000000238418579D
													/ (double)f4);
								}
							}

							// Thorns enchantment
							if(this.getCaster() != null){
								EnchantmentHelper.applyThornEnchantments(entityHit, this.getCaster());
								EnchantmentHelper.applyArthropodEnchantments(this.getCaster(), entityHit);
							}

							if(this.getCaster() != null && raytraceresult.entityHit != this.getCaster()
									&& raytraceresult.entityHit instanceof Player
									&& this.getCaster() instanceof ServerPlayer){
								((ServerPlayer)this.getCaster()).connection
										.sendPacket(new SPacketChangeGameState(6, 0.0F));
							}
						}

						if(!(raytraceresult.entityHit instanceof EntityEnderman) && !this.doOverpenetration()){
							this.discard();
						}
					}else{
						if(!this.doOverpenetration()) this.discard();

						// Was the 'rebound' that happened when entities were immune to damage
						/* this.motionX *= -0.10000000149011612D; this.motionY *= -0.10000000149011612D; this.motionZ *=
						 * -0.10000000149011612D; this.rotationYaw += 180.0F; this.prevRotationYaw += 180.0F;
						 * this.ticksInAir = 0; */
					}
				}
				// If the arrow hits a block
				else{
					this.blockX = raytraceresult.getBlockPos().getX();
					this.blockY = raytraceresult.getBlockPos().getY();
					this.blockZ = raytraceresult.getBlockPos().getZ();
					this.stuckInBlock = this.level.getBlockState(raytraceresult.getBlockPos());
					this.motionX = (double)((float)(raytraceresult.hitVec.x - this.getX()));
					this.motionY = (double)((float)(raytraceresult.hitVec.y - this.getY()));
					this.motionZ = (double)((float)(raytraceresult.hitVec.z - this.getZ()));
					// f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ *
					// this.motionZ);
					// this.getX() -= this.motionX / (double)f2 * 0.05000000074505806D;
					// this.getY() -= this.motionY / (double)f2 * 0.05000000074505806D;
					// this.getZ() -= this.motionZ / (double)f2 * 0.05000000074505806D;
					// this.playSound("random.bowhit", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
					this.inGround = true;
					this.arrowShake = 7;

					this.onBlockHit(raytraceresult);

					if(this.stuckInBlock.getMaterial() != Material.AIR){
						this.stuckInBlock.getBlock().onEntityCollision(this.world, raytraceresult.getBlockPos(),
								this.stuckInBlock, this);
					}
				}
			}

			// Seeking
			if(getSeekingStrength() > 0){

				Vec3 velocity = new Vec3(motionX, motionY, motionZ);

				HitResult hit = RayTracer.rayTrace(world, this.position(),
						this.position().add(velocity.scale(SEEKING_TIME)), getSeekingStrength(), false,
						true, false, LivingEntity.class, RayTracer.ignoreEntityFilter(null));

				if(hit != null && hit.entityHit != null){

					if(AllyDesignationSystem.isValidTarget(getCaster(), hit.entityHit)){

						Vec3 direction = new Vec3(hit.entityHit.getX(), hit.entityHit.getY() + hit.entityHit.getBbHeight()/2,
								hit.entityHit.getZ()).subtract(this.position()).normalize().scale(velocity.length());

						motionX = motionX + 2 * (direction.x - motionX) / SEEKING_TIME;
						motionY = motionY + 2 * (direction.y - motionY) / SEEKING_TIME;
						motionZ = motionZ + 2 * (direction.z - motionZ) / SEEKING_TIME;
					}
				}
			}

			this.getX() += this.motionX;
			this.getY() += this.motionY;
			this.getZ() += this.motionZ;
			// f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

			// for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f2) * 180.0D / Math.PI);
			// this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
			// {
			// ;
			// }

			while(this.rotationPitch - this.prevRotationPitch >= 180.0F){
				this.prevRotationPitch += 360.0F;
			}

			while(this.rotationYaw - this.prevRotationYaw < -180.0F){
				this.prevRotationYaw -= 360.0F;
			}

			while(this.rotationYaw - this.prevRotationYaw >= 180.0F){
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

			float f3 = 0.99F;

			if(this.isInWater()){
				for(int l = 0; l < 4; ++l){
					float f4 = 0.25F;
					this.world.spawnParticle(ParticleTypes.WATER_BUBBLE, this.getX() - this.motionX * (double)f4,
							this.getY() - this.motionY * (double)f4, this.getZ() - this.motionZ * (double)f4, this.motionX,
							this.motionY, this.motionZ);
				}

				f3 = 0.8F;
			}

			if(this.isWet()){
				this.extinguish();
			}

			if(this.doDeceleration()){
				this.motionX *= (double)f3;
				this.motionY *= (double)f3;
				this.motionZ *= (double)f3;
			}

			if(this.doGravity()) this.motionY -= 0.05;

			this.setPosition(this.getX(), this.getY(), this.getZ());
			this.doBlockCollisions();
		}
	}

	@Override
	public void shoot(double x, double y, double z, float speed, float randomness){
		float f2 = Math.sqrt(x * x + y * y + z * z);
		x /= (double)f2;
		y /= (double)f2;
		z /= (double)f2;
		x += this.random.nextGaussian() * (double)(this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)randomness;
		y += this.random.nextGaussian() * (double)(this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)randomness;
		z += this.random.nextGaussian() * (double)(this.random.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)randomness;
		x *= (double)speed;
		y *= (double)speed;
		z *= (double)speed;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f3 = Math.sqrt(x * x + z * z);
		this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(x, z) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(y, (double)f3) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}

	// There was an override for setPositionAndRotationDirect here, but it was exactly the same as the superclass
	// method (in Entity), so it was removed since it was redundant.

	/** Sets the velocity to the args. Args: x, y, z. THIS IS CLIENT SIDE ONLY! DO NOT USE IN COMMON OR SERVER CODE! */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_){
		this.motionX = p_70016_1_;
		this.motionY = p_70016_3_;
		this.motionZ = p_70016_5_;

		if(this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F){
			float f = Math.sqrt(p_70016_1_ * p_70016_1_ + p_70016_5_ * p_70016_5_);
			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(p_70016_1_, p_70016_5_) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(p_70016_3_, (double)f) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.getX(), this.getY(), this.getZ(), this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}
	}
	
	// Data reading and writing

	@Override
	public void writeEntityToNBT(CompoundTag tag){
		tag.setShort("xTile", (short)this.blockX);
		tag.setShort("yTile", (short)this.blockY);
		tag.setShort("zTile", (short)this.blockZ);
		tag.setShort("life", (short)this.ticksInGround);
		if(this.stuckInBlock != null){
			ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(this.stuckInBlock.getBlock());
			tag.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
		}
		tag.setByte("inData", (byte)this.inData);
		tag.setByte("shake", (byte)this.arrowShake);
		tag.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		tag.setFloat("damageMultiplier", this.damageMultiplier);
		if(this.getCaster() != null){
			tag.setUniqueId("casterUUID", this.getCaster().getUUID());
		}
	}

	@Override
	public void readEntityFromNBT(CompoundTag tag){
		this.blockX = tag.getShort("xTile");
		this.blockY = tag.getShort("yTile");
		this.blockZ = tag.getShort("zTile");
		this.ticksInGround = tag.getShort("life");
		// Commented out for now because there's some funny stuff going on with blockstates and id.
		// this.stuckInBlock = Block.getBlockById(tag.getByte("inTile") & 255);
		this.inData = tag.getByte("inData") & 255;
		this.arrowShake = tag.getByte("shake") & 255;
		this.inGround = tag.getByte("inGround") == 1;
		this.damageMultiplier = tag.getFloat("damageMultiplier");
		casterUUID = tag.getUUID("casterUUID");
	}
	
	@Override
	public void writeSpawnData(ByteBuf buffer){
		if(this.getCaster() != null) buffer.writeInt(this.getCaster().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf buffer){
		if(buffer.isReadable()) this.caster = new WeakReference<>(
				(LivingEntity)this.level.getEntityByID(buffer.readInt()));
	}

	// Miscellaneous overrides
	
	@Override
	protected boolean canTriggerWalking(){
		return false;
	}
	
	@Override
	public boolean canBeAttackedWithItem(){
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public float getShadowSize(){
		return 0.0F;
	}
	
	@Override
	public SoundSource getSoundCategory(){
		return WizardrySounds.SPELLS;
	}

	@Override
	protected void entityInit(){}
}