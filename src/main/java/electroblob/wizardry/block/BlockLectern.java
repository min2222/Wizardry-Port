package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityLectern;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.level.block.BlockHorizontal;
import net.minecraft.world.level.block.ITileEntityProvider;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockFaceShape;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockLectern extends BlockHorizontal implements ITileEntityProvider {

	public BlockLectern(){
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH));
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setSoundType(SoundType.WOOD);
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer.Builder(this).add(FACING).build();
	}

	@Override
	public BlockState getStateFromMeta(int meta){
		Direction enumfacing = Direction.byIndex(meta);
		if(enumfacing.getAxis() == Direction.Axis.Y) enumfacing = Direction.NORTH;
		return this.defaultBlockState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(BlockState state){
		return state.getValue(FACING).getIndex();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void randomDisplayTick(BlockState state, Level world, BlockPos pos, Random rand){

		Player entityplayer = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5,
				pos.getZ() + 0.5, TileEntityLectern.BOOK_OPEN_DISTANCE, false);

		if(entityplayer != null){
			ParticleBuilder.create(Type.DUST).pos(pos.getX() + random.nextFloat(), pos.getY() + 1, pos.getZ() + random.nextFloat())
					.vel(0, 0.03, 0).clr(1, 1, 0.65f).fade(0.7f, 0, 1).shaded(false).spawn(world);
		}
	}

	@Override
	public boolean isOpaqueCube(BlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(BlockState state){
		return false;
	}

	@Override
	public boolean isFullBlock(BlockState state){
		return false;
	}

	@Override
	public boolean isNormalCube(BlockState state){
		return false;
	}

	@Override
	public boolean canPlaceTorchOnTop(BlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockState state, BlockPos pos, Direction face){
		return face == Direction.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer){
		return this.defaultBlockState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public BlockState withRotation(BlockState state, Rotation rotation){
		return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState withMirror(BlockState state, Mirror mirror){
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Nullable
	@Override
	public BlockEntity createNewTileEntity(Level world, int meta){
		return new TileEntityLectern();
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState block, Player player, InteractionHand hand,
                                    Direction side, float hitX, float hitY, float hitZ){

		BlockEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity == null || player.isShiftKeyDown()){
			return false;
		}

		player.openGui(Wizardry.instance, WizardryGuiHandler.LECTERN, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

}
