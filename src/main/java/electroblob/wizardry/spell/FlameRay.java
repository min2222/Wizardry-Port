package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class FlameRay extends SpellRay {

	public FlameRay(){
		super("flame_ray", SpellActions.POINT, true);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
		addProperties(DAMAGE, BURN_DURATION);
		this.soundValues(2.5f, 1, 0);
	}

	// The following three methods serve as a good example of how to implement continuous spell sounds (hint: it's easy)

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
		// Fire can damage armour stands
		if(target instanceof LivingEntity){

			if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
				if(!world.isRemote && ticksInUse == 1 && caster instanceof Player) ((Player)caster)
				.sendStatusMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()), true);
			// This now only damages in line with the maxHurtResistantTime. Some mods don't play nicely and fiddle
			// with this mechanic for their own purposes, so this line makes sure that doesn't affect wizardry.
			}else if(ticksInUse % 10 == 0){
				target.setFire((int)(getProperty(BURN_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
				EntityUtils.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.FIRE),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
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
		ParticleBuilder.create(Type.MAGIC_FIRE).pos(x, y, z).vel(vx, vy, vz).collide(true).spawn(world);
		ParticleBuilder.create(Type.MAGIC_FIRE).pos(x, y, z).vel(vx, vy, vz).collide(true).spawn(world);
	}

}
