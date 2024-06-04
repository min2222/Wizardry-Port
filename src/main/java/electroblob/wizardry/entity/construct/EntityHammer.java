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
		return this.getEntityBoundingBox();
	}

	@Override
	public void applyEntityCollision(Entity entity){
		super.applyEntityCollision(entity);
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

//		if(this.ticksExisted % 20 == 1 && !this.onGround && world.isRemote){
//			// Though this sound does repeat, it stops when it hits the ground.
//			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_HAMMER_FALLING, WizardrySounds.SPELLS, 3.0f, 1.0f, false);
//		}

		if(this.world.isRemote && this.ticksExisted % 3 == 0){
			ParticleBuilder.create(Type.SPARK)
					.pos(this.posX - 0.5d + rand.nextDouble(), this.posY + 2 * rand.nextDouble(), this.posZ - 0.5d + rand.nextDouble())
					.spawn(world);
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
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

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(seekerRange, this.posX,
					this.posY + 1, this.posZ, world);

			int maxTargets = Spells.lightning_hammer.getProperty(LightningHammer.SECONDARY_MAX_TARGETS).intValue();
			while(targets.size() > maxTargets) targets.remove(targets.size() - 1);

			for(LivingEntity target : targets){

				if(EntityUtils.isLiving(target) && this.isValidTarget(target)
						&& target.ticksExisted % Spells.lightning_hammer.getProperty(LightningHammer.ATTACK_INTERVAL).floatValue() == 0){

					if(world.isRemote){

						ParticleBuilder.create(Type.LIGHTNING).pos(posX, posY + height - 0.1, posZ) .target(target).spawn(world);

						ParticleBuilder.spawnShockParticles(world, target.posX,
								target.posY + target.height, target.posZ);
					}

					target.playSound(WizardrySounds.ENTITY_HAMMER_ATTACK, 1.0F, rand.nextFloat() * 0.4F + 1.5F);

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

			List<Entity> collided = world.getEntitiesInAABBexcluding(this, this.getCollisionBoundingBox(), e -> e instanceof LivingEntity);

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

		if(this.world.isRemote){
			this.world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}

		super.despawn();
	}

	@Override
	public void fall(float distance, float damageMultiplier){

		if(world.isRemote){

			for(int i = 0; i < 40; i++){
				double particleX = this.posX - 1.0d + 2 * rand.nextDouble();
				double particleZ = this.posZ - 1.0d + 2 * rand.nextDouble();
				// Roundabout way of getting a block instance for the block the hammer is standing on (if any).
				BlockState block = world.getBlockState(new BlockPos(this.posX, this.posY - 2, this.posZ));

				if(block != null){
					world.spawnParticle(ParticleTypes.BLOCK_DUST, particleX, this.posY, particleZ,
							particleX - this.posX, 0, particleZ - this.posZ, Block.getStateId(block));
				}
			}

			if(this.fallDistance > 10){
				EntityUtils.getEntitiesWithinRadius(10, posX, posY, posZ, world, Player.class)
						.forEach(p -> Wizardry.proxy.shakeScreen(p, 6));
			}

		}else{
			// Just to check the hammer has actually fallen from the sky, rather than the block under it being broken.
			if(this.fallDistance > 10){
				EntityLightningBolt entitylightning = new EntityLightningBolt(world, this.posX, this.posY, this.posZ,
						false);
				world.addWeatherEffect(entitylightning);
			}

			this.playSound(WizardrySounds.ENTITY_HAMMER_LAND, 1.0F, 0.6f);
		}
	}

	@Override
	public boolean processInitialInteract(Player player, InteractionHand hand){

		if(player == this.getCaster() && ItemArtefact.isArtefactActive(player, WizardryItems.ring_hammer)
				&& player.getHeldItemMainhand().isEmpty() && ticksExisted > 10){

			this.setDead();

			ItemStack hammer = new ItemStack(WizardryItems.lightning_hammer);
			if(!hammer.hasTagCompound()) hammer.setTag(new CompoundTag());
			hammer.getTag().putInteger(ItemLightningHammer.DURATION_NBT_KEY, lifetime);
			hammer.setItemDamage(ticksExisted);
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
