package electroblob.wizardry.entity.construct;

import com.google.common.collect.Lists;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Boulder;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityBoulder extends EntityScaledConstruct {

	private double velX, velZ;
	private int hitsRemaining;
	private boolean soundStarted = false;

	public EntityBoulder(Level world){
		super(world);
		setSize(2.375f, 2.375f);
		this.noClip = false;
		this.setNoGravity(false);
		this.stepHeight = 1;
		hitsRemaining = 5;
	}

	public void setHorizontalVelocity(double velX, double velZ){
		this.velX = velX;
		this.velZ = velZ;
	}

	@Override
	public void onUpdate(){

		if(level.isClientSide && !soundStarted && onGround){
			soundStarted = true;
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_BOULDER_ROLL, WizardrySounds.SPELLS, 1, 1, true);
		}

		super.onUpdate();

		this.motionY -= 0.03999999910593033D; // Gravity

		this.move(MoverType.SELF, velX, motionY, velZ);

		// Entity damage
		List<LivingEntity> collided = world.getEntitiesWithinAABB(LivingEntity.class, this.getEntityBoundingBox());

		float damage = Spells.boulder.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
		float knockback = Spells.boulder.getProperty(Boulder.KNOCKBACK_STRENGTH).floatValue();

		for(LivingEntity entity : collided){

			if(!isValidTarget(entity)) break;

			boolean crushBonus = entity.getY() < this.getY()
					&& entity.getEntityBoundingBox().minX > this.getEntityBoundingBox().minX
					&& entity.getEntityBoundingBox().maxX < this.getEntityBoundingBox().maxX
					&& entity.getEntityBoundingBox().minZ > this.getEntityBoundingBox().minZ
					&& entity.getEntityBoundingBox().maxZ < this.getEntityBoundingBox().maxZ;

			if(EntityUtils.attackEntityWithoutKnockback(entity, MagicDamage.causeIndirectMagicDamage(this,
					getCaster(), DamageType.MAGIC), crushBonus ? damage * 1.5f : damage) && !crushBonus){
				// Only knock back if not crushing
				EntityUtils.applyStandardKnockback(this, entity, knockback);
				entity.motionX += this.motionX;
				entity.motionZ += this.motionZ;
			}
			entity.playSound(WizardrySounds.ENTITY_BOULDER_HIT, 1, 1);
		}

		// Wall smashing
		if(EntityUtils.canDamageBlocks(getCaster(), world) && collidedHorizontally){
			AABB box = getEntityBoundingBox().offset(velX, 0, velZ);
			List<BlockPos> cuboid = Lists.newArrayList(BlockPos.getAllInBox(Mth.floor(box.minX), Mth.floor(box.minY),
					Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ)));
			smashBlocks(cuboid, true);
		}

		// Trailing particles
		for(int i = 0; i < 10; i++){

			double particleX = this.getX() + width * 0.7 * (random.nextDouble() - 0.5);
			double particleZ = this.getZ() + width * 0.7 * (random.nextDouble() - 0.5);

			BlockState block = world.getBlockState(new BlockPos(this).down());

			if(block.getBlock() != Blocks.AIR){
				world.spawnParticle(ParticleTypes.BLOCK_DUST, particleX, this.getY(), particleZ,
						0, 0.2, 0, Block.getStateId(block));
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

		if(blocks.removeIf(p -> world.getBlockState(p).getBlock().getExplosionResistance(world, p, this, null) > 3
				|| (!level.isClientSide && !BlockUtils.canBreakBlock(getCaster(), world, p)))){
			// If any of the blocks were not breakable, the boulder is smashed
			if(breakIfTooHard){
				this.despawn();
			}else{
				return false;
			}

		}else{

			if(!level.isClientSide){
				blocks.forEach(p -> world.destroyBlock(p, false));
				if(--hitsRemaining <= 0) this.despawn();
			}else{
				world.playSound(getX(), getY(), getZ(), WizardrySounds.ENTITY_BOULDER_BREAK_BLOCK, SoundSource.BLOCKS, 1, 1, false);
			}
		}

		shakeNearbyPlayers();
		return true;
	}

	private void shakeNearbyPlayers(){
		EntityUtils.getEntitiesWithinRadius(10, getX(), getY(), getZ(), world, Player.class)
				.forEach(p -> Wizardry.proxy.shakeScreen(p, 8));
	}

	@Override
	public void despawn(){

		if(level.isClientSide){

			for(int i = 0; i < 200; i++){
				double x = getX() + (random.nextDouble() - 0.5) * width;
				double y = getY() + random.nextDouble() * height;
				double z = getZ() + (random.nextDouble() - 0.5) * width;
				world.spawnParticle(ParticleTypes.BLOCK_DUST, x, y, z, (x - getX()) * 0.1,
						(y - getY() + height / 2) * 0.1, (z - getZ()) * 0.1, Block.getStateId(Blocks.DIRT.getDefaultState()));
			}

			world.playSound(getX(), getY(), getZ(), WizardrySounds.ENTITY_BOULDER_BREAK_BLOCK, SoundSource.BLOCKS, 1, 1, false);
		}

		super.despawn();
	}

	@Override
	public void move(MoverType type, double x, double y, double z){
		super.move(type, x, y, z);
		this.rotationPitch += Math.toDegrees(Math.sqrt(x*x + y*y + z*z) / (width/2)); // That's how we roll
	}

	@Override
	public void fall(float distance, float damageMultiplier){

		super.fall(distance, damageMultiplier);

		// Floor smashing
		if(EntityUtils.canDamageBlocks(getCaster(), world) && distance > 3){
			AABB box = getEntityBoundingBox().offset(velX, motionY, velZ);
			List<BlockPos> cuboid = Lists.newArrayList(BlockPos.getAllInBox(Mth.floor(box.minX), Mth.floor(box.minY),
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
				BlockState block = world.getBlockState(new BlockPos(this.getX(), this.getY() - 2, this.getZ()));

				if(block.getBlock() != Blocks.AIR){
					world.spawnParticle(ParticleTypes.BLOCK_DUST, particleX, this.getY(), particleZ,
							particleX - this.getX(), 0, particleZ - this.getZ(), Block.getStateId(block));
				}
			}

			// Other landing effects
			if(distance > 1.2){
				world.playSound(getX(), getY(), getZ(), WizardrySounds.ENTITY_BOULDER_LAND, SoundSource.BLOCKS, Math.min(2, distance / 4), 1, false);
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
	public AABB getCollisionBoundingBox(){
		return this.getEntityBoundingBox();
	}

	@Override
	protected void readEntityFromNBT(CompoundTag nbt){
		super.readEntityFromNBT(nbt);
		velX = nbt.getDouble("velX");
		velZ = nbt.getDouble("velZ");
		hitsRemaining = nbt.getInt("hitsRemaining");
	}

	@Override
	protected void writeEntityToNBT(CompoundTag nbt){
		super.writeEntityToNBT(nbt);
		nbt.setDouble("velX", velX);
		nbt.setDouble("velZ", velZ);
		nbt.putInt("hitsRemaining", hitsRemaining);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeDouble(velX);
		data.writeDouble(velZ);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		this.velX = data.readDouble();
		this.velZ = data.readDouble();
	}

}
