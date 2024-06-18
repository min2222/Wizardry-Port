package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityBoulder;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class Boulder extends SpellConstruct<EntityBoulder> {

	public static final String SPEED = "speed";
	public static final String KNOCKBACK_STRENGTH = "knockback_strength";

	public Boulder(){
		super("boulder", SpellActions.SUMMON, EntityBoulder::new, false);
		addProperties(SPEED, DAMAGE, KNOCKBACK_STRENGTH);
	}

	@Override
	protected void addConstructExtras(EntityBoulder construct, Direction side, LivingEntity caster, SpellModifiers modifiers){
		float speed = getProperty(SPEED).floatValue();
		// Unlike tornado, boulder always has the same speed
		Vec3 direction = caster == null ? new Vec3(side.step()) : GeometryUtils.horizontalise(caster.getLookAngle());
		construct.setHorizontalVelocity(direction.x * speed, direction.z * speed);
		construct.setYRot(caster == null ? side.toYRot() : caster.getYRot());
		double yOffset = caster == null ? 0 : 1.6;
		construct.setPos(construct.getX() + direction.x, construct.getY() + yOffset, construct.getZ() + direction.z);
	}

}
