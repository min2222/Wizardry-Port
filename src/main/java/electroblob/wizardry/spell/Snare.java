package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Snare extends SpellRay {

	public Snare(){
		super("snare", SpellActions.POINT, false);
		this.soundValues(1, 1.4f, 0.4f);
		this.ignoreLivingEntities(true);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(side == Direction.UP && world.isSideSolid(pos, Direction.UP) && BlockUtils.canBlockBeReplaced(world, pos.up())){
			if(!world.isClientSide){
				world.setBlockAndUpdate(pos.up(), WizardryBlocks.snare.defaultBlockState());
				((TileEntityPlayerSave)world.getTileEntity(pos.up())).setCaster(caster);
				((TileEntityPlayerSave)world.getTileEntity(pos.up())).sync();
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
		float brightness = world.random.nextFloat() * 0.25f;
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(20 + world.random.nextInt(8))
		.clr(brightness, brightness + 0.1f, 0).spawn(world);
		ParticleBuilder.create(Type.LEAF).pos(x, y, z).vel(0, -0.01, 0).time(40 + world.random.nextInt(10)).spawn(world);
	}

}
