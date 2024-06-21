package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityLevitatingBlock;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityBlackHole extends EntityScaledConstruct {

	private static final double SUCTION_STRENGTH = 0.075;
	/** The maximum number of blocks that can be unhooked each tick, reduces lag from excessive numbers of entities. */
	private static final int BLOCK_UNHOOK_LIMIT = 3;

	public int[] randomiser;
	public int[] randomiser2;

	public EntityBlackHole(Level world){
		this(WizardryEntities.BLACK_HOLE.get(), world);
		float r = Spells.BLACK_HOLE.getProperty(Spell.EFFECT_RADIUS).floatValue();
		setSize(r * 2, r);
		randomiser = new int[30];
		for(int i = 0; i < randomiser.length; i++){
			randomiser[i] = this.random.nextInt(10);
		}
		randomiser2 = new int[30];
		for(int i = 0; i < randomiser2.length; i++){
			randomiser2[i] = this.random.nextInt(10);
		}
	}
	
	public EntityBlackHole(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		float r = Spells.BLACK_HOLE.getProperty(Spell.EFFECT_RADIUS).floatValue();
		setSize(r * 2, r);
		randomiser = new int[30];
		for(int i = 0; i < randomiser.length; i++){
			randomiser[i] = this.random.nextInt(10);
		}
		randomiser2 = new int[30];
		for(int i = 0; i < randomiser2.length; i++){
			randomiser2[i] = this.random.nextInt(10);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		randomiser = nbttagcompound.getIntArray("randomiser");
		randomiser2 = nbttagcompound.getIntArray("randomiser2");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putIntArray("randomiser", randomiser);
		nbttagcompound.putIntArray("randomiser2", randomiser2);
	}

	public void tick(){

		super.tick();

		// System.out.println("Client side: " + this.level.isClientSide + ", Caster: " + this.caster);

		// Particle effect. Finishes 40 ticks before the end so the particles disappear at the same time.
		if(this.tickCount + 40 < this.lifetime){
			for(int i = 0; i < 5; i++){
				// this.world.spawnParticle(EnumParticleTypes.PORTAL, this.getX() + (this.random.nextDouble() - 0.5D) *
				// (double)this.width, this.getY() + this.random.nextDouble() * (double)this.getBbHeight() - 0.75D, this.getZ() +
				// (this.random.nextDouble() - 0.5D) * (double)this.width, (this.random.nextDouble() - 0.5D) * 2.0D,
				// -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
				this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(),
						(this.random.nextDouble() - 0.5D) * 4.0D, (this.random.nextDouble() - 0.5D) * 4.0D - 1,
						(this.random.nextDouble() - 0.5D) * 4.0D);
			}
		}

		if(this.lifetime - this.tickCount == 75){
			this.playSound(WizardrySounds.ENTITY_BLACK_HOLE_VANISH, 1.5f, 1.0f);
		}else if(this.tickCount % 80 == 1 && this.tickCount + 80 < this.lifetime){
			this.playSound(WizardrySounds.ENTITY_BLACK_HOLE_AMBIENT, 1.5f, 1.0f);
		}

		if(!this.level.isClientSide){

			double radius = 2 * getBbHeight() * sizeMultiplier;

			boolean suckInBlocks = getCaster() instanceof Player && EntityUtils.canDamageBlocks(getCaster(), level)
					&& ItemArtefact.isArtefactActive((Player)getCaster(), WizardryItems.CHARM_BLACK_HOLE.get());

			if(suckInBlocks){

				List<BlockPos> sphere = BlockUtils.getBlockSphere(this.blockPosition(), radius);

				int blocksUnhooked = 0;

				for(BlockPos pos : sphere){

					if(random.nextInt(Math.max(1, (int)this.distanceToSqr(Vec3.atCenterOf(pos)) * 3)) == 0){

						if(!BlockUtils.isBlockUnbreakable(level, pos) && !level.isEmptyBlock(pos)
								&& level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) && BlockUtils.canBreakBlock(getCaster(), level, pos)){
							// Checks that the block above is not solid, since this causes the falling block to vanish.
//							&& !world.isBlockNormalCube(pos.up(), false)){

							FallingBlockEntity fallingBlock = new EntityLevitatingBlock(level, pos.getX() + 0.5,
									pos.getY() + 0.5, pos.getZ() + 0.5, level.getBlockState(pos));
//							fallingBlock.noClip = true;
							fallingBlock.time = 1; // Prevent it from trying to delete the block itself
							level.addFreshEntity(fallingBlock);
							level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

							if(++blocksUnhooked >= BLOCK_UNHOOK_LIMIT) break; // Lag prevention
						}
					}
				}

			}

			List<Entity> targets = EntityUtils.getEntitiesWithinRadius(radius, this.getX(), this.getY(),
					this.getZ(), this.level, Entity.class);

			targets.removeIf(t -> !(t instanceof LivingEntity || (suckInBlocks && t instanceof FallingBlockEntity)));

			for(Entity target : targets){

				if(this.isValidTarget(target)){

					// If the target can't be moved, it isn't sucked in but is still damaged if it gets too close
					if(!(target instanceof Player && ((getCaster() instanceof Player && !Wizardry.settings.playersMoveEachOther)
							|| ItemArtefact.isArtefactActive((Player)target, WizardryItems.AMULET_ANCHORING.get())))){

						EntityUtils.undoGravity(target);
						if(target instanceof EntityLevitatingBlock) ((EntityLevitatingBlock)target).suspend();

						// Sucks the target in
						if(this.getX() > target.getX() && target.getDeltaMovement().x < 1){
							target.setDeltaMovement(target.getDeltaMovement().add(SUCTION_STRENGTH, 0, 0));
						}else if(this.getX() < target.getX() && target.getDeltaMovement().x > -1){
							target.setDeltaMovement(target.getDeltaMovement().subtract(SUCTION_STRENGTH, 0, 0));
						}

						if(this.getY() > target.getY() && target.getDeltaMovement().y < 1){
							target.setDeltaMovement(target.getDeltaMovement().add(0, SUCTION_STRENGTH, 0));
						}else if(this.getY() < target.getY() && target.getDeltaMovement().y > -1){
							target.setDeltaMovement(target.getDeltaMovement().subtract(0, SUCTION_STRENGTH, 0));
						}

						if(this.getZ() > target.getZ() && target.getDeltaMovement().z < 1){
							target.setDeltaMovement(target.getDeltaMovement().add(0, 0, SUCTION_STRENGTH));
						}else if(this.getZ() < target.getZ() && target.getDeltaMovement().z > -1){
							target.setDeltaMovement(target.getDeltaMovement().subtract(0, 0, SUCTION_STRENGTH));
						}

						// Player motion is handled on that player's client so needs packets
						if(target instanceof ServerPlayer){
							((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
						}
					}

					if(this.distanceTo(target) <= 2){
						// Damages the target if it is close enough, or destroys it if it's a block
						if(target instanceof FallingBlockEntity){
							target.playSound(WizardrySounds.ENTITY_BLACK_HOLE_BREAK_BLOCK, 0.5f,
									(random.nextFloat() - random.nextFloat()) * 0.2f + 1);
							BlockState state = ((FallingBlockEntity)target).getBlockState();
							if(state != null) level.levelEvent(2001, target.blockPosition(), Block.getId(state));
							target.discard();

						}else{
							if(this.getCaster() != null){
								target.hurt(
										MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.MAGIC),
										2 * damageMultiplier);
							}else{
								target.hurt(DamageSource.MAGIC, 2 * damageMultiplier);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderAtSqrDistance(double distance){
		return true;
	}

}
