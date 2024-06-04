package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
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

public class ConjureBlock extends SpellRay {
	
	private static final String BLOCK_LIFETIME = "block_lifetime";

	public ConjureBlock(){
		super("conjure_block", SpellActions.POINT, false);
		this.ignoreLivingEntities(true);
		addProperties(BLOCK_LIFETIME);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(caster != null && caster.isSneaking() && world.getBlockState(pos).getBlock() == WizardryBlocks.spectral_block){

			if(!level.isClientSide){
				// Dispelling of blocks
				world.setBlockToAir(pos);
			}else{
				ParticleBuilder.create(Type.FLASH).pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).scale(3)
				.clr(0.75f, 1, 0.85f).spawn(world);
			}
			
			return true;
		}
		
		pos = pos.offset(side);
		
		if(level.isClientSide){
			ParticleBuilder.create(Type.FLASH).pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).scale(3)
			.clr(0.75f, 1, 0.85f).spawn(world);
		}
		
		if(BlockUtils.canBlockBeReplaced(world, pos)){

			if(!level.isClientSide){
				
				world.setBlockState(pos, WizardryBlocks.spectral_block.getDefaultState());
				
				if(world.getTileEntity(pos) instanceof TileEntityTimer){
					((TileEntityTimer)world.getTileEntity(pos)).setLifetime((int)(getProperty(BLOCK_LIFETIME).floatValue()
							* modifiers.get(WizardryItems.duration_upgrade)));
				}
			}

			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
