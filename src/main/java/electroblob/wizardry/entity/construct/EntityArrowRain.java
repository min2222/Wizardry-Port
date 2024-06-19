package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.projectile.EntityConjuredArrow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.spell.Spell;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityArrowRain extends EntityScaledConstruct {

	public EntityArrowRain(Level world){
		this(WizardryEntities.ARROW_RAIN.get(), world);
		setSize(Spells.arrow_rain.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 5);
	}
	
	public EntityArrowRain(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(Spells.arrow_rain.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 5);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void tick(){

		super.tick();

		if(!this.level.isClientSide){

			double x = getX() + (level.random.nextDouble() - 0.5D) * (double)getBbWidth();
			double y = getY() + level.random.nextDouble() * (double)getBbHeight();
			double z = getZ() + (level.random.nextDouble() - 0.5D) * (double)getBbWidth();

			EntityConjuredArrow arrow = new EntityConjuredArrow(level, x, y, z);

			arrow.setDeltaMovement(Mth.cos((float)Math.toRadians(this.getYRot() + 90)), -0.6, Mth.sin((float)Math.toRadians(this.getYRot() + 90)));

			arrow.setOwner(this.getCaster());
			arrow.setBaseDamage(7.0d * damageMultiplier);

			this.level.addFreshEntity(arrow);
		}
	}

}
