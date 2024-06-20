package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.List;

public class Cobwebs extends SpellRay {

	public Cobwebs(){
		super("cobwebs", SpellActions.POINT, false);
		this.ignoreLivingEntities(true);
		addProperties(EFFECT_RADIUS, DURATION);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		boolean flag = false;
		
		pos = pos.relative(side);

		int blastUpgradeCount = (int)((modifiers.get(WizardryItems.BLAST_UPGRADE.get()) - 1) / Constants.RANGE_INCREASE_PER_LEVEL + 0.5f);

		float radius = getProperty(EFFECT_RADIUS).floatValue() + 0.73f * blastUpgradeCount;

		List<BlockPos> sphere = BlockUtils.getBlockSphere(pos, radius * modifiers.get(WizardryItems.BLAST_UPGRADE.get()));

		for(BlockPos pos1 : sphere){

			if(world.isEmptyBlock(pos1)){
				if(!world.isClientSide){
					world.setBlockAndUpdate(pos1, WizardryBlocks.VANISHING_COBWEB.get().defaultBlockState());
					if(world.getBlockEntity(pos1) instanceof TileEntityTimer){
						((TileEntityTimer)world.getBlockEntity(pos1))
								.setLifetime((int)(getProperty(DURATION).doubleValue()
										* modifiers.get(WizardryItems.DURATION_UPGRADE.get())));
					}
				}
				flag = true;
			}
		}
		
		return flag;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
