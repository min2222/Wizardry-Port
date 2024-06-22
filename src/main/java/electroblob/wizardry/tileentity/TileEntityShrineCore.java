package electroblob.wizardry.tileentity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockPedestal;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.packet.PacketConquerShrine;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.potion.PotionContainment;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.ArcaneLock;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistry;

public class TileEntityShrineCore extends BlockEntity {
	
	private static final double ACTIVATION_RADIUS = 5;

	private boolean activated = false;
	private AABB containmentField;
	private final UUID[] linkedWizards = new UUID[3];
	private BlockEntity linkedContainer;
	private BlockPos linkedContainerPos; // Temporary stores the container position read from NBT until the world is set
	
	public TileEntityShrineCore(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
		super(p_155228_, p_155229_, p_155230_);
		initContainmentField(p_155229_);
	}

	private void initContainmentField(BlockPos pos){
		float r = PotionContainment.getContainmentDistance(0);
		this.containmentField = new AABB(-r, -r, -r, r, r, r).move(GeometryUtils.getCentre(pos));
	}

	public void linkContainer(BlockEntity container){
		this.linkedContainer = container;
	}

    public static void update(Level p_155014_, BlockPos p_155015_, BlockState p_155016_, TileEntityShrineCore p_155017_) {

		if(p_155017_.linkedContainer == null && p_155017_.linkedContainerPos != null){
			p_155017_.linkContainer(p_155014_.getBlockEntity(p_155017_.linkedContainerPos));
		}

		double x = p_155015_.getX() + 0.5;
		double y = p_155015_.getY() + 0.5;
		double z = p_155015_.getZ() + 0.5;

		if(!p_155017_.activated && p_155014_.getNearestPlayer(x, y, z, ACTIVATION_RADIUS, false) != null){

			p_155017_.activated = true;

			if(p_155014_.isClientSide){
				ParticleBuilder.create(Type.SPHERE).pos(x, y + 1, z).clr(0xf06495).scale(5).time(12).spawn(p_155014_);
			}

			p_155014_.playLocalSound(x, y, z,
					WizardrySounds.BLOCK_PEDESTAL_ACTIVATE, SoundSource.BLOCKS, 1.5f, 1, false);

			if(!p_155014_.isClientSide){

				EntityEvilWizard[] wizards = new EntityEvilWizard[p_155017_.linkedWizards.length];

				for(int i = 0; i < p_155017_.linkedWizards.length; i++){

					EntityEvilWizard wizard = new EntityEvilWizard(p_155014_);

					float angle = p_155014_.random.nextFloat() * 2 * (float)Math.PI;
					double x1 = p_155015_.getX() + 0.5 + 5 * Mth.sin(angle);
					double z1 = p_155015_.getZ() + 0.5 + 5 * Mth.cos(angle);
					Integer y1 = BlockUtils.getNearestFloor(p_155014_, new BlockPos(x1, p_155015_.getY(), z1), 8);
					if(y1 == null){
						// Fallback to the position of the shrine core if it failed to find a position (unlikely)
						x1 = p_155015_.getX() + 1; // Offset it so the wizard isn't inside the block
						y1 = p_155015_.getY();
						z1 = p_155015_.getZ();
					}

					wizard.moveTo(x1, y1 + 0.5, z1, 0, 0);
					wizard.setElement(((ForgeRegistry<Element>)Element.registry.get()).getValue(p_155014_.getBlockState(p_155015_).getValue(BlockPedestal.ELEMENT)));
					wizard.finalizeSpawn((ServerLevelAccessor) p_155014_, p_155014_.getCurrentDifficultyAt(p_155015_), MobSpawnType.STRUCTURE, null, null);
					wizard.hasStructure = true;

					p_155014_.addFreshEntity(wizard);
					wizards[i] = wizard;
					p_155017_.linkedWizards[i] = wizard.getUUID();
				}

				for(EntityEvilWizard wizard : wizards) wizard.groupUUIDs.addAll(Arrays.asList(p_155017_.linkedWizards));
			}

			p_155017_.containNearbyTargets();
		}

		if(p_155017_.activated && p_155014_.getGameTime() % 20L == 0) p_155017_.containNearbyTargets();

		if(p_155017_.activated && p_155017_.areWizardsDead() && !p_155014_.isClientSide){
			p_155017_.conquer();
		}
	}

	private boolean areWizardsDead(){

		for(UUID uuid : linkedWizards){
			Entity entity = EntityUtils.getEntityByUUID(level, uuid);
			if(entity instanceof EntityEvilWizard && entity.isAlive()) return false;
		}

		return true;
	}

	public void conquer(){

		double x = this.worldPosition.getX() + 0.5;
		double y = this.worldPosition.getY() + 0.5;
		double z = this.worldPosition.getZ() + 0.5;

		if(!level.isClientSide){

			WizardryPacketHandler.net.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(x, y, z, 64, this.level.dimension())), new PacketConquerShrine.Message(this.worldPosition));

