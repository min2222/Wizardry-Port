package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntitySmokeBomb extends EntityBomb {

	public EntitySmokeBomb(Level world){
		this(WizardryEntities.SMOKE_BOMB.get(), world);
	}
	
	public EntitySmokeBomb(EntityType<? extends EntityBomb> type, Level world){
		super(type, world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onHit(HitResult rayTrace){

		// Particle effect
		if(level.isClientSide){
			
			ParticleBuilder.create(Type.FLASH).pos(this.position()).scale(5 * blastMultiplier).clr(0, 0, 0).spawn(level);
			
			this.level.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
						
			for(int i = 0; i < 60 * blastMultiplier; i++){

				float brightness = random.nextFloat() * 0.1f + 0.1f;
				ParticleBuilder.create(Type.CLOUD, random, getX(), getY(), getZ(), 2*blastMultiplier, false)
						.clr(brightness, brightness, brightness).time(80 + this.random.nextInt(12)).shaded(true).spawn(level);

				brightness = random.nextFloat() * 0.3f;
				ParticleBuilder.create(Type.DARK_MAGIC, random, getX(), getY(), getZ(), 2*blastMultiplier, false)
				.clr(brightness, brightness, brightness).spawn(level);
			}
		}

		if(!this.level.isClientSide){

			this.playSound(WizardrySounds.ENTITY_SMOKE_BOMB_SMASH, 1.5F, random.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_SMOKE_BOMB_SMOKE, 1.2F, 1.0f);

			double range = Spells.SMOKE_BOMB.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(range, this.getX(), this.getY(),
					this.getZ(), this.level);

			int duration = Spells.SMOKE_BOMB.getProperty(Spell.EFFECT_DURATION).intValue();

			for(LivingEntity target : targets){
				if(target != this.getOwner()){
					target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0));
				}
			}

			this.discard();
		}
	}
}
