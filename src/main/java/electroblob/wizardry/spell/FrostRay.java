package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EntityBlaze;
import net.minecraft.world.entity.monster.EntityMagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class FrostRay extends SpellRay {

	public FrostRay(){
		super("frost_ray", SpellActions.POINT, true);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
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

			if(target.isBurning()) target.extinguish();

			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isClientSide && ticksInUse == 1 && caster instanceof Player) ((Player)caster)
				.sendStatusMessage(Component.translatable("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()), true);
			// This now only damages in line with the maxHurtResistantTime. Some mods don't play nicely and fiddle
			// with this mechanic for their own purposes, so this line makes sure that doesn't affect wizardry.
			}else{
				// For frost ray the entity can move slightly, unlike freeze
				((LivingEntity)target).addEffect(new MobEffectInstance(WizardryPotions.frost,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue()));

				if(ticksInUse % 10 == 0){
					float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
					if(target instanceof EntityBlaze || target instanceof EntityMagmaCube) damage *= 2;

					EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster,
							DamageType.FROST), damage);
				}
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
		float brightness = world.random.nextFloat();
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.random.nextInt(12))
		.clr(0.4f + 0.6f * brightness, 0.6f + 0.4f*brightness, 1).collide(true).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).vel(vx, vy, vz).time(8 + world.random.nextInt(12)).collide(true).spawn(world);
	}

}