			if(level.getBlockState(worldPosition).getBlock() instanceof BlockPedestal){
				Element element = ((ForgeRegistry<Element>)Element.registry.get()).getValue(level.getBlockState(worldPosition).getValue(BlockPedestal.ELEMENT));
				if(element == Element.EARTH) {
					level.setBlockAndUpdate(worldPosition, WizardryBlocks.EARTH_RUNESTONE_PEDESTAL.get().defaultBlockState());
				}
				else if(element == Element.FIRE) {
					level.setBlockAndUpdate(worldPosition, WizardryBlocks.FIRE_RUNESTONE_PEDESTAL.get().defaultBlockState());
				}
				else if(element == Element.HEALING) {
					level.setBlockAndUpdate(worldPosition, WizardryBlocks.HEALING_RUNESTONE_PEDESTAL.get().defaultBlockState());
				}
				else if(element == Element.ICE) {
					level.setBlockAndUpdate(worldPosition, WizardryBlocks.ICE_RUNESTONE_PEDESTAL.get().defaultBlockState());
				}
				else if(element == Element.LIGHTNING) {
					level.setBlockAndUpdate(worldPosition, WizardryBlocks.LIGHTNING_RUNESTONE_PEDESTAL.get().defaultBlockState());
				}
				else if(element == Element.NECROMANCY) {
					level.setBlockAndUpdate(worldPosition, WizardryBlocks.NECROMANCY_RUNESTONE_PEDESTAL.get().defaultBlockState());
				}
				else if(element == Element.SORCERY) {
					level.setBlockAndUpdate(worldPosition, WizardryBlocks.SORCERY_RUNESTONE_PEDESTAL.get().defaultBlockState());
				}
				
			}else{
				Wizardry.logger.warn("What's going on?! A shrine core is being conquered but the block at its position is not a runestone pedestal!");
			}
		}

		if(!level.isClientSide){
			if(linkedContainer != null) NBTExtras.removeUniqueId(linkedContainer.getPersistentData(), ArcaneLock.NBT_KEY);
		}else{
			BlockEntity tileEntity = level.getBlockEntity(this.worldPosition.above());
			if(tileEntity != null){ // Bit of a dirty fix but it's only visual, so meh
				NBTExtras.removeUniqueId(tileEntity.getPersistentData(), ArcaneLock.NBT_KEY);
			}
		}

		level.playLocalSound(x, y, z, WizardrySounds.BLOCK_PEDESTAL_CONQUER, SoundSource.BLOCKS, 1, 1, false);

		if(level.isClientSide){
			ParticleBuilder.create(Type.SPHERE).scale(5).pos(x, y + 1, z).clr(0xf06495).time(12).spawn(level);
			for(int i=0; i<5; i++){
				float brightness = 0.8f + level.random.nextFloat() * 0.2f;
				ParticleBuilder.create(Type.SPARKLE, level.random, x, y + 1, z, 1, true)
				.clr(1, brightness, brightness).spawn(level);
			}
		}
	}

	private void containNearbyTargets(){

		List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, containmentField,
				e -> e instanceof Player || e instanceof EntityWizard || e instanceof EntityEvilWizard);

		for(LivingEntity entity : entities){
			entity.addEffect(new MobEffectInstance(WizardryPotions.CONTAINMENT.get(), 219));
			NBTExtras.storeTagSafely(entity.getPersistentData(), PotionContainment.ENTITY_TAG, NbtUtils.writeBlockPos(this.worldPosition));
		}
	}

	@Override
	public void saveAdditional(CompoundTag compound){

		compound.putBoolean("activated", this.activated);
		if(linkedContainer != null) NBTExtras.storeTagSafely(compound, "linkedContainerPos", NbtUtils.writeBlockPos(linkedContainer.getBlockPos()));

		ListTag tagList = new ListTag();
		for(UUID uuid : linkedWizards){
			if(uuid != null) tagList.add(NbtUtils.createUUID(uuid));
		}
		NBTExtras.storeTagSafely(compound, "wizards", tagList);
		super.saveAdditional(compound);
	}

	@Override
	public void load(CompoundTag compound){

		this.activated = compound.getBoolean("activated");
		this.linkedContainerPos = NbtUtils.readBlockPos(compound.getCompound("linkedContainerPos"));

		ListTag tagList = compound.getList("wizards", Tag.TAG_COMPOUND);
		int i = 0;
		for(Tag tag : tagList){
			if(tag instanceof CompoundTag) linkedWizards[i++] = NbtUtils.loadUUID((CompoundTag)tag);
			else Wizardry.logger.warn("Unexpected tag type in NBT tag list of compound tags!");
		}

		super.load(compound);
		// Must be after super
		initContainmentField(this.worldPosition);
	}
}
