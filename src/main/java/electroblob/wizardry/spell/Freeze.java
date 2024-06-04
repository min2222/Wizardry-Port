package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EntityBlaze;
import net.minecraft.world.entity.monster.EntityMagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class Freeze extends SpellRay {

	public Freeze(){
		super("freeze", SpellActions.POINT, false);
		this.soundValues(1, 1.4f, 0.4f);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		this.hitLiquids(true);
		this.ignoreUncollidables(false);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){

			if(target instanceof EntityBlaze || target instanceof EntityMagmaCube){
				target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			}

			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote && caster instanceof Player) ((Player)caster).sendStatusMessage(
						Component.translatable("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				((LivingEntity)target).addPotionEffect(new MobEffectInstance(WizardryPotions.frost,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue()));
			}

			if(target.isBurning()) target.extinguish();

			return true;
		}
		
		return false; // If the spell hit a non-living entity
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote && BlockUtils.canPlaceBlock(caster, world, pos)){
			BlockUtils.freeze(world, pos, true);
		}
		
		return true; // Always succeeds if it hits a block
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = 0.5f + (world.rand.nextFloat() / 2);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8))
		.clr(brightness, brightness + 0.1f, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).spawn(world);
	}

}
