package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntitySparkBomb extends EntityBomb {

	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public EntitySparkBomb(Level world){
		this(WizardryEntities.SPARK_BOMB.get(), world);
	}
	
	public EntitySparkBomb(EntityType<? extends EntityBomb> type, Level world){
		super(type, world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onHit(HitResult rayTrace){
		
		this.playSound(WizardrySounds.ENTITY_SPARK_BOMB_HIT_BLOCK, 0.5f, 0.5f);

		Entity entityHit = rayTrace.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) rayTrace).getEntity() : null;

		if(entityHit != null){
			// This is if the spark bomb gets a direct hit
			float damage = Spells.SPARK_BOMB.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

			this.playSound(WizardrySounds.ENTITY_SPARK_BOMB_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.SHOCK).setProjectile(),
					damage);

		}

		// Particle effect
		if(level.isClientSide){
			ParticleBuilder.spawnShockParticles(level, getX(), getY() + getBbHeight()/2, getZ());
		}

		double seekerRange = Spells.SPARK_BOMB.getProperty(Spell.EFFECT_RADIUS).doubleValue() * blastMultiplier;

		List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(seekerRange, this.getX(), this.getY(),
				this.getZ(), this.level);

		for(int i = 0; i < Math.min(targets.size(), Spells.SPARK_BOMB.getProperty(SECONDARY_MAX_TARGETS).intValue()); i++){

			boolean flag = targets.get(i) != entityHit && targets.get(i) != this.getOwner()
					&& !(targets.get(i) instanceof Player
							&& ((Player)targets.get(i)).isCreative());

			// Detects (client side) if target is the thrower, to stop particles being spawned around them.
			//if(flag && level.isClientSide && targets.get(i).getEntityId() == this.playerID) flag = false;

			if(flag){

				LivingEntity target = targets.get(i);

				if(!this.level.isClientSide){

					target.playSound(WizardrySounds.ENTITY_SPARK_BOMB_CHAIN, 1.0F, random.nextFloat() * 0.4F + 1.5F);

					target.hurt(
							MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.SHOCK),
							Spells.SPARK_BOMB.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);

				}else{
					ParticleBuilder.create(Type.LIGHTNING).pos(this.position()).target(target).spawn(level);
					ParticleBuilder.spawnShockParticles(level, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ());
				}
			}
		}

		this.discard();
	}
}
