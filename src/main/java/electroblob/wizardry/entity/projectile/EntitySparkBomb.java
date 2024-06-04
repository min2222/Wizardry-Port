package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntitySparkBomb extends EntityBomb {

	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public EntitySparkBomb(Level world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(HitResult rayTrace){
		
		this.playSound(WizardrySounds.ENTITY_SPARK_BOMB_HIT_BLOCK, 0.5f, 0.5f);

		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){
			// This is if the spark bomb gets a direct hit
			float damage = Spells.spark_bomb.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

			this.playSound(WizardrySounds.ENTITY_SPARK_BOMB_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(),
					damage);

		}

		// Particle effect
		if(level.isClientSide){
			ParticleBuilder.spawnShockParticles(world, getX(), getY() + height/2, getZ());
		}

		double seekerRange = Spells.spark_bomb.getProperty(Spell.EFFECT_RADIUS).doubleValue() * blastMultiplier;

		List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(seekerRange, this.getX(), this.getY(),
				this.getZ(), this.world);

		for(int i = 0; i < Math.min(targets.size(), Spells.spark_bomb.getProperty(SECONDARY_MAX_TARGETS).intValue()); i++){

			boolean flag = targets.get(i) != entityHit && targets.get(i) != this.getThrower()
					&& !(targets.get(i) instanceof Player
							&& ((Player)targets.get(i)).isCreative());

			// Detects (client side) if target is the thrower, to stop particles being spawned around them.
			//if(flag && level.isClientSide && targets.get(i).getEntityId() == this.playerID) flag = false;

			if(flag){

				LivingEntity target = targets.get(i);

				if(!this.level.isClientSide){

					target.playSound(WizardrySounds.ENTITY_SPARK_BOMB_CHAIN, 1.0F, random.nextFloat() * 0.4F + 1.5F);

					target.hurt(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
							Spells.spark_bomb.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);

				}else{
					ParticleBuilder.create(Type.LIGHTNING).pos(this.getPositionVector()).target(target).spawn(world);
					ParticleBuilder.spawnShockParticles(world, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ());
				}
			}
		}

		this.discard();
	}
}
