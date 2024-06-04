package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class HealAlly extends SpellRay {

	public HealAlly(){
		super("heal_ally", SpellActions.POINT, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(HEALTH);
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return false;
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			
			LivingEntity entity = (LivingEntity)target;
			
			if(entity.getHealth() < entity.getMaxHealth() && entity.getHealth() > 0){
				
				entity.heal(getProperty(HEALTH).floatValue() * modifiers.get(SpellModifiers.POTENCY));

				if(level.isClientSide) ParticleBuilder.spawnHealParticles(world, entity);
				playSound(world, entity, ticksInUse, -1, modifiers);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
