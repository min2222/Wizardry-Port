package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class Enrage extends SpellAreaEffect {

	public Enrage(){
		super("enrage", SpellActions.SUMMON, false);
		this.alwaysSucceed(true);
	}

	@Override
	public boolean canBeCastBy(DispenserBlockEntity dispenser){
		return false;
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(caster != null && target instanceof Mob){
			target.setLastHurtByMob(caster); // Yours truly, angry mobs
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){

		if(caster != null) origin = caster.getEyePosition(1);

		for(int i = 0; i < 30; i++){
			double x = origin.x - 1 + world.random.nextDouble() * 2;
			double y = origin.y - 0.25 + world.random.nextDouble() * 0.5;
			double z = origin.z - 1 + world.random.nextDouble() * 2;
			ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.9f, 0.1f, 0).spawn(world);
		}
	}

}
