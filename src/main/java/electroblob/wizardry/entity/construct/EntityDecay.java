package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityDecay extends EntityMagicConstruct {

	public int textureIndex;

	public EntityDecay(Level world){
		this(WizardryEntities.DECAY.get(), world);
		textureIndex = this.random.nextInt(10);
	}
	
	public EntityDecay(EntityType<? extends EntityMagicConstruct> type, Level world){
		super(type, world);
		textureIndex = this.random.nextInt(10);
	}

	@Override
	public void tick(){

		super.tick();

		if(this.random.nextInt(700) == 0 && this.tickCount + 100 < lifetime)
			this.playSound(WizardrySounds.ENTITY_DECAY_AMBIENT, 0.2F + random.nextFloat() * 0.2F,
					0.6F + random.nextFloat() * 0.15F);

		if(!this.level.isClientSide){
			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(this.getBbWidth()/2f, this.getX(), this.getY(),
					this.getZ(), this.getBbHeight(), this.level);
			for(LivingEntity target : targets){
				if(target != this.getCaster()){
					// If this check wasn't here the potion would be reapplied every tick and hence the entity would be
					// damaged each tick.
					// In this case, we do want particles to be shown.
					if(!target.hasEffect(WizardryPotions.DECAY.get()))
						target.addEffect(new MobEffectInstance(WizardryPotions.DECAY.get(),
								Spells.decay.getProperty(Spell.EFFECT_DURATION).intValue(), 0));
				}
			}
			
		}else if(this.random.nextInt(15) == 0){
			
			double radius = random.nextDouble() * 0.8;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			float brightness = random.nextFloat() * 0.4f;
			
			ParticleBuilder.create(Type.DARK_MAGIC)
			.pos(this.getX() + radius * Mth.cos(angle), this.getY(), this.getZ() + radius * Mth.sin(angle))
			.clr(brightness, 0, brightness + 0.1f)
			.spawn(level);
		}
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance){
		return true;
	}
	
}
