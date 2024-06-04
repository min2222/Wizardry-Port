package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemLightningHammer;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.LightningHammer;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.effect.EntityLightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityHammer extends EntityMagicConstruct {

	/** How long the hammer has been falling for. */
	public int fallTime;

	public boolean spin = false;

	public EntityHammer(Level world){
		super(world);
		this.setSize(1.0f, 1.9F);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.noClip = false;
	}

	@Override
	public boolean isBurning(){
		return false;
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public AABB getCollisionBoundingBox(){
		return this.getBoundingBox();
	}

	@Override
	public void applyEntityCollision(Entity entity){
		super.applyEntityCollision(entity);
	}

	@Override
	public void tick(){

		super.tick();

//		if(this.tickCount % 20 == 1 && !this.onGround && level.isClientSide){
//			// Though this sound does repeat, it stops when it hits the ground.
//			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_HAMMER_FALLING, WizardrySounds.SPELLS, 3.0f, 1.0f, false);
//		}

		if(this.level.isClientSide && this.tickCount % 3 == 0){
			ParticleBuilder.create(Type.SPARK)
					.pos(this.getX() - 0.5d + random.nextDouble(), this.getY() + 2 * random.nextDouble(), this.getZ() - 0.5d + random.nextDouble())
					.spawn(world);
		}

		this.prevgetX() = this.getX();
		this.prevgetY() = this.getY();
		this.prevgetZ() = this.getZ();
		++this.fallTime;
		this.motionY -= 0.03999999910593033D;
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if(this.onGround){

			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;

			this.rotationPitch = 0;
			this.spin = false;

			double seekerRange = Spells.lightning_hammer.getProperty(Spell.EFFECT_RADIUS).doubleValue();

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(seekerRange, this.getX(),
					this.getY() + 1, this.getZ(), world);

			int maxTargets = Spells.lightning_hammer.getProperty(LightningHammer.SECONDARY_MAX_TARGETS).intValue();
			while(targets.size() > maxTargets) targets.remove(targets.size() - 1);

			for(LivingEntity target : targets){

				if(EntityUtils.isLiving(target) && this.isValidTarget(target)
						&& target.tickCount % Spells.lightning_hammer.getProperty(LightningHammer.ATTACK_INTERVAL).floatValue() == 0){

					if(level.isClientSide){

						ParticleBuilder.create(Type.LIGHTNING).pos(getX(), getY() + height - 0.1, getZ()) .target(target).spawn(world);

						ParticleBuilder.spawnShockParticles(world, target.getX(),
								target.getY() + target.getBbHeight(), target.getZ());
					}

					target.playSound(WizardrySounds.ENTITY_HAMMER_ATTACK, 1.0F, random.nextFloat() * 0.4F + 1.5F);

					float damage = Spells.lightning_hammer.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier;

					if(this.getCaster() != null){
						EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(
								this, getCaster(), DamageType.SHOCK), damage);
						EntityUtils.applyStandardKnockback(this, target);
					}else{
						target.hurt(DamageSource.MAGIC, damage);
					}
				}
			}

		}else{

			if(spin) this.setRotation(this.rotationYaw, this.rotationPitch + 15);

			List<Entity> collided = level.getEntitiesInAABBexcluding(this, this.getCollisionBoundingBox(), e -> e instanceof LivingEntity);

			float damage = Spells.lightning_hammer.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

			for(Entity entity : collided){
				entity.hurt(MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.SHOCK), damage);
				//if(entity instanceof EntityLivingBase) ((EntityLivingBase)entity).knockBack(this, 2, -this.motionX, -this.motionZ);
			}
		}
	}

	@Override
	public void despawn(){

		this.playSound(WizardrySounds.ENTITY_HAMMER_EXPLODE, 1.0F, 1.0f);

		if(this.level.isClientSide){
			this.world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		super.despawn();
	}

	@Override
	public void fall(float distance, float damageMultiplier){

		if(level.isClientSide){

			for(int i = 0; i < 40; i++){
				double particleX = this.getX() - 1.0d + 2 * random.nextDouble();
				double particleZ = this.getZ() - 1.0d + 2 * random.nextDouble();
				// Roundabout way of getting a block instance for the block the hammer is standing on (if any).
				BlockState block = level.getBlockState(new BlockPos(this.getX(), this.getY() - 2, this.getZ()));

				if(block != null){
					world.spawnParticle(ParticleTypes.BLOCK_DUST, particleX, this.getY(), particleZ,
							particleX - this.getX(), 0, particleZ - this.getZ(), Block.getStateId(block));
				}
			}

			if(this.fallDistance > 10){
				EntityUtils.getEntitiesWithinRadius(10, getX(), getY(), getZ(), world, Player.class)
						.forEach(p -> Wizardry.proxy.shakeScreen(p, 6));
			}

		}else{
			// Just to check the hammer has actually fallen from the sky, rather than the block under it being broken.
			if(this.fallDistance > 10){
				EntityLightningBolt entitylightning = new EntityLightningBolt(world, this.getX(), this.getY(), this.getZ(),
						false);
				world.addWeatherEffect(entitylightning);
			}

			this.playSound(WizardrySounds.ENTITY_HAMMER_LAND, 1.0F, 0.6f);
		}
	}

	@Override
	public boolean processInitialInteract(Player player, InteractionHand hand){

		if(player == this.getCaster() && ItemArtefact.isArtefactActive(player, WizardryItems.ring_hammer)
				&& player.getMainHandItem().isEmpty() && tickCount > 10){

			this.discard();

			ItemStack hammer = new ItemStack(WizardryItems.lightning_hammer);
			if(!hammer.hasTagCompound()) hammer.setTag(new CompoundTag());
			hammer.getTag().putInt(ItemLightningHammer.DURATION_NBT_KEY, lifetime);
			hammer.setItemDamage(tickCount);
			hammer.getTag().putFloat(ItemLightningHammer.DAMAGE_MULTIPLIER_NBT_KEY, damageMultiplier);

			player.setHeldItem(InteractionHand.MAIN_HAND, hammer);
			return true;

		}else{
			return super.processInitialInteract(player, hand);
		}
	}

	@Override
	public void writeEntityToNBT(CompoundTag nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setByte("Time", (byte)this.fallTime);
		nbttagcompound.setBoolean("Spin", spin);
	}

	@Override
	public void readEntityFromNBT(CompoundTag nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		this.fallTime = nbttagcompound.getByte("Time") & 255;
		this.spin = nbttagcompound.getBoolean("Spin");
	}

	// Need to sync the caster so they don't have particles spawned at them

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeBoolean(spin);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		spin = data.readBoolean();
	}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}
}
