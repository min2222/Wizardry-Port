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
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFalling;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
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

		      if (this.blockState.isAir()) {
		          this.discard();
		       } else {
		          Block block = this.blockState.getBlock();
		          ++this.time;
		          if (!this.isNoGravity()) {
		             this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
		          }

		          this.move(MoverType.SELF, this.getDeltaMovement());
		          if (!this.level.isClientSide) {
		             BlockPos blockpos = this.blockPosition();
		             boolean flag = this.blockState.getBlock() instanceof ConcretePowderBlock;
		             boolean flag1 = flag && this.blockState.canBeHydrated(this.level, blockpos, this.level.getFluidState(blockpos), blockpos);
		             double d0 = this.getDeltaMovement().lengthSqr();
		             if (flag && d0 > 1.0D) {
		                BlockHitResult blockhitresult = this.level.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
		                if (blockhitresult.getType() != HitResult.Type.MISS && this.blockState.canBeHydrated(this.level, blockpos, this.level.getFluidState(blockhitresult.getBlockPos()), blockhitresult.getBlockPos())) {
		                   blockpos = blockhitresult.getBlockPos();
		                   flag1 = true;
		                }
		             }

		             if (!this.onGround && !flag1) {
		                if (!this.level.isClientSide && (this.time > 100 && (blockpos.getY() <= this.level.getMinBuildHeight() || blockpos.getY() > this.level.getMaxBuildHeight()) || this.time > 600)) {
		                   if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
		                      this.spawnAtLocation(block);
		                   }

		                   this.discard();
		                }
		             } else {
		                BlockState blockstate = this.level.getBlockState(blockpos);
		                this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
		                if (!blockstate.is(Blocks.MOVING_PISTON)) {
		                   if (!this.cancelDrop) {
		                      boolean flag2 = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level, blockpos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
		                      boolean flag3 = FallingBlock.isFree(this.level.getBlockState(blockpos.below())) && (!flag || !flag1);
		                      boolean flag4 = this.blockState.canSurvive(this.level, blockpos) && !flag3;
		                      if (flag2 && flag4) {
		                         if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level.getFluidState(blockpos).getType() == Fluids.WATER) {
		                            this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
		                         }

		                         if (this.level.setBlock(blockpos, this.blockState, 3)) {
		                            ((ServerLevel)this.level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockpos, this.level.getBlockState(blockpos)));
		                            this.discard();
		                            if (block instanceof Fallable) {
		                               ((Fallable)block).onLand(this.level, blockpos, this.blockState, blockstate, this);
		                            }

		                            if (this.blockData != null && this.blockState.hasBlockEntity()) {
		                               BlockEntity blockentity = this.level.getBlockEntity(blockpos);
		                               if (blockentity != null) {
		                                  CompoundTag compoundtag = blockentity.saveWithoutMetadata();

		                                  for(String s : this.blockData.getAllKeys()) {
		                                     compoundtag.put(s, this.blockData.get(s).copy());
		                                  }

		                                  try {
		                                     blockentity.load(compoundtag);
		                                  } catch (Exception exception) {
		                                     LOGGER.error("Failed to load block entity from falling block", (Throwable)exception);
		                                  }

		                                  blockentity.setChanged();
		                               }
		                            }
		                         } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
		                            this.discard();
		                            this.callOnBrokenAfterFall(block, blockpos);
		                            this.spawnAtLocation(block);
		                         }
		                      } else {
		                         this.discard();
		                         if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
		                            this.callOnBrokenAfterFall(block, blockpos);
		                            this.spawnAtLocation(block);
		                         }
		                      }
		                   } else {
		                      this.discard();
		                      this.callOnBrokenAfterFall(block, blockpos);
		                   }
		                }
		             }
		          }

		          this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
		       }

			// === End super copy ===
		}

		double velocitySquared = getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().y * getDeltaMovement().y + getDeltaMovement().z * getDeltaMovement().z;

		if(velocitySquared >= 0.2){

			List<Entity> list = this.level.getEntities(this, this.getBoundingBox());

			for(Entity entity : list){

				if(entity instanceof LivingEntity && isValidTarget(entity)){

					float damage = Spells.GREATER_TELEKINESIS.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
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
