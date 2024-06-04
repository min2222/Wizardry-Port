package electroblob.wizardry.block;

import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityMagicLight;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ITileEntityProvider;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

public class BlockMagicLight extends Block implements ITileEntityProvider {

	//private static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

	public BlockMagicLight(Material material){
		super(material);
		this.setLightLevel(1.0f);
		this.setBlockUnbreakable();
	}

	@Override
	public AABB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos){
		// The other two bounding box methods in Block aren't nullable, so this is the
		// only one that can return NULL_AABB.
		return NULL_AABB;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ){
		// Let the player dispel any lights if they have the lantern charm, not just the permanent ones because that would be annoying!
		if(player.getItemInHand(hand).getItem() instanceof ISpellCastingItem && ItemArtefact.isArtefactActive(player, WizardryItems.charm_light)){

			level.setBlockToAir(pos);
			return true;

		}else{
			return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
		}
	}

//	@Override
//	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
//		return AABB;
//	}

	@Override
	public boolean isCollidable(){
		// This method has nothing to do with entity movement, it's just for raytracing
		return true;
	}

	@Override
	public boolean addDestroyEffects(Level world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager){
		if(level.getBlockState(pos).getBlock() == this) return true; // No break particles!
		else return super.addDestroyEffects(world, pos, manager);
	}

	@Override
	public boolean hasTileEntity(BlockState state){
		return true;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int metadata){
		return new TileEntityMagicLight(600);
	}

	@Override
	public boolean isOpaqueCube(BlockState state){
		return false;
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(BlockState state){
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
