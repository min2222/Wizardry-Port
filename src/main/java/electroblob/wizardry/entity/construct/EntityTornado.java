package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.Tornado;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;

public class EntityTornado extends EntityScaledConstruct {

	private double velX, velZ;

	public EntityTornado(Level world){
		this(WizardryEntities.TORNADO.get(), world);
		setSize(Spells.tornado.getProperty(Spell.EFFECT_RADIUS).floatValue(), 8);
	}
	
	public EntityTornado(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(Spells.tornado.getProperty(Spell.EFFECT_RADIUS).floatValue(), 8);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void setHorizontalVelocity(double velX, double velZ){
		this.velX = velX;
		this.velZ = velZ;
	}

	@Override
	public void tick(){

		super.tick();

		double radius = getBbWidth()/2;

		if(this.tickCount % 120 == 1 && level.isClientSide){
			// Repeat is false so that the sound fades out when the tornado does rather than stopping suddenly
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_TORNADO_AMBIENT, WizardrySounds.SPELLS, 1.0f, 1.0f, false);
		}

		this.move(MoverType.SELF, new Vec3(velX, this.getDeltaMovement().y, velZ));

		BlockPos pos = this.blockPosition();
		Integer y = BlockUtils.getNearestSurface(level, pos.above(3), Direction.UP, 5, true, BlockUtils.SurfaceCriteria.NOT_AIR_TO_AIR);

		if(y != null){

			pos = new BlockPos(pos.getX(), y, pos.getZ());

			if(this.level.getBlockState(pos).getMaterial() == Material.LAVA){
				// Fire tornado!
				this.setSecondsOnFire(5);
			}
		}

		if(!this.level.isClientSide){

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, this.getX(), this.getY(),
					this.getZ(), this.level);

			for(LivingEntity target : targets){

				if(target instanceof Player && ((getCaster() instanceof Player && !Wizardry.settings.playersMoveEachOther)
						|| ItemArtefact.isArtefactActive((Player)target, WizardryItems.AMULET_ANCHORING.get()))){
					continue;
				}

				if(this.isValidTarget(target)){

					double velY = target.getDeltaMovement().y;

					// TODO: This doesn't seem right...
					double dx = (this.getX() - target.getX() > 0 ? 0.5 : -0.5) - (this.getX() - target.getX()) * 0.125;
					double dz = (this.getZ() - target.getZ() > 0 ? 0.5 : -0.5) - (this.getZ() - target.getZ()) * 0.125;

					if(this.isOnFire()){
						target.setSecondsOnFire(4); // Just a fun Easter egg so no properties here!
					}

					float damage = Spells.tornado.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

					if(this.getCaster() != null){
						target.hurt( MagicDamage.causeIndirectMagicDamage(this, getCaster(),
								DamageType.MAGIC), damage);
					}else{
						target.hurt(DamageSource.MAGIC, damage);
					}

					target.setDeltaMovement(dx, velY + Spells.tornado.getProperty(Tornado.UPWARD_ACCELERATION).floatValue(), dz);

					// Player motion is handled on that player's client so needs packets
					if(target instanceof ServerPlayer){
						((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
					}
				}
			}
		}else{
			for(int i = 1; i < 10; i++){

				double yPos = random.nextDouble() * 8;

				int blockX = (int)this.getX() - 2 + this.random.nextInt(4);
				int blockZ = (int)this.getZ() - 2 + this.random.nextInt(4);

				BlockPos pos1 = new BlockPos(blockX, this.getY() + 3, blockZ);

				Integer blockY = BlockUtils.getNearestSurface(level, pos1, Direction.UP, 5, true, BlockUtils.SurfaceCriteria.NOT_AIR_TO_AIR);

				if(blockY != null){

					blockY--;

					pos1 = new BlockPos(pos1.getX(), blockY, pos1.getZ());

					BlockState block = this.level.getBlockState(pos1);

					// If the block it found was air or something it can't pick up, it makes a best guess based on the biome
					if(!canTornadoPickUpBitsOf(block)){
						block = level.getBiome(pos1).topBlock;
					}

					Wizardry.proxy.spawnTornadoParticle(level, this.getX(), this.getY() + yPos, this.getZ(), this.velX, this.velZ,
							yPos / 3 + 0.5d, 100, block, pos1);
					Wizardry.proxy.spawnTornadoParticle(level, this.getX(), this.getY() + yPos, this.getZ(), this.velX, this.velZ,
							yPos / 3 + 0.5d, 100, block, pos1);

					// Sometimes spawns leaf particles if the block is leaves, or snow particles if the block is snow
					if(this.random.nextInt(3) == 0){

						ResourceLocation type = null;

						if(block.getMaterial() == Material.LEAVES) type = Type.LEAF;
						if(block.getMaterial() == Material.SNOW || block.getMaterial() == Material.TOP_SNOW)
							type = Type.SNOW;

						if(type != null){
							double yPos1 = random.nextDouble() * 8;
							ParticleBuilder.create(type)
									.pos(this.getX() + (random.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d), this.getY() + yPos1,
											this.getZ() + (random.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d))
									.time(40 + random.nextInt(10))
									.spawn(level);
						}
					}
				}
			}
		}
	}

	private static boolean canTornadoPickUpBitsOf(BlockState block){
		Material material = block.getMaterial();
		return material == Material.TOP_SNOW || material == Material.DIRT || material == Material.GRASS
				|| material == Material.LAVA || material == Material.SAND || material == Material.SNOW
				|| material == Material.WATER || material == Material.PLANT || material == Material.LEAVES
				|| material == Material.REPLACEABLE_PLANT;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		velX = nbttagcompound.getDouble("velX");
		velZ = nbttagcompound.getDouble("velZ");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putDouble("velX", velX);
		nbttagcompound.putDouble("velZ", velZ);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		super.writeSpawnData(data);
		data.writeDouble(velX);
		data.writeDouble(velZ);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){
		super.readSpawnData(data);
		this.velX = data.readDouble();
		this.velZ = data.readDouble();
	}

}
