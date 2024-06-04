package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class RayOfPurification extends SpellRay {

	/** The number by which this spell's damage is multiplied for undead entities. */
	public static final String UNDEAD_DAMAGE_MULTIPLIER = "undead_damage_multiplier";

	public RayOfPurification(){
		super("ray_of_purification", SpellActions.POINT, true);
		addProperties(DAMAGE, EFFECT_DURATION, UNDEAD_DAMAGE_MULTIPLIER);
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

		if(EntityUtils.isLiving(target)){

			if(MagicDamage.isEntityImmune(DamageType.RADIANT, target)){
				if(!world.isRemote && ticksInUse == 1 && caster instanceof Player) ((Player)caster)
				.sendStatusMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()), true);
			}else if (ticksInUse % 10 == 0) {

				float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
				// Fire
				if(((LivingEntity)target).isEntityUndead()){
					damage *= getProperty(UNDEAD_DAMAGE_MULTIPLIER).floatValue();
				}
				// Damage
				EntityUtils.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.RADIANT), damage);
				// Blindness
				((LivingEntity)target).addPotionEffect(new MobEffectInstance(MobEffects.BLINDNESS,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade))));
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
	protected void spawnParticleRay(Level world, Vec3 origin, Vec3 direction, LivingEntity caster, double distance){

		if(caster != null){
			ParticleBuilder.create(Type.BEAM).entity(caster).pos(origin.subtract(caster.getPositionVector()))
					.length(distance).clr(1, 0.6f + 0.3f * world.rand.nextFloat(), 0.2f)
					.scale(Mth.sin(caster.ticksExisted * 0.2f) * 0.1f + 1.4f).spawn(world);
		}else{
			ParticleBuilder.create(Type.BEAM).pos(origin).target(origin.add(direction.scale(distance)))
					.clr(1, 0.6f + 0.3f * world.rand.nextFloat(), 0.2f)
					.scale(Mth.sin(Wizardry.proxy.getThePlayer().ticksExisted * 0.2f) * 0.1f + 1.4f).spawn(world);
		}
	}
}
