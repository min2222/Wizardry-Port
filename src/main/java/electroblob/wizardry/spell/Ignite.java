package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class Ignite extends SpellRay {
	
	public Ignite(){
		super("ignite", SpellActions.POINT, false);
		this.soundValues(1, 1, 0.4f);
		addProperties(BURN_DURATION);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		// Fire can damage armour stands, so this includes them
		if(target instanceof LivingEntity) {
			
			if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
				if(!level.isClientSide && caster instanceof Player) ((Player)caster).sendStatusMessage(
						Component.translatable("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				target.setFire((int)(getProperty(BURN_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		pos = pos.offset(side);
		
		if(world.isAirBlock(pos)){
			
			if(!level.isClientSide && BlockUtils.canPlaceBlock(caster, world, pos)){
				world.setBlockState(pos, Blocks.FIRE.getDefaultState());
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
	}

}
