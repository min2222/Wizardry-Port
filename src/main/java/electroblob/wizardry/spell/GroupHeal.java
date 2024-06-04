package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class GroupHeal extends SpellAreaEffect {

	public GroupHeal(){
		super("group_heal", SpellActions.POINT_UP, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		this.targetAllies(true);
		addProperties(HEALTH);
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(target.getHealth() < target.getMaxHealth() && target.getHealth() > 0){

			Heal.heal(target, getProperty(HEALTH).floatValue() * modifiers.get(SpellModifiers.POTENCY));

			if(level.isClientSide) ParticleBuilder.spawnHealParticles(world, target);
			playSound(world, target, ticksInUse, -1, modifiers);
			return true;
		}

		return false; // Only succeeds if something was healed
	}

	@Override
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){
		// We're spawning particles above so don't bother with this method
	}

}
