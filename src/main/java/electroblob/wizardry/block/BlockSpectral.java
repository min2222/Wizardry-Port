package electroblob.wizardry.block;

import javax.annotation.Nullable;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlockSpectral extends BaseEntityBlock {

	public BlockSpectral(BlockBehaviour.Properties material){
		super(material);
	}

	// Replaces getRenderBlockPass
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.TRANSLUCENT;
	}
	
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

	@Override
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random){
		
		for(int i=0; i<2; i++){
			ParticleBuilder.create(Type.DUST)
			.pos(pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble())
			.time((int)(16.0D / (Math.random() * 0.8D + 0.2D)))
			.clr(0.4f + random.nextFloat() * 0.2f, 0.6f + random.nextFloat() * 0.4f, 0.6f + random.nextFloat() * 0.4f)
			.shaded(true).spawn(world);
		}
	}

//	// Overriden to make the block always look full brightness despite not emitting
//	// full light.
//	@Override
//	public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos){
//		return 15;
//	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityTimer(pos, state, 1200);
	}

	@SubscribeEvent
	public static void onBlockPlaceEvent(EntityPlaceEvent event){
		// Spectral blocks cannot be built on
		if(event.getPlacedAgainst() == WizardryBlocks.SPECTRAL_BLOCK.get().defaultBlockState()){
			event.setCanceled(true);
		}
	}
	
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153273_, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTicker(p_153273_, p_153275_, WizardryBlocks.TIMER_BLOCK_ENTITY.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level p_151988_, BlockEntityType<T> p_151989_, BlockEntityType<TileEntityTimer> p_151990_) {
        return createTickerHelper(p_151989_, p_151990_, TileEntityTimer::update);
    }

}
