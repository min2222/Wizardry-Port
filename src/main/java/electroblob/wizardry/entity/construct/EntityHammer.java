package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemLightningHammer;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.LightningHammer;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityHammer extends EntityMagicConstruct {

	/** How long the hammer has been falling for. */
	public int fallTime;

	public boolean spin = false;

	public EntityHammer(Level world){
		this(WizardryEntities.LIGHTNING_HAMMER.get(), world);
		this.setDeltaMovement(Vec3.ZERO);
		this.noPhysics = false;
	}
	
	public EntityHammer(EntityType<? extends EntityMagicConstruct> type, Level world){
		super(type, world);
		this.setDeltaMovement(Vec3.ZERO);
		this.noPhysics = false;
	}

	@Override
	public boolean isOnFire(){
		return false;
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
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
					.spawn(level);
		}

		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		++this.fallTime;
		this.setDeltaMovement(this.getDeltaMovement().subtract(0, 0.03999999910593033D, 0));
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.9800000190734863D));

		if(this.onGround){

			this.setDeltaMovement(this.getDeltaMovement().multiply(0.699999988079071D, -0.5D, 0.699999988079071D));

			this.setXRot(0);
			this.spin = false;

			double seekerRange = Spells.lightning_hammer.getProperty(Spell.EFFECT_RADIUS).doubleValue();

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(seekerRange, this.getX(),
					this.getY() + 1, this.getZ(), level);

			int maxTargets = Spells.lightning_hammer.getProperty(LightningHammer.SECONDARY_MAX_TARGETS).intValue();
			while(targets.size() > maxTargets) targets.remove(targets.size() - 1);

			for(LivingEntity target : targets){

				if(EntityUtils.isLiving(target) && this.isValidTarget(target)
						&& target.tickCount % Spells.lightning_hammer.getProperty(LightningHammer.ATTACK_INTERVAL).floatValue() == 0){

					if(level.isClientSide){

						ParticleBuilder.create(Type.LIGHTNING).pos(getX(), getY() + getBbHeight() - 0.1, getZ()) .target(target).spawn(level);

						ParticleBuilder.spawnShockParticles(level, target.getX(),
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

			if(spin) this.setRot(this.getYRot(), this.getXRot() + 15);

			List<Entity> collided = level.getEntities(this, this.getBoundingBox(), e -> e instanceof LivingEntity);

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
			this.level.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		super.despawn();
	}
	
	@Override
	public void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos){

		if(level.isClientSide){

			for(int i = 0; i < 40; i++){
				double particleX = this.getX() - 1.0d + 2 * random.nextDouble();
				double particleZ = this.getZ() - 1.0d + 2 * random.nextDouble();
				// Roundabout way of getting a block instance for the block the hammer is standing on (if any).
				BlockState block = level.getBlockState(new BlockPos(this.getX(), this.getY() - 2, this.getZ()));

				if(block != null){
					level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block), particleX, this.getY(), particleZ,
							particleX - this.getX(), 0, particleZ - this.getZ());
				}
			}

			if(this.fallDistance > 10){
				EntityUtils.getEntitiesWithinRadius(10, getX(), getY(), getZ(), level, Player.class)
						.forEach(p -> Wizardry.proxy.shakeScreen(p, 6));
			}

		}else{
			// Just to check the hammer has actually fallen from the sky, rather than the block under it being broken.
			if(this.fallDistance > 10){
				LightningBolt entitylightning = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
				entitylightning.setPos(this.position());
				entitylightning.setVisualOnly(false);
				level.addFreshEntity(entitylightning);
			}

			this.playSound(WizardrySounds.ENTITY_HAMMER_LAND, 1.0F, 0.6f);
		}
		super.checkFallDamage(distance, onGround, state, pos);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand){

		if(player == this.getCaster() && ItemArtefact.isArtefactActive(player, WizardryItems.RING_HAMMER.get())
				&& player.getMainHandItem().isEmpty() && tickCount > 10){

			this.discard();

			ItemStack hammer = new ItemStack(WizardryItems.LIGHTNING_HAMMER.get());
			if(!hammer.hasTag()) hammer.setTag(new CompoundTag());
			hammer.getTag().putInt(ItemLightningHammer.DURATION_NBT_KEY, lifetime);
			hammer.setDamageValue(tickCount);
			hammer.getTag().putFloat(ItemLightningHammer.DAMAGE_MULTIPLIER_NBT_KEY, damageMultiplier);

			player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
			return InteractionResult.SUCCESS;

		}else{
			return super.interact(player, hand);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putByte("Time", (byte)this.fallTime);
		nbttagcompound.putBoolean("Spin", spin);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		this.fallTime = nbttagcompound.getByte("Time") & 255;
		this.spin = nbttagcompound.getBoolean("Spin");
	}

	// Need to sync the caster so they don't have particles spawned at them

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		super.writeSpawnData(data);
		data.writeBoolean(spin);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){
		super.readSpawnData(data);
		spin = data.readBoolean();
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance){
		return true;
	}
}
