package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityIceCharge extends EntityBomb {

	public static final String ICE_SHARDS = "ice_shards";

	public EntityIceCharge(Level world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(HitResult rayTrace){

		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){
			// This is if the ice charge gets a direct hit
			float damage = Spells.ice_charge.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FROST).setProjectile(),
					damage);

			if(entityHit instanceof LivingEntity && !MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
				((LivingEntity)entityHit).addEffect(new MobEffectInstance(WizardryPotions.frost,
						Spells.ice_charge.getProperty(Spell.DIRECT_EFFECT_DURATION).intValue(),
						Spells.ice_charge.getProperty(Spell.DIRECT_EFFECT_STRENGTH).intValue()));
		}

		// Particle effect
		if(level.isClientSide){
			this.world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			for(int i = 0; i < 30 * blastMultiplier; i++){

				ParticleBuilder.create(Type.ICE, rand, this.getX(), this.getY(), this.getZ(), 2 * blastMultiplier, false)
				.time(35).gravity(true).spawn(world);

				float brightness = 0.4f + random.nextFloat() * 0.5f;
				ParticleBuilder.create(Type.DARK_MAGIC, rand, this.getX(), this.getY(), this.getZ(), 2 * blastMultiplier, false)
				.clr(brightness, brightness + 0.1f, 1.0f).spawn(world);
			}
		}

		if(!this.level.isClientSide){

			this.playSound(WizardrySounds.ENTITY_ICE_CHARGE_SMASH, 1.5f, random.nextFloat() * 0.4f + 0.6f);
			this.playSound(WizardrySounds.ENTITY_ICE_CHARGE_ICE, 1.2f, random.nextFloat() * 0.4f + 1.2f);

			double radius = Spells.ice_charge.getProperty(Spell.EFFECT_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, this.getX(), this.getY(),
					this.getZ(), this.world);

			// Slows targets
			for(LivingEntity target : targets){
				if(target != entityHit && target != this.getThrower()){
					if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
						target.addEffect(new MobEffectInstance(WizardryPotions.frost,
								Spells.ice_charge.getProperty(Spell.SPLASH_EFFECT_DURATION).intValue(),
								Spells.ice_charge.getProperty(Spell.SPLASH_EFFECT_STRENGTH).intValue()));
				}
			}

			// Places snow and ice on ground.
			for(int i = -1; i < 2; i++){
				for(int j = -1; j < 2; j++){

					BlockPos pos = new BlockPos(this.getX() + i, this.getY(), this.getZ() + j);

					Integer y = BlockUtils.getNearestSurface(world, pos, Direction.UP, 7, true,
							BlockUtils.SurfaceCriteria.SOLID_LIQUID_TO_AIR);

					if(y != null){

						pos = new BlockPos(pos.getX(), y, pos.getZ());

						double dist = this.getDistance(pos.getX(), pos.getY(), pos.getZ());

						// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
						if(random.nextInt((int)dist * 2 + 1) < 1 && dist < 2){
							if(level.getBlockState(pos.down()).getBlock() == Blocks.WATER){
								level.setBlockAndUpdate(pos.down(), Blocks.ICE.defaultBlockState());
							}else{
								// Don't need to check whether the block at pos can be replaced since getNearestFloorLevelB
								// only ever returns floors with air above them.
								level.setBlockAndUpdate(pos, Blocks.SNOW_LAYER.defaultBlockState());
							}
						}
					}
				}
			}

			// Releases shards
			for(int i = 0; i < Spells.ice_charge.getProperty(ICE_SHARDS).intValue(); i++){
				double dx = random.nextDouble() - 0.5;
				double dy = random.nextDouble() - 0.5;
				double dz = random.nextDouble() - 0.5;
				EntityIceShard iceshard = new EntityIceShard(world);
				iceshard.setPosition(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
				iceshard.motionX = dx * 1.5;
				iceshard.motionY = dy * 1.5;
				iceshard.motionZ = dz * 1.5;
				iceshard.setCaster(this.getThrower());
				iceshard.damageMultiplier = this.damageMultiplier;
				world.addFreshEntity(iceshard);
			}

			this.discard();
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}
}
