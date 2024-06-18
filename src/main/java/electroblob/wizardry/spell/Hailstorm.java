package electroblob.wizardry.spell;

import javax.annotation.Nullable;

import electroblob.wizardry.entity.construct.EntityHailstorm;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Hailstorm extends SpellConstructRanged<EntityHailstorm> {

	public Hailstorm(){
		super("hailstorm", EntityHailstorm::new, false);
		this.floor(true);
		addProperties(EFFECT_RADIUS);
	}

	@Override
	protected boolean spawnConstruct(Level world, double x, double y, double z, Direction side, @Nullable LivingEntity caster, SpellModifiers modifiers){

		// Moves the entity back towards the caster a bit, so the area of effect is better centred on the position.
		// 3 is the distance to move the entity back towards the caster.
		double dx = caster == null ? side.step().x() : caster.getX() - x;
		double dz = caster == null ? side.step().z() : caster.getZ() - z;
		double dist = Math.sqrt(dx * dx + dz * dz);
		if(dist != 0){
			double distRatio = 3 / dist;
			x += dx * distRatio;
			z += dz * distRatio;
		}
		// Moves the entity up 5 blocks so that it is above mobs' heads.
		y += 5;

		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

	@Override
	protected void addConstructExtras(EntityHailstorm construct, Direction side, LivingEntity caster, SpellModifiers modifiers){
		// Makes the arrows shoot in the direction the caster was looking when they cast the spell.
		if(caster != null){
			construct.setYRot(caster.yHeadRot);
		}else{
			construct.setYRot(side.toYRot());
		}
	}

}
