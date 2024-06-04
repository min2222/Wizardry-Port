package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class GuardianBeam extends SpellRay {

	public static final String AIR_DEPLETION = "air_depletion";

	public GuardianBeam(){
		super("guardian_beam", SpellActions.POINT, true);
		addProperties(DAMAGE, AIR_DEPLETION);
	}

	@Override
	protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		if(ticksInUse % 50 == 1) super.playSound(world, x, y, z, ticksInUse, duration, modifiers, sounds);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target)){

			if(ticksInUse % 50 == 1){

				EntityUtils.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

				if(!((LivingEntity)target).canBreatheUnderwater() && !((LivingEntity)target).isPotionActive(MobEffects.WATER_BREATHING)){
					target.setAir(Math.max(-20, target.getAir() - getProperty(AIR_DEPLETION).intValue()));
				}
			}
			
			if(level.isClientSide){

				float t = (ticksInUse % 50) / 50f;
				float yellowness = t * t;
				int r = 64 + (int)(yellowness * 191.0F);
				int g = 32 + (int)(yellowness * 191.0F);
				int b = 128 - (int)(yellowness * 64.0F);

				if(ticksInUse % 3 == 0) ParticleBuilder.create(Type.GUARDIAN_BEAM).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target)
				.clr(r, g, b).spawn(world);

				Vec3 direction = GeometryUtils.getCentre(target).subtract(origin);
				Vec3 pos = origin.add(direction.scale(world.random.nextFloat()));
				ParticleBuilder.create(Type.MAGIC_BUBBLE, world.rand, pos.x, pos.y, pos.z, 0.15, false).spawn(world);
			}
		}

		return true;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false; // Only works on hit
	}

	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
	}
}
