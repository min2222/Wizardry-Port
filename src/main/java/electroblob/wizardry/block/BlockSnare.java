package electroblob.wizardry.block;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockSnare extends BaseEntityBlock  {

	private static final VoxelShape AABB = Shapes.create(0.0f, 0.0f, 0.0f, 1.0f, 0.0625f, 1.0f);

	public BlockSnare(BlockBehaviour.Properties material){
		super(material);
	}

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_60572_, BlockGetter p_60573_, BlockPos p_60574_, CollisionContext p_60575_) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState p_60495_, Level world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            if (world.getBlockEntity(pos) instanceof TileEntityPlayerSave) {
                TileEntityPlayerSave tileentity = (TileEntityPlayerSave) world.getBlockEntity(pos);

                if (AllyDesignationSystem.isValidTarget(tileentity.getCaster(), entity)) {
                    DamageSource source = tileentity.getCaster() == null ? DamageSource.CACTUS : MagicDamage.causeDirectMagicDamage(tileentity.getCaster(), DamageType.MAGIC);

                    entity.hurt(source, Spells.SNARE.getProperty(Spell.DAMAGE).floatValue());

                    ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            Spells.SNARE.getProperty(Spell.EFFECT_DURATION).intValue(),
                            Spells.SNARE.getProperty(Spell.EFFECT_STRENGTH).intValue()));

                    if (!world.isClientSide) world.destroyBlock(pos, false);
                }
            }
        }
    }

	// The similarly named onNeighborChange method does NOT do the same thing.
    @Override
    public void neighborChanged(BlockState p_60509_, Level p_60510_, BlockPos p_60511_, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        if (!p_60510_.getBlockState(p_60511_.below()).isFaceSturdy(p_60510_, p_60511_.below(), Direction.UP)) {
            p_60510_.setBlockAndUpdate(p_60511_, Blocks.AIR.defaultBlockState());
        }
    }

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new TileEntityPlayerSave(pos, state);
	}

}
