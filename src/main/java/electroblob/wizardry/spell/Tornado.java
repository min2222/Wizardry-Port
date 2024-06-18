package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityTornado;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class Tornado extends SpellConstruct<EntityTornado> {

	public static final String SPEED = "speed";
	public static final String UPWARD_ACCELERATION = "upward_acceleration";

	public Tornado(){
		super("tornado", SpellActions.POINT, EntityTornado::new, false);
		addProperties(EFFECT_RADIUS, SPEED, DAMAGE, UPWARD_ACCELERATION);
	}

	@Override
	protected void addConstructExtras(EntityTornado construct, Direction side, LivingEntity caster, SpellModifiers modifiers){
		float speed = getProperty(SPEED).floatValue();
		Vec3 direction = caster == null ? new Vec3(side.step()) : caster.getLookAngle();
		construct.setHorizontalVelocity(direction.x * speed, direction.z * speed);
	}

}
