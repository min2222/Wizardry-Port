package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
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
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityIceCharge extends EntityBomb {

	public static final String ICE_SHARDS = "ice_shards";

	public EntityIceCharge(Level world){
		this(WizardryEntities.ICE_CHARGE.get(), world);
	}
	
	public EntityIceCharge(EntityType<? extends EntityBomb> type, Level world){
		super(type, world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onHit(HitResult rayTrace){

		Entity entityHit = rayTrace.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) rayTrace).getEntity() : null;

		if(entityHit != null){
			// This is if the ice charge gets a direct hit
			float damage = Spells.ICE_CHARGE.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.FROST).setProjectile(),
					damage);

			if(entityHit instanceof LivingEntity && !MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
				((LivingEntity)entityHit).addEffect(new MobEffectInstance(WizardryPotions.FROST.get(),
						Spells.ICE_CHARGE.getProperty(Spell.DIRECT_EFFECT_DURATION).intValue(),
						Spells.ICE_CHARGE.getProperty(Spell.DIRECT_EFFECT_STRENGTH).intValue()));
		}

		// Particle effect
		if(level.isClientSide){
			this.level.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			for(int i = 0; i < 30 * blastMultiplier; i++){

				ParticleBuilder.create(Type.ICE, random, this.getX(), this.getY(), this.getZ(), 2 * blastMultiplier, false)
				.time(35).gravity(true).spawn(level);

				float brightness = 0.4f + random.nextFloat() * 0.5f;
				ParticleBuilder.create(Type.DARK_MAGIC, random, this.getX(), this.getY(), this.getZ(), 2 * blastMultiplier, false)
				.clr(brightness, brightness + 0.1f, 1.0f).spawn(level);
			}
		}

		if(!this.level.isClientSide){

			this.playSound(WizardrySounds.ENTITY_ICE_CHARGE_SMASH, 1.5f, random.nextFloat() * 0.4f + 0.6f);
			this.playSound(WizardrySounds.ENTITY_ICE_CHARGE_ICE, 1.2f, random.nextFloat() * 0.4f + 1.2f);

			double radius = Spells.ICE_CHARGE.getProperty(Spell.EFFECT_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, this.getX(), this.getY(),
					this.getZ(), this.level);

			// Slows targets
			for(LivingEntity target : targets){
				if(target != entityHit && target != this.getOwner()){
					if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
						target.addEffect(new MobEffectInstance(WizardryPotions.FROST.get(),
								Spells.ICE_CHARGE.getProperty(Spell.SPLASH_EFFECT_DURATION).intValue(),
								Spells.ICE_CHARGE.getProperty(Spell.SPLASH_EFFECT_STRENGTH).intValue()));
				}
			}

			// Places snow and ice on ground.
			for(int i = -1; i < 2; i++){
				for(int j = -1; j < 2; j++){

					BlockPos pos = new BlockPos(this.getX() + i, this.getY(), this.getZ() + j);

					Integer y = BlockUtils.getNearestSurface(level, pos, Direction.UP, 7, true,
							BlockUtils.SurfaceCriteria.SOLID_LIQUID_TO_AIR);

					if(y != null){

						pos = new BlockPos(pos.getX(), y, pos.getZ());

						double dist = this.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());

						// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
						if(random.nextInt((int)dist * 2 + 1) < 1 && dist < 2){
							if(level.getBlockState(pos.below()).getBlock() == Blocks.WATER){
								level.setBlockAndUpdate(pos.below(), Blocks.ICE.defaultBlockState());
							}else{
								// Don't need to check whether the block at pos can be replaced since getNearestFloorLevelB
								// only ever returns floors with air above them.
								level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
							}
						}
					}
				}
			}

			// Releases shards
			for(int i = 0; i < Spells.ICE_CHARGE.getProperty(ICE_SHARDS).intValue(); i++){
				double dx = random.nextDouble() - 0.5;
				double dy = random.nextDouble() - 0.5;
				double dz = random.nextDouble() - 0.5;
				EntityIceShard iceshard = new EntityIceShard(level);
				iceshard.setPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
				iceshard.setDeltaMovement(dx * 1.5, dy * 1.5, dz * 1.5);
				iceshard.setCaster((LivingEntity) this.getOwner());
				iceshard.damageMultiplier = this.damageMultiplier;
				level.addFreshEntity(iceshard);
			}

			this.discard();
		}
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}
}
