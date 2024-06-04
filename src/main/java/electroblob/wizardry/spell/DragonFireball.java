package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EntityDragonFireball;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

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

		Vec3 look = caster.getLookVec();

		if(!world.isRemote){

			EntityDragonFireball fireball = new EntityDragonFireball(world, caster, 1, 1, 1);

			fireball.setPosition(caster.posX + look.x, caster.posY + look.y + 1.3, caster.posZ + look.z);

			double acceleration = getProperty(ACCELERATION).doubleValue() * modifiers.get(WizardryItems.range_upgrade);

			fireball.accelerationX = look.x * acceleration;
			fireball.accelerationY = look.y * acceleration;
			fireball.accelerationZ = look.z * acceleration;

			world.spawnEntity(fireball);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target,
                        SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){

				EntityDragonFireball fireball = new EntityDragonFireball(world, caster, 1, 1, 1);

				double dx = target.posX - caster.posX;
				double dy = target.posY + (double)(target.height / 2.0F)
						- (caster.posY + (double)(caster.height / 2.0F));
				double dz = target.posZ - caster.posZ;

				double acceleration = getProperty(ACCELERATION).doubleValue();

				fireball.accelerationX = dx / caster.getDistance(target) * acceleration;
				fireball.accelerationY = dy / caster.getDistance(target) * acceleration;
				fireball.accelerationZ = dz / caster.getDistance(target) * acceleration;

				fireball.setPosition(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);

				world.spawnEntity(fireball);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return true;
	}

}
