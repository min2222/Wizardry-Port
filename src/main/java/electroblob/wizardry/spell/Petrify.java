package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Petrify extends SpellRay {

	// This is more descriptive and more accurate than the standard "effect_duration" in this case
	public static final String MINIMUM_EFFECT_DURATION = "minimum_effect_duration";

	public Petrify(){
		super("petrify", SpellActions.POINT, false);
		this.soundValues(1, 1.1f, 0.2f);
		addProperties(MINIMUM_EFFECT_DURATION);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof Mob && !world.isClientSide){
			// Unchecked cast is fine because the block is a static final field
			if(((BlockStatue)WizardryBlocks.petrified_stone).convertToStatue((Mob)target,
					caster, (int)(getProperty(MINIMUM_EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)))){
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
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.random.nextInt(8)).clr(0.2f, 0.2f, 0.2f).spawn(world);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0.1f, 0.1f).spawn(world);
	}

}
