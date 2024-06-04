package electroblob.wizardry.block;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityThorns;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import net.minecraft.world.level.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.world.level.block.properties.PropertyEnum;
import net.minecraft.world.level.block.properties.PropertyInteger;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class BlockThorns extends BlockBush implements ITileEntityProvider {

	public static final int GROWTH_STAGES = 8;
	public static final int GROWTH_STAGE_DURATION = 2;

	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, GROWTH_STAGES-1);
	public static final PropertyEnum<EnumBlockHalf> HALF = PropertyEnum.create("half", EnumBlockHalf.class);

	public BlockThorns(){
		this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, EnumBlockHalf.LOWER).withProperty(AGE, 7));
		this.setHardness(4);
		this.setSoundType(SoundType.PLANT);
		this.setCreativeTab(null);
	}

	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		return FULL_BLOCK_AABB;
	}

	@Override
	public BlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(HALF, meta == 0 ? EnumBlockHalf.LOWER : EnumBlockHalf.UPPER);
	}

	@Override
	public int getMetaFromState(BlockState state){
		return state.getValue(HALF).ordinal();
	}

	@Override
	public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos){

		if(state.getValue(HALF) == EnumBlockHalf.UPPER) pos = pos.down();
		// Copied from BlockFlowerPot on authority of the Forge docs, which say it needs to be here
		BlockEntity tileentity = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);

		if(tileentity instanceof TileEntityThorns){
			return state.withProperty(AGE, ((TileEntityThorns)tileentity).getAge());
		}else{
			return state.withProperty(AGE, 7);
		}
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, HALF, AGE);
	}

	public void placeAt(Level world, BlockPos lowerPos, int flags){
		world.setBlockState(lowerPos, this.getDefaultState().withProperty(HALF, EnumBlockHalf.LOWER).withProperty(AGE, 0), flags);
		world.setBlockState(lowerPos.up(), this.getDefaultState().withProperty(HALF, EnumBlockHalf.UPPER).withProperty(AGE, 0), flags);
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		world.setBlockState(pos.up(), this.getDefaultState().withProperty(HALF, EnumBlockHalf.UPPER), 2);
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state){
		super.breakBlock(world, pos, state);
		if(state.getValue(HALF) == EnumBlockHalf.LOWER){
			if(world.getBlockState(pos.up()).getBlock() == this){
				world.destroyBlock(pos.up(), false);
			}
		}else{
			if(world.getBlockState(pos.down()).getBlock() == this){
				world.destroyBlock(pos.down(), false);
			}
		}
	}

	public boolean canBlockStay(Level worldIn, BlockPos pos, BlockState state){
		if(state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER){
			return worldIn.getBlockState(pos.down()).getBlock() == this;
		}else{
			BlockState iblockstate = worldIn.getBlockState(pos.up());
			return iblockstate.getBlock() == this && this.canSustainBush(worldIn.getBlockState(pos.down()));
		}
	}

	@Override
	public void onEntityCollision(Level world, BlockPos pos, BlockState state, Entity entity){
		if(applyThornDamage(world, pos, state, entity)){
			entity.setInWeb(); // Needs to be called client-side for players (and besides, all of this is common code)
		}
	}

	private static boolean applyThornDamage(Level world, BlockPos pos, BlockState state, Entity target){

		DamageSource source = DamageSource.CACTUS;
		float damage = Spells.forest_of_thorns.getProperty(Spell.DAMAGE).floatValue();

		BlockEntity tileentity = world.getTileEntity(state.getValue(HALF) == EnumBlockHalf.UPPER ? pos.down() : pos);

		if(tileentity instanceof TileEntityThorns){

			damage *= ((TileEntityThorns)tileentity).damageMultiplier;

			LivingEntity caster = ((TileEntityThorns)tileentity).getCaster();

			if(!AllyDesignationSystem.isValidTarget(caster, target)) return false; // Don't attack or slow allies of the caster

			if(caster != null){
				source = MagicDamage.causeDirectMagicDamage(caster, MagicDamage.DamageType.MAGIC);
			}
		}

		if(target.tickCount % 20 == 0){
			EntityUtils.attackEntityWithoutKnockback(target, source, damage);
		}

		return true;
	}

	@Override
	public Block.EnumOffsetType getOffsetType(){
		return Block.EnumOffsetType.XZ;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int metadata){
		return new TileEntityThorns();
	}

	@Override
	public boolean hasTileEntity(BlockState state){
		return state.getValue(HALF) == EnumBlockHalf.LOWER;
	}

	@Override public boolean isReplaceable(IBlockAccess world, BlockPos pos){ return false; }
	@Override protected boolean canSustainBush(BlockState state){ return state.isNormalCube(); }
	@Override public Item getItemDropped(BlockState state, Random rand, int fortune){ return Items.AIR; }
	@Override public boolean canSilkHarvest(Level world, BlockPos pos, BlockState state, Player player){ return false; }

	@SubscribeEvent
	public static void onLeftClickBlockEvent(PlayerInteractEvent.LeftClickBlock event){
		if(!event.getWorld().isRemote && event.getWorld().getBlockState(event.getPos()).getBlock() == WizardryBlocks.thorns){
			applyThornDamage(event.getWorld(), event.getPos(), event.getWorld().getBlockState(event.getPos()), event.getEntity());
		}
	}

}
