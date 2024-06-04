package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class EntityHailstorm extends EntityScaledConstruct {

	public EntityHailstorm(Level world){
		super(world);
		setSize(Spells.hailstorm.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 5);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void tick(){

		super.tick();

		if(!this.level.isClientSide){

			double x = getX() + (world.random.nextDouble() - 0.5D) * (double)width;
			double y = getY() + world.random.nextDouble() * (double)height;
			double z = getZ() + (world.random.nextDouble() - 0.5D) * (double)width;

			EntityIceShard iceshard = new EntityIceShard(world);
			iceshard.setPosition(x, y, z);

			iceshard.motionX = Mth.cos((float)Math.toRadians(this.rotationYaw + 90));
			iceshard.motionY = -0.6;
			iceshard.motionZ = Mth.sin((float)Math.toRadians(this.rotationYaw + 90));

			iceshard.setCaster(this.getCaster());
			iceshard.damageMultiplier = this.damageMultiplier;

			this.world.addFreshEntity(iceshard);
		}
	}

}
