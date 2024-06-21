package electroblob.wizardry.entity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFalling;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/** Custom extended version of {@link EntityFallingBlock} for use with the greater telekinesis spell. */
public class EntityLevitatingBlock extends FallingBlockEntity implements IEntityAdditionalSpawnData {

	private static final Field fallTile;

	static {
		fallTile = ObfuscationReflectionHelper.findField(FallingBlock.class, "field_175132_d");
		fallTile.setAccessible(true);
	}

	/** The entity that created this levitating block */
	private WeakReference<LivingEntity> caster;

	/**
	 * The UUID of the caster. Note that this is only for loading purposes; during normal updates the actual entity
	 * instance is stored (so that getEntityByUUID is not called constantly), so this will not always be synced (this is
	 * why it is private).
	 */
	private UUID casterUUID;

	/** The damage multiplier for this levitating block, determined by the wand with which it was cast. */
	public float damageMultiplier = 1.0f;

	private int suspendTimer = 5;

	public EntityLevitatingBlock(Level world){
		this(WizardryEntities.LEVITATING_BLOCK.get(), world);
	}
	
	public EntityLevitatingBlock(EntityType<? extends FallingBlockEntity> type, Level world){
		super(type, world);
	}

	public EntityLevitatingBlock(Level world, double x, double y, double z, BlockState state){
		super(world, x, y, z, state);
	}

	/** Resets the suspension timer to 5 ticks, during which this block will not re-attach itself to the ground. */
	public void suspend(){
		suspendTimer = 5;
	}

	@Override
	public void tick(){

		if(suspendTimer > 0){
			suspendTimer--;
		}

		if(this.getCaster() == null && this.casterUUID != null){
			Entity entity = EntityUtils.getEntityByUUID(level, casterUUID);
			if(entity instanceof LivingEntity){
				this.caster = new WeakReference<>((LivingEntity)entity);
			}
		}

		if(getBlockState() != null){

			// === Copied from super ===

			Block block = getBlockState().getBlock();

			if(getBlockState().getMaterial() == Material.AIR){
				this.discard();

			}else{

				this.xo = this.getX();
				this.yo = this.getY();
				this.zo = this.getZ();

				if(this.time++ == 0){

					BlockPos blockpos = this.blockPosition();

					if(this.level.getBlockState(blockpos).getBlock() == block){
						this.level.setBlockAndUpdate(blockpos, Blocks.AIR.defaultBlockState());
					}else if(!this.level.isClientSide){
						this.discard();
						return;
					}
				}

				if(!this.isNoGravity()){
					this.setDeltaMovement(this.getDeltaMovement().subtract(0, 0.03999999910593033D, 0));
				}

				this.move(MoverType.SELF, this.getDeltaMovement());

				if(!this.level.isClientSide){

					BlockPos blockpos1 = this.blockPosition();
					boolean isConcrete = getBlockState().getBlock() == Blocks.CONCRETE_POWDER;
					boolean isConcreteInWater = isConcrete && this.level.getBlockState(blockpos1).getMaterial() == Material.WATER;
					double d0 = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;

					if(isConcrete && d0 > 1.0D){

						HitResult raytraceresult = this.level.clip(new Vec3(this.xo, this.yo, this.zo), new Vec3(this.getX(), this.getY(), this.getZ()), true);

						if(raytraceresult != null && this.level.getBlockState(raytraceresult.getBlockPos()).getMaterial() == Material.WATER){
							blockpos1 = raytraceresult.getBlockPos();
							isConcreteInWater = true;
						}
					}

					if(!this.onGround && !isConcreteInWater){

						if(this.time > 100 && !this.level.isClientSide && (blockpos1.getY() < 1 || blockpos1.getY() > 256) || this.time > 600){
							this.discard();
						}

					}else{

						BlockState iblockstate = this.level.getBlockState(blockpos1);

						if(this.level.isEmptyBlock(new BlockPos(this.getX(), this.getY() - 0.009999999776482582D, this.getZ()))){
							if(!isConcreteInWater && FallingBlock.canFallThrough(this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 0.009999999776482582D, this.getZ())))){
								this.onGround = false;
								return;
							}
						}

						this.motionX *= 0.699999988079071D;
						this.motionZ *= 0.699999988079071D;
						this.motionY *= -0.5D;

						if(iblockstate.getBlock() != Blocks.PISTON_EXTENSION){

							if(suspendTimer == 0){

								this.discard(); // Moved inside the above if statement

								if(this.level.mayPlace(block, blockpos1, true, Direction.UP, null)
										&& (isConcreteInWater || !FallingBlock.canFallThrough(this.level.getBlockState(blockpos1.below())))
										&& this.level.setBlock(blockpos1, getBlockState(), 3)){

									if(block instanceof FallingBlock){
										((FallingBlock)block).onEndFalling(this.level, blockpos1, getBlockState(), iblockstate);
									}

									if(this.blockData != null && block.defaultBlockState().hasBlockEntity()){

										BlockEntity tileentity = this.level.getBlockEntity(blockpos1);

										if(tileentity != null){

											CompoundTag nbttagcompound = tileentity.writeToNBT(new CompoundTag());

											for(String s : this.blockData.getAllKeys()){
												Tag nbtbase = this.blockData.getCompound(s);

												if(!"x".equals(s) && !"y".equals(s) && !"z".equals(s)){
													NBTExtras.storeTagSafely(nbttagcompound, s, nbtbase.copy());
												}
											}

											tileentity.load(nbttagcompound);
											tileentity.markDirty();
										}
									}

								}else{
									// Never drops the block, instead if it can't reattach to the world it breaks
									level.levelEvent(2001, this.blockPosition(), Block.getId(getBlockState()));
								}
							}
						}
					}
				}

				this.setDeltaMovement(this.getDeltaMovement().scale(0.9800000190734863D));
			}

