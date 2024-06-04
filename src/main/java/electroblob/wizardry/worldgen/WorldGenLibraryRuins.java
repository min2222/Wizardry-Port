package electroblob.wizardry.worldgen;

import com.google.common.collect.ImmutableList;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.util.BlockUtils;
import net.minecraft.world.level.block.BlockPlanks;
import net.minecraft.world.level.block.BlockStoneBrick;
import net.minecraft.world.level.block.BlockStoneSlab;
import net.minecraft.world.level.block.BlockStoneSlab.EnumType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Random;

public class WorldGenLibraryRuins extends WorldGenSurfaceStructure {

	private static final List<Type> BIOME_TYPES = ImmutableList.of(Type.FOREST, Type.JUNGLE, Type.SWAMP);

	@Override
	public String getStructureName(){
		return "library_ruins";
	}

	@Override
	public long getRandomSeedModifier(){
		return 10149523L;
	}

	@Override
	public boolean canGenerate(Random random, Level world, int chunkX, int chunkZ){
		return ArrayUtils.contains(Wizardry.settings.libraryDimensions, world.provider.getDimension())
				// +8 for the anti-cascading offset, and +8 for the middle of the generated area makes +16 in total
				&& BiomeDictionary.getTypes(level.getBiome(new BlockPos(chunkX * 16 + 16, 0, chunkZ * 16 + 16)))
				.stream().anyMatch(BIOME_TYPES::contains)
				&& Wizardry.settings.libraryRarity > 0 && random.nextInt(Wizardry.settings.libraryRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random){
		return Wizardry.settings.libraryFiles[random.nextInt(Wizardry.settings.libraryFiles.length)];
	}

	@Override
	public void spawnStructure(Random random, Level world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile){

		final Biome biome = level.getBiome(origin);
		final float stoneBrickChance = random.nextFloat();
		final float mossiness = 0.4f;
		final BlockPlanks.EnumType woodType = BlockUtils.getBiomeWoodVariant(biome);

		ITemplateProcessor processor = new MultiTemplateProcessor(true,
				// Cobblestone/stone brick
				(w, p, i) -> {
					if(w.random.nextFloat() > stoneBrickChance){
						// Behold, three different ways of doing the same thing, because this is pre-flattening!
						// Also, stone bricks are about the least consistently-named thing in the entire game, so yay
						if(i.blockState.getBlock() == Blocks.COBBLESTONE){
							return new Template.BlockInfo(i.pos, Blocks.STONEBRICK.defaultBlockState(), i.tileentityData);
						}else if(i.blockState.getBlock() == Blocks.STONE_SLAB
								&& i.blockState.getValue(BlockStoneSlab.VARIANT) == EnumType.COBBLESTONE){
							return new Template.BlockInfo(i.pos, i.blockState.withProperty(BlockStoneSlab.VARIANT, EnumType.SMOOTHBRICK), i.tileentityData);
						}else if(i.blockState.getBlock() == Blocks.STONE_STAIRS){ // "Stone" stairs are actually cobblestone
							return new Template.BlockInfo(i.pos, BlockUtils.copyState(Blocks.STONE_BRICK_STAIRS, i.blockState), i.tileentityData);
						}
					}
					return i;
				},
				// Wood type
				new WoodTypeTemplateProcessor(woodType),
				// Mossifier
				new MossifierTemplateProcessor(mossiness, 0.04f, origin.getY() + 1),
				// Stone brick smasher-upper
				(w, p, i) -> i.blockState.getBlock() == Blocks.STONEBRICK && w.random.nextFloat() < 0.1f ?
						new Template.BlockInfo(i.pos, Blocks.STONEBRICK.defaultBlockState().withProperty(
								BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), i.tileentityData) : i,
				// Bookshelf marker
				(w, p, i) -> {
					TileEntityBookshelf.markAsNatural(i.tileentityData);
					return i;
				}
		);

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);

		WizardryAntiqueAtlasIntegration.markLibrary(world, origin.getX(), origin.getZ(), false);
	}

}
