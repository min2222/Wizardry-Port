package electroblob.wizardry.block;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import java.util.Random;

public class BlockSnare extends Block implements ITileEntityProvider {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0f, 0.0f, 0.0f, 1.0f, 0.0625f, 1.0f);

	public BlockSnare(Material material){
		super(material);
		this.setSoundType(SoundType.PLANT);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos){
		return NULL_AABB;
	}

	@Override
	public boolean hasTileEntity(IBlockState state){
		return true;
	}

	@Override
	public void onEntityCollision(Level world, BlockPos pos, IBlockState state, Entity entity){

		if(entity instanceof LivingEntity){

			if(world.getTileEntity(pos) instanceof TileEntityPlayerSave){

				TileEntityPlayerSave tileentity = (TileEntityPlayerSave)world.getTileEntity(pos);

				if(AllyDesignationSystem.isValidTarget(tileentity.getCaster(), entity)){

					DamageSource source = tileentity.getCaster() == null ? DamageSource.CACTUS
							: MagicDamage.causeDirectMagicDamage(tileentity.getCaster(), DamageType.MAGIC);

					entity.attackEntityFrom(source, Spells.snare.getProperty(Spell.DAMAGE).floatValue());

					((LivingEntity)entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,
							Spells.snare.getProperty(Spell.EFFECT_DURATION).intValue(),
							Spells.snare.getProperty(Spell.EFFECT_STRENGTH).intValue()));

					if(!world.isRemote) world.destroyBlock(pos, false);
				}
			}
		}
	}

	// The similarly named onNeighborChange method does NOT do the same thing.
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos){
		super.neighborChanged(state, world, pos, block, fromPos);
		if(!world.isSideSolid(pos.down(), Direction.UP, false)){
			world.setBlockToAir(pos);
		}
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(Level world, int metadata){
		return new TileEntityPlayerSave();
	}

}
