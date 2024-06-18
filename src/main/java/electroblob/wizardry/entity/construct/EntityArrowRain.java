package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.projectile.EntityConjuredArrow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class EntityArrowRain extends EntityScaledConstruct {

	public EntityArrowRain(Level world){
		super(world);
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

			arrow.motionX = Mth.cos((float)Math.toRadians(this.getYRot() + 90));
			arrow.motionY = -0.6;
			arrow.motionZ = Mth.sin((float)Math.toRadians(this.getYRot() + 90));

			arrow.setOwner(this.getCaster());
			arrow.setBaseDamage(7.0d * damageMultiplier);

			this.level.addFreshEntity(arrow);
		}
	}

}
