package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DragonFireball extends Spell {

	public static final String ACCELERATION = "acceleration";

	public DragonFireball(){
		super("dragon_fireball", UseAnim.NONE, false);
		addProperties(ACCELERATION);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3 look = caster.getLookAngle();

		if(!world.isClientSide){

			net.minecraft.world.entity.projectile.DragonFireball fireball = new net.minecraft.world.entity.projectile.DragonFireball(world, caster, 1, 1, 1);

			fireball.setPos(caster.getX() + look.x, caster.getY() + look.y + 1.3, caster.getZ() + look.z);

			double acceleration = getProperty(ACCELERATION).doubleValue() * modifiers.get(WizardryItems.range_upgrade);

			fireball.xPower = look.x * acceleration;
			fireball.yPower = look.y * acceleration;
			fireball.zPower  = look.z * acceleration;

			world.addFreshEntity(fireball);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		caster.swing(hand);
		return true;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target,
                        SpellModifiers modifiers){

		if(target != null){

			if(!world.isClientSide){

				net.minecraft.world.entity.projectile.DragonFireball fireball = new net.minecraft.world.entity.projectile.DragonFireball(world, caster, 1, 1, 1);

				double dx = target.getX() - caster.getX();
				double dy = target.getY() + (double)(target.getBbHeight() / 2.0F)
						- (caster.getY() + (double)(caster.getBbHeight() / 2.0F));
				double dz = target.getZ() - caster.getZ();

				double acceleration = getProperty(ACCELERATION).doubleValue();

				fireball.xPower = dx / caster.distanceTo(target) * acceleration;
				fireball.yPower = dy / caster.distanceTo(target) * acceleration;
				fireball.zPower = dz / caster.distanceTo(target) * acceleration;

				fireball.setPos(caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ());

				world.addFreshEntity(fireball);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swing(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return true;
	}

}
