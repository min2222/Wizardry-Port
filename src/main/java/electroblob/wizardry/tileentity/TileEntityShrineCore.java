package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockPedestal;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.packet.PacketConquerShrine;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.potion.PotionContainment;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.ArcaneLock;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.ITickable;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TileEntityShrineCore extends BlockEntity implements ITickable {

	private static final double ACTIVATION_RADIUS = 5;

	private boolean activated = false;
	private AABB containmentField;
	private final UUID[] linkedWizards = new UUID[3];
	private BlockEntity linkedContainer;
	private BlockPos linkedContainerPos; // Temporary stores the container position read from NBT until the world is set

	@Override
	public void setPos(BlockPos pos){
		super.setPos(pos);
		initContainmentField(pos);
	}

	private void initContainmentField(BlockPos pos){
		float r = PotionContainment.getContainmentDistance(0);
		this.containmentField = new AABB(-r, -r, -r, r, r, r).offset(GeometryUtils.getCentre(pos));
	}

	public void linkContainer(BlockEntity container){
		this.linkedContainer = container;
	}

	@Override
	public void update(){

		if(this.linkedContainer == null && this.linkedContainerPos != null){
			this.linkContainer(world.getTileEntity(this.linkedContainerPos));
		}

		double x = this.pos.getX() + 0.5;
		double y = this.pos.getY() + 0.5;
		double z = this.pos.getZ() + 0.5;

		if(!activated && world.getClosestPlayer(x, y, z, ACTIVATION_RADIUS, false) != null){

			this.activated = true;

			if(level.isClientSide){
				ParticleBuilder.create(Type.SPHERE).pos(x, y + 1, z).clr(0xf06495).scale(5).time(12).spawn(world);
			}

			world.playSound(x, y, z,
					WizardrySounds.BLOCK_PEDESTAL_ACTIVATE, SoundSource.BLOCKS, 1.5f, 1, false);

			if(!level.isClientSide){

				EntityEvilWizard[] wizards = new EntityEvilWizard[linkedWizards.length];

				for(int i = 0; i < linkedWizards.length; i++){

					EntityEvilWizard wizard = new EntityEvilWizard(world);

					float angle = world.random.nextFloat() * 2 * (float)Math.PI;
					double x1 = this.pos.getX() + 0.5 + 5 * Mth.sin(angle);
					double z1 = this.pos.getZ() + 0.5 + 5 * Mth.cos(angle);
					Integer y1 = BlockUtils.getNearestFloor(world, new BlockPos(x1, this.pos.getY(), z1), 8);
					if(y1 == null){
						// Fallback to the position of the shrine core if it failed to find a position (unlikely)
						x1 = this.pos.getX() + 1; // Offset it so the wizard isn't inside the block
						y1 = this.pos.getY();
						z1 = this.pos.getZ();
					}

					wizard.setLocationAndAngles(x1, y1 + 0.5, z1, 0, 0);
					wizard.setElement(world.getBlockState(pos).getValue(BlockPedestal.ELEMENT));
					wizard.onInitialSpawn(world.getDifficultyForLocation(pos), null);
					wizard.hasStructure = true;

					world.spawnEntity(wizard);
					wizards[i] = wizard;
					linkedWizards[i] = wizard.getUniqueID();
				}

				for(EntityEvilWizard wizard : wizards) wizard.groupUUIDs.addAll(Arrays.asList(linkedWizards));
			}

			containNearbyTargets();
		}

		if(activated && world.getTotalWorldTime() % 20L == 0) containNearbyTargets();

		if(activated && areWizardsDead() && !level.isClientSide){
			conquer();
		}
	}

	private boolean areWizardsDead(){

		for(UUID uuid : linkedWizards){
			Entity entity = EntityUtils.getEntityByUUID(world, uuid);
			if(entity instanceof EntityEvilWizard && entity.isEntityAlive()) return false;
		}

		return true;
	}

	public void conquer(){

		double x = this.pos.getX() + 0.5;
		double y = this.pos.getY() + 0.5;
		double z = this.pos.getZ() + 0.5;

		if(!level.isClientSide){

			WizardryPacketHandler.net.sendToAllAround(new PacketConquerShrine.Message(this.pos),
					new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), x, y, z, 64));

			if(world.getBlockState(pos).getBlock() == WizardryBlocks.runestone_pedestal){
				world.setBlockState(pos, WizardryBlocks.runestone_pedestal.getDefaultState()
						.withProperty(BlockPedestal.ELEMENT, world.getBlockState(pos).getValue(BlockPedestal.ELEMENT)));
			}else{
				Wizardry.logger.warn("What's going on?! A shrine core is being conquered but the block at its position is not a runestone pedestal!");
			}
		}

		world.markTileEntityForRemoval(this);

		if(!level.isClientSide){
			if(linkedContainer != null) NBTExtras.removeUniqueId(linkedContainer.getTileData(), ArcaneLock.NBT_KEY);
		}else{
			BlockEntity tileEntity = world.getTileEntity(this.pos.up());
			if(tileEntity != null){ // Bit of a dirty fix but it's only visual, so meh
				NBTExtras.removeUniqueId(tileEntity.getTileData(), ArcaneLock.NBT_KEY);
			}
		}

		world.playSound(x, y, z, WizardrySounds.BLOCK_PEDESTAL_CONQUER, SoundSource.BLOCKS, 1, 1, false);

		if(level.isClientSide){
			ParticleBuilder.create(Type.SPHERE).scale(5).pos(x, y + 1, z).clr(0xf06495).time(12).spawn(world);
			for(int i=0; i<5; i++){
				float brightness = 0.8f + world.random.nextFloat() * 0.2f;
				ParticleBuilder.create(Type.SPARKLE, world.rand, x, y + 1, z, 1, true)
				.clr(1, brightness, brightness).spawn(world);
			}
		}
	}

	private void containNearbyTargets(){

		List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, containmentField,
				e -> e instanceof Player || e instanceof EntityWizard || e instanceof EntityEvilWizard);

		for(LivingEntity entity : entities){
			entity.addEffect(new MobEffectInstance(WizardryPotions.containment, 219));
			NBTExtras.storeTagSafely(entity.getEntityData(), PotionContainment.ENTITY_TAG, NbtUtils.createPosTag(this.pos));
		}
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound){

		compound.setBoolean("activated", this.activated);
		if(linkedContainer != null) NBTExtras.storeTagSafely(compound, "linkedContainerPos", NbtUtils.createPosTag(linkedContainer.getPos()));

		ListTag tagList = new ListTag();
		for(UUID uuid : linkedWizards){
			if(uuid != null) tagList.appendTag(NbtUtils.createUUIDTag(uuid));
		}
		NBTExtras.storeTagSafely(compound, "wizards", tagList);

		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(CompoundTag compound){

		this.activated = compound.getBoolean("activated");
		this.linkedContainerPos = NbtUtils.getPosFromTag(compound.getCompoundTag("linkedContainerPos"));

		ListTag tagList = compound.getTagList("wizards", Constants.NBT.TAG_COMPOUND);
		int i = 0;
		for(Tag tag : tagList){
			if(tag instanceof CompoundTag) linkedWizards[i++] = NbtUtils.getUUIDFromTag((CompoundTag)tag);
			else Wizardry.logger.warn("Unexpected tag type in NBT tag list of compound tags!");
		}

		super.readFromNBT(compound);
		// Must be after super
		initContainmentField(this.pos);
	}
}
