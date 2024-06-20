package electroblob.wizardry.spell;

import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.BLAST_UPGRADE.get());

		List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, hit.x, hit.y, hit.z, world);

		DamageSource source = caster == null ? DamageSource.MAGIC : MagicDamage.causeDirectMagicDamage(caster, DamageType.RADIANT);
		float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);

		for(LivingEntity target : targets){
			EntityUtils.attackEntityWithoutKnockback(target, source, damage);
			target.setSecondsOnFire(getProperty(BURN_DURATION).intValue());
		}

		if(world.isClientSide){
			
			ParticleBuilder.create(Type.BEAM).pos(hit.x, (world.getMinBuildHeight() + world.dimensionType().logicalHeight()), hit.z).target(hit).scale(8)
			.clr(0xffbf00).time(10).spawn(world);
			ParticleBuilder.create(Type.SPHERE).pos(hit).scale(4).clr(0xfff098).spawn(world);

			if(side == Direction.UP){
				Vec3 vec = hit.add(new Vec3(side.step()).scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET));
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
