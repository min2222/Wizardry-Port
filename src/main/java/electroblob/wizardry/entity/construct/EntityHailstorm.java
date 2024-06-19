package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.spell.Spell;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityHailstorm extends EntityScaledConstruct {

	public EntityHailstorm(Level world){
		this(WizardryEntities.HAILSTORM.get(), world);
		setSize(Spells.hailstorm.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 5);
	}
	
	public EntityHailstorm(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(Spells.hailstorm.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 5);
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

			EntityIceShard iceshard = new EntityIceShard(level);
			iceshard.setPos(x, y, z);

			iceshard.setDeltaMovement(Mth.cos((float)Math.toRadians(this.getYRot() + 90)), -0.6, Mth.sin((float)Math.toRadians(this.getYRot() + 90)));

			iceshard.setCaster(this.getCaster());
			iceshard.damageMultiplier = this.damageMultiplier;

			this.level.addFreshEntity(iceshard);
		}
	}

}
