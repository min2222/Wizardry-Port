package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class CelestialSmite extends SpellRay {

	public CelestialSmite(){
		super("celestial_smite", SpellActions.POINT, false);
		addProperties(EFFECT_RADIUS, DAMAGE, BURN_DURATION);
		this.ignoreLivingEntities(true);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, @Nullable LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, @Nullable LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, hit.x, hit.y, hit.z, world);

		DamageSource source = caster == null ? DamageSource.MAGIC : MagicDamage.causeDirectMagicDamage(caster, DamageType.RADIANT);
		float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);

		for(LivingEntity target : targets){
			EntityUtils.attackEntityWithoutKnockback(target, source, damage);
			target.setSecondsOnFire(getProperty(BURN_DURATION).intValue());
		}

		if(world.isClientSide){

			ParticleBuilder.create(Type.BEAM).pos(hit.x, level.getActualHeight(), hit.z).target(hit).scale(8)
			.clr(0xffbf00).time(10).spawn(world);
			ParticleBuilder.create(Type.SPHERE).pos(hit).scale(4).clr(0xfff098).spawn(world);

			if(side == Direction.UP){
				Vec3 vec = hit.add(new Vec3(side.getDirectionVec()).scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET));
				ParticleBuilder.create(Type.SCORCH).pos(vec).face(side).scale(3).spawn(world);
			}
		}

		return true;
	}

	@Override
	protected boolean onMiss(Level world, @Nullable LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
}
