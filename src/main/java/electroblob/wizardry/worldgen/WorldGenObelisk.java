package electroblob.wizardry.worldgen;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockRunestone;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.entity.living.EntityRemnant;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.Mirror;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.Random;

public class WorldGenObelisk extends WorldGenSurfaceStructure {

	private static final String SPAWNER_DATA_BLOCK_TAG = "spawner";

	private static final ResourceLocation REMNANT_ID = new ResourceLocation(Wizardry.MODID, "remnant");

	@Override
	public String getStructureName(){
		return "obelisk";
	}

	@Override
	public long getRandomSeedModifier(){
		return 19348242L;
	}

	@Override
	public Mirror[] getValidMirrors(){
		return new Mirror[]{Mirror.NONE}; // It's symmetrical so there's no point mirroring it
	}

	@Override
	public boolean canGenerate(Random random, Level world, int chunkX, int chunkZ){
		return ArrayUtils.contains(Wizardry.settings.obeliskDimensions, world.provider.getDimension())
				&& Wizardry.settings.obeliskRarity > 0 && random.nextInt(Wizardry.settings.obeliskRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random){
		return Wizardry.settings.obeliskFiles[random.nextInt(Wizardry.settings.obeliskFiles.length)];
	}

	@Override
	public void spawnStructure(Random random, Level world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile){

		final Element element = Element.values()[1 + random.nextInt(Element.values().length-1)];

		ITemplateProcessor processor = (w, p, i) -> i.blockState.getBlock() instanceof BlockRunestone ? new Template.BlockInfo(
				i.pos, i.blockState.withProperty(BlockRunestone.ELEMENT, element), i.tileentityData) : i;

		template.addBlocksToWorld(world, origin, processor, settings, 2 | 16);

		WizardryAntiqueAtlasIntegration.markObelisk(world, origin.getX(), origin.getZ());

		// Mob spawner
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);

		for(Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()){

			if(entry.getValue().equals(SPAWNER_DATA_BLOCK_TAG)){

				BlockPos pos = entry.getKey();

				level.setBlockAndUpdate(pos, Blocks.MOB_SPAWNER.defaultBlockState());

				if(level.getTileEntity(pos) instanceof TileEntityMobSpawner){

					MobSpawnerBaseLogic spawnerLogic = ((TileEntityMobSpawner)level.getTileEntity(pos)).getSpawnerBaseLogic();

					EntityRemnant remant = new EntityRemnant(world);
					remant.setElement(element);
					remant.setBoundOrigin(pos);

					CompoundTag entityTag = new CompoundTag();
					entityTag.setString("id", REMNANT_ID.toString());
					remant.writeEntityToNBT(entityTag);
					CompoundTag nbt = new CompoundTag();
					nbt.setTag("Entity", entityTag);

					spawnerLogic.setNextSpawnData(new WeightedSpawnerEntity(nbt));

				}else{
					Wizardry.logger.info("Tried to set the mob spawned by an obelisk, but the expected TileEntityMobSpawner was not present");
				}

			}else{
				// This probably shouldn't happen...
				Wizardry.logger.info("Unrecognised data block value {} in structure {}", entry.getValue(), structureFile);
			}
		}
	}
}
