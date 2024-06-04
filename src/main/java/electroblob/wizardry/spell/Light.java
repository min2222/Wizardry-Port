package electroblob.wizardry.spell;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class Light extends Spell {

	public Light(){
		super("light", SpellActions.POINT, false);
		addProperties(RANGE, DURATION);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		HitResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, false);

		if(rayTrace != null && rayTrace.typeOfHit == HitResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);

			if(world.isEmptyBlock(pos)){

				if(!world.isClientSide){
					level.setBlockAndUpdate(pos, WizardryBlocks.magic_light.defaultBlockState());
					if(level.getTileEntity(pos) instanceof TileEntityTimer){
						int lifetime = ItemArtefact.isArtefactActive(caster, WizardryItems.charm_light) ? -1
								: (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
						((TileEntityTimer)level.getTileEntity(pos)).setLifetime(lifetime);
					}
				}

				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}else{

			int x = (int)(Math.floor(caster.getX()) + caster.getLookVec().x * range);
			int y = (int)(Math.floor(caster.getY()) + caster.eyeHeight + caster.getLookVec().y * range);
			int z = (int)(Math.floor(caster.getZ()) + caster.getLookVec().z * range);

			BlockPos pos = new BlockPos(x, y, z);

			if(world.isEmptyBlock(pos)){
				if(!world.isClientSide){
					level.setBlockAndUpdate(pos, WizardryBlocks.magic_light.defaultBlockState());
					if(level.getTileEntity(pos) instanceof TileEntityTimer){
						int lifetime = ItemArtefact.isArtefactActive(caster, WizardryItems.charm_light) ? -1
								: (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
						((TileEntityTimer)level.getTileEntity(pos)).setLifetime(lifetime);
					}
				}

				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}
		return false;
	}

}
