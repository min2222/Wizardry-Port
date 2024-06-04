package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityDecay extends EntityMagicConstruct {

	public int textureIndex;

	public EntityDecay(Level world){
		super(world);
		textureIndex = this.random.nextInt(10);
		this.getBbHeight() = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.random.nextInt(700) == 0 && this.ticksExisted + 100 < lifetime)
			this.playSound(WizardrySounds.ENTITY_DECAY_AMBIENT, 0.2F + random.nextFloat() * 0.2F,
					0.6F + random.nextFloat() * 0.15F);

		if(!this.level.isClientSide){
			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(this.width/2f, this.getX(), this.getY(),
					this.getZ(), this.getBbHeight(), this.world);
			for(LivingEntity target : targets){
				if(target != this.getCaster()){
					// If this check wasn't here the potion would be reapplied every tick and hence the entity would be
					// damaged each tick.
					// In this case, we do want particles to be shown.
					if(!target.isPotionActive(WizardryPotions.decay))
						target.addEffect(new MobEffectInstance(WizardryPotions.decay,
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
			.spawn(world);
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}
	
}
