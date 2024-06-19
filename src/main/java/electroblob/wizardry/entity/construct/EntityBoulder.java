package electroblob.wizardry.entity.construct;

import java.util.List;

import com.google.common.collect.Lists;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Boulder;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityBoulder extends EntityScaledConstruct {

	private double velX, velZ;
	private int hitsRemaining;
	private boolean soundStarted = false;

	public EntityBoulder(Level world){
		this(WizardryEntities.BOULDER.get(), world);
		setSize(2.375f, 2.375f);
		this.noPhysics = false;
		this.setNoGravity(false);
		hitsRemaining = 5;
	}
	
	public EntityBoulder(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(2.375f, 2.375f);
		this.noPhysics = false;
		this.setNoGravity(false);
		hitsRemaining = 5;
	}
	
	@Override
	public float getStepHeight() {
		return 1;
	}

	public void setHorizontalVelocity(double velX, double velZ){
		this.velX = velX;
		this.velZ = velZ;
	}

	@Override
	public void tick(){

		if(level.isClientSide && !soundStarted && onGround){
			soundStarted = true;
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_BOULDER_ROLL, WizardrySounds.SPELLS, 1, 1, true);
		}

		super.tick();

		this.setDeltaMovement(this.getDeltaMovement().subtract(0, 0.03999999910593033D, 0)); // Gravity

		this.move(MoverType.SELF, new Vec3(velX, this.getDeltaMovement().y, velZ));

		// Entity damage
		List<LivingEntity> collided = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());

		float damage = Spells.boulder.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
		float knockback = Spells.boulder.getProperty(Boulder.KNOCKBACK_STRENGTH).floatValue();

		for(LivingEntity entity : collided){

			if(!isValidTarget(entity)) break;

			boolean crushBonus = entity.getY() < this.getY()
					&& entity.getBoundingBox().minX > this.getBoundingBox().minX
					&& entity.getBoundingBox().maxX < this.getBoundingBox().maxX
					&& entity.getBoundingBox().minZ > this.getBoundingBox().minZ
					&& entity.getBoundingBox().maxZ < this.getBoundingBox().maxZ;

			if(EntityUtils.attackEntityWithoutKnockback(entity, MagicDamage.causeIndirectMagicDamage(this,
					getCaster(), DamageType.MAGIC), crushBonus ? damage * 1.5f : damage) && !crushBonus){
				// Only knock back if not crushing
				EntityUtils.applyStandardKnockback(this, entity, knockback);
				entity.setDeltaMovement(entity.getDeltaMovement().add(this.getDeltaMovement().x, 0, this.getDeltaMovement().z));
			}
			entity.playSound(WizardrySounds.ENTITY_BOULDER_HIT, 1, 1);
		}

		// Wall smashing
		if(EntityUtils.canDamageBlocks(getCaster(), level) && horizontalCollision){
			AABB box = getBoundingBox().move(velX, 0, velZ);
			List<BlockPos> cuboid = Lists.newArrayList(BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY),
					Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ)));
			smashBlocks(cuboid, true);
		}

		// Trailing particles
		for(int i = 0; i < 10; i++){

			double particleX = this.getX() + getBbWidth() * 0.7 * (random.nextDouble() - 0.5);
			double particleZ = this.getZ() + getBbWidth() * 0.7 * (random.nextDouble() - 0.5);

			BlockState block = level.getBlockState(this.blockPosition().below());

			if(block.getBlock() != Blocks.AIR){
				level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block), particleX, this.getY(), particleZ,
						0, 0.2, 0);
			}
		}
	}

	/**
	 * Attempts to smash the given list of blocks. If the boulder has exhausted its hits, or if any of the blocks are
	 * too hard to be smashed and breakIfTooHard is true, the boulder is destroyed.
	 * @param blocks A list of block positions at which any solid blocks that are weak enough will be destroyed
	 * @param breakIfTooHard True if the boulder should break if any of the blocks are too hard, false if it should stop
	 *                       on those blocks
	 * @return True if something was destroyed (either the boulder, one or more blocks, or both), false otherwise
	 */
	private boolean smashBlocks(List<BlockPos> blocks, boolean breakIfTooHard){

		if(blocks.removeIf(p -> level.getBlockState(p).getExplosionResistance(level, p, null) > 3
				|| (!level.isClientSide && !BlockUtils.canBreakBlock(getCaster(), level, p)))){
			// If any of the blocks were not breakable, the boulder is smashed
			if(breakIfTooHard){
				this.despawn();
			}else{
				return false;
			}

		}else{

			if(!level.isClientSide){
				blocks.forEach(p -> level.destroyBlock(p, false));
				if(--hitsRemaining <= 0) this.despawn();
			}else{
				level.playLocalSound(getX(), getY(), getZ(), WizardrySounds.ENTITY_BOULDER_BREAK_BLOCK, SoundSource.BLOCKS, 1, 1, false);
			}
		}

		shakeNearbyPlayers();
		return true;
	}

	private void shakeNearbyPlayers(){
		EntityUtils.getEntitiesWithinRadius(10, getX(), getY(), getZ(), level, Player.class)
				.forEach(p -> Wizardry.proxy.shakeScreen(p, 8));
	}

	@Override
	public void despawn(){

		if(level.isClientSide){

			for(int i = 0; i < 200; i++){
				double x = getX() + (random.nextDouble() - 0.5) * getBbWidth();
				double y = getY() + random.nextDouble() * getBbHeight();
				double z = getZ() + (random.nextDouble() - 0.5) * getBbWidth();
				level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), x, y, z, (x - getX()) * 0.1,
						(y - getY() + getBbHeight() / 2) * 0.1, (z - getZ()) * 0.1);
			}

			level.playLocalSound(getX(), getY(), getZ(), WizardrySounds.ENTITY_BOULDER_BREAK_BLOCK, SoundSource.BLOCKS, 1, 1, false);
		}

		super.despawn();
	}

	@Override
	public void move(MoverType type, Vec3 vec3){
		super.move(type, vec3);
		double x = vec3.x;
		double y = vec3.y;
		double z = vec3.z;
		this.setXRot((float) (this.getXRot() + Math.toDegrees(Math.sqrt(x*x + y*y + z*z) / (getBbWidth()/2)))); // That's how we roll
	}
	
	@Override
	public void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos){

		super.checkFallDamage(distance, onGround, state ,pos);

		// Floor smashing
		if(EntityUtils.canDamageBlocks(getCaster(), level) && distance > 3){
			AABB box = getBoundingBox().move(velX, getDeltaMovement().y, velZ);
			List<BlockPos> cuboid = Lists.newArrayList(BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY),
					Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ)));
			if(smashBlocks(cuboid, distance > 8)) return;
			hitsRemaining--;
		}

		if(level.isClientSide){

			// Landing particles
			for(int i = 0; i < 40; i++){

				double particleX = this.getX() - 1.5 + 3 * random.nextDouble();
				double particleZ = this.getZ() - 1.5 + 3 * random.nextDouble();
				// Roundabout way of getting a block instance for the block the boulder is standing on (if any).
				BlockState block = level.getBlockState(new BlockPos(this.getX(), this.getY() - 2, this.getZ()));

				if(block.getBlock() != Blocks.AIR){
					level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block), particleX, this.getY(), particleZ,
							particleX - this.getX(), 0, particleZ - this.getZ());
				}
			}

			// Other landing effects
			if(distance > 1.2){
				level.playLocalSound(getX(), getY(), getZ(), WizardrySounds.ENTITY_BOULDER_LAND, SoundSource.BLOCKS, (float) Math.min(2, distance / 4), 1, false);
				shakeNearbyPlayers();
//				EntityUtils.getEntitiesWithinRadius(Math.min(12, distance * 2), getX(), getY(), getZ(), world, EntityPlayer.class)
//						.forEach(p -> Wizardry.proxy.shakeScreen(p, Math.min(12, distance * 2)));
			}
		}
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt){
		super.readAdditionalSaveData(nbt);
		velX = nbt.getDouble("velX");
		velZ = nbt.getDouble("velZ");
		hitsRemaining = nbt.getInt("hitsRemaining");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt){
		super.addAdditionalSaveData(nbt);
		nbt.putDouble("velX", velX);
		nbt.putDouble("velZ", velZ);
		nbt.putInt("hitsRemaining", hitsRemaining);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		super.writeSpawnData(data);
		data.writeDouble(velX);
		data.writeDouble(velZ);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){
		super.readSpawnData(data);
		this.velX = data.readDouble();
		this.velZ = data.readDouble();
	}

}
