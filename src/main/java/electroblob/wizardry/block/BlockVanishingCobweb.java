package electroblob.wizardry.block;

import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class BlockVanishingCobweb extends Block implements ITileEntityProvider {

	public BlockVanishingCobweb(Material material){
		super(material);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public EnumBlockRenderType getRenderType(BlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(BlockState state){
		return false;
	}

	@Override
	public AABB getCollisionBoundingBox(BlockState state, IBlockAccess world, BlockPos pos){
		return NULL_AABB;
	}

	@Override
	public boolean isFullCube(BlockState state){
		return false;
	}

	@Override
	public boolean hasTileEntity(BlockState state){
		return true;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int metadata){
		return new TileEntityTimer(400);
	}

	@Override
	public void onEntityCollision(Level world, BlockPos pos, BlockState state, Entity entity){
		entity.setInWeb();
	}

	@Override
	public int quantityDropped(Random par1Random){
		return 0;
	}

}
