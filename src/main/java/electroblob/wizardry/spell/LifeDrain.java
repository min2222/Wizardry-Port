package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class LifeDrain extends SpellRay {

	public static final String HEAL_FACTOR = "heal_factor";

	public LifeDrain(){
		super("life_drain", SpellActions.POINT, true);
		this.particleVelocity(-0.5);
		this.particleSpacing(0.4);
		addProperties(DAMAGE, HEAL_FACTOR);
		this.soundValues(0.6f, 1, 0);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(Level world, LivingEntity entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){

			if(ticksInUse % 10 == 0){
				
				float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
				
				EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster,
						DamageType.MAGIC), damage);
				
				if(caster != null) caster.heal(damage * getProperty(HEAL_FACTOR).floatValue());
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
		return true;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		if(world.random.nextInt(5) == 0) ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0, 0).spawn(world);
		// This used to multiply the velocity by the distance from the caster
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.random.nextInt(6))
		.clr(0.5f, 0, 0).spawn(world);
	}

}
