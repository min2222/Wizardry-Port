package electroblob.wizardry.block;

import javax.annotation.Nullable;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityThorns;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlockThorns extends BushBlock implements EntityBlock {

	public static final int GROWTH_STAGES = 8;
	public static final int GROWTH_STAGE_DURATION = 2;

	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, GROWTH_STAGES-1);
	public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);

	public BlockThorns(){
		super(BlockBehaviour.Properties.of(Material.PLANT).noCollission().offsetType(BlockBehaviour.OffsetType.XZ).destroyTime(4).sound(SoundType.CROP));
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(AGE, Integer.valueOf(7)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx){

		BlockEntity tileentity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

		if(tileentity instanceof TileEntityThorns){
			return this.defaultBlockState().setValue(AGE, ((TileEntityThorns)tileentity).getAge());
		}else{
			return this.defaultBlockState().setValue(AGE, 7);
		}
	}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_49915_) {
        p_49915_.add(HALF).add(AGE);
    }

	public void placeAt(Level world, BlockPos lowerPos, int flags){
		world.setBlock(lowerPos, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER).setValue(AGE, 0), flags);
		world.setBlock(lowerPos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(AGE, 0), flags);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		world.setBlock(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), 2);
	}

	@Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState p_60518_, boolean p_60519_) {
    	super.onRemove(state, world, pos, p_60518_, p_60519_);
		if(state.getValue(HALF) == DoubleBlockHalf.LOWER){
			if(world.getBlockState(pos.above()).getBlock() == this){
				world.destroyBlock(pos.above(), false);
			}
		}else{
			if(world.getBlockState(pos.below()).getBlock() == this){
				world.destroyBlock(pos.below(), false);
			}
		}
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		if(state.getValue(HALF) == DoubleBlockHalf.UPPER){
			return worldIn.getBlockState(pos.below()).getBlock() == this;
		}else{
			BlockState iblockstate = worldIn.getBlockState(pos.above());
			return iblockstate.getBlock() == this && this.mayPlaceOn(worldIn.getBlockState(pos.below()), worldIn, pos);
		}
    }

	@Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
		if(applyThornDamage(world, pos, state, entity)){
			entity.makeStuckInBlock(state, new Vec3(0.25D, (double) 0.05F, 0.25D)); // Needs to be called client-side for players (and besides, all of this is common code)
		}
	}

	private static boolean applyThornDamage(Level world, BlockPos pos, BlockState state, Entity target){

		DamageSource source = DamageSource.CACTUS;
		float damage = Spells.FOREST_OF_THORNS.getProperty(Spell.DAMAGE).floatValue();

		BlockEntity tileentity = world.getBlockEntity(state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos);

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
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new TileEntityThorns(p_153215_, p_153216_);
	}

	@Override
	public boolean canBeReplaced(BlockState p_60470_, BlockPlaceContext p_60471_) {
		return false;
	}
	
	@Override
	protected boolean mayPlaceOn(BlockState p_51042_, BlockGetter p_51043_, BlockPos p_51044_) {
		return true;
	}
	
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153273_, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTicker(p_153273_, p_153275_, WizardryBlocks.THORNS_BLOCK_ENTITY.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level p_151988_, BlockEntityType<T> p_151989_, BlockEntityType<TileEntityThorns> p_151990_) {
        return createTickerHelper(p_151989_, p_151990_, TileEntityThorns::update);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>) p_152135_ : null;
    }

	@SubscribeEvent
	public static void onLeftClickBlockEvent(PlayerInteractEvent.LeftClickBlock event){
		if(!event.getLevel().isClientSide && event.getLevel().getBlockState(event.getPos()).getBlock() == WizardryBlocks.THORNS.get()){
			applyThornDamage(event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos()), event.getEntity());
		}
	}

}