			// === End super copy ===
		}

		double velocitySquared = motionX * motionX + motionY * motionY + motionZ * motionZ;

		if(velocitySquared >= 0.2){

			List<Entity> list = this.level.getEntities(this, this.getBoundingBox());

			for(Entity entity : list){

				if(entity instanceof LivingEntity && isValidTarget(entity)){

					float damage = Spells.greater_telekinesis.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
					damage *= Math.min(1, velocitySquared/0.4); // Reduce damage at low speeds

					entity.hurt(MagicDamage.causeIndirectMagicDamage(this, getCaster(),
							MagicDamage.DamageType.FORCE), damage);

					double dx = -this.getDeltaMovement().x;
					double dz;
					for(dz = -this.getDeltaMovement().z; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random()) * 0.01D){
						dx = (Math.random() - Math.random()) * 0.01D;
					}
					((LivingEntity)entity).knockback(0.6f, dx, dz);
				}
			}
		}

	}

	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this construct simply had no caster in the first place.
	 */
	public LivingEntity getCaster(){
		return caster == null ? null : caster.get();
	}

	public void setCaster(LivingEntity caster){
		if(getCaster() != caster) this.caster = new WeakReference<>(caster);
	}

	/**
	 * Shorthand for {@link AllyDesignationSystem#isValidTarget(Entity, Entity)}, with the owner of this construct as the
	 * attacker. Also allows subclasses to override it if they wish to do so.
	 */
	public boolean isValidTarget(Entity target){
		return AllyDesignationSystem.isValidTarget(this.getCaster(), target);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		casterUUID = nbttagcompound.getUUID("casterUUID");
		damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		if(this.getCaster() != null){
			nbttagcompound.putUUID("casterUUID", this.getCaster().getUUID());
		}
		nbttagcompound.putFloat("damageMultiplier", damageMultiplier);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buf){
		if(buf.isReadable()){
			Block block = Block.REGISTRY.getObjectById(buf.readInt());
			try{
				fallTile.set(this, block.getStateFromMeta(buf.readInt()));
			}catch(IllegalAccessException e){
				Wizardry.logger.error("Error reading levitating block data from packet: ", e);
			}
		}
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buf){
		if(getBlockState() != null){
			buf.writeInt(Block.REGISTRY.getIDForObject(getBlock().getBlock()));
			buf.writeInt(getBlockState().getBlock().getMetaFromState(getBlock()));
		}
	}
}
