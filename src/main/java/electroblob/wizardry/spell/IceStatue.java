package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class IceStatue extends SpellRay {

	public IceStatue(){
		super("ice_statue", SpellActions.POINT, false);
		this.soundValues(1, 1.4f, 0.4f);
		addProperties(EFFECT_DURATION);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return createSoundsWithSuffixes("shoot", "freeze");
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof Mob && !level.isClientSide){
			// Unchecked cast is fine because the block is a static final field
			if(((BlockStatue)WizardryBlocks.ice_statue).convertToStatue((Mob)target,
					caster, (int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)))){

				//target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.random.nextFloat() * 0.4F + 0.8F);
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
		float brightness = 0.5f + world.random.nextFloat() * 0.5f;
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.random.nextInt(8))
		.clr(brightness, brightness + 0.1f, 1.0f).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).time(20 + world.random.nextInt(10)).spawn(world);
	}

}
