package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class Poison extends SpellRay {

	public Poison(){
		super("poison", SpellActions.POINT, false);
		this.soundValues(1, 1.1f, 0.2f);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return true;
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			
			// Has no effect on undead or spiders.
			if(MagicDamage.isEntityImmune(DamageType.POISON, target)){
				if(!world.isClientSide && caster instanceof Player) ((Player)caster).sendStatusMessage(
						Component.translatable("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.POISON),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
				((LivingEntity)target).addEffect(new MobEffectInstance(MobEffects.POISON,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
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
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.3f, 0.7f, 0).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.random.nextInt(8)).clr(0.1f, 0.4f, 0).spawn(world);
	}

}
