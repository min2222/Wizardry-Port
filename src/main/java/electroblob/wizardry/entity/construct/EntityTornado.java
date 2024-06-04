package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
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
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityTornado extends EntityScaledConstruct {

	private double velX, velZ;

	public EntityTornado(Level world){
		super(world);
		setSize(Spells.tornado.getProperty(Spell.EFFECT_RADIUS).floatValue(), 8);
		this.isImmuneToFire = false;
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
	public void onUpdate(){

		super.onUpdate();

		double radius = width/2;

		if(this.ticksExisted % 120 == 1 && world.isRemote){
			// Repeat is false so that the sound fades out when the tornado does rather than stopping suddenly
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_TORNADO_AMBIENT, WizardrySounds.SPELLS, 1.0f, 1.0f, false);
		}

		this.move(MoverType.SELF, velX, motionY, velZ);

		BlockPos pos = new BlockPos(this);
		Integer y = BlockUtils.getNearestSurface(world, pos.up(3), Direction.UP, 5, true, BlockUtils.SurfaceCriteria.NOT_AIR_TO_AIR);

		if(y != null){

			pos = new BlockPos(pos.getX(), y, pos.getZ());

			if(this.world.getBlockState(pos).getMaterial() == Material.LAVA){
				// Fire tornado!
				this.setFire(5);
			}
		}

		if(!this.world.isRemote){

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, this.posX, this.posY,
					this.posZ, this.world);

			for(LivingEntity target : targets){

				if(target instanceof Player && ((getCaster() instanceof Player && !Wizardry.settings.playersMoveEachOther)
						|| ItemArtefact.isArtefactActive((Player)target, WizardryItems.amulet_anchoring))){
					continue;
				}

				if(this.isValidTarget(target)){

					double velY = target.motionY;

					// TODO: This doesn't seem right...
					double dx = (this.posX - target.posX > 0 ? 0.5 : -0.5) - (this.posX - target.posX) * 0.125;
					double dz = (this.posZ - target.posZ > 0 ? 0.5 : -0.5) - (this.posZ - target.posZ) * 0.125;

					if(this.isBurning()){
						target.setFire(4); // Just a fun Easter egg so no properties here!
					}

					float damage = Spells.tornado.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

					if(this.getCaster() != null){
						target.attackEntityFrom( MagicDamage.causeIndirectMagicDamage(this, getCaster(),
								DamageType.MAGIC), damage);
					}else{
						target.attackEntityFrom(DamageSource.MAGIC, damage);
					}

					target.motionX = dx;
					target.motionY = velY + Spells.tornado.getProperty(Tornado.UPWARD_ACCELERATION).floatValue();
					target.motionZ = dz;

					// Player motion is handled on that player's client so needs packets
					if(target instanceof ServerPlayer){
						((ServerPlayer)target).connection.sendPacket(new SPacketEntityVelocity(target));
					}
				}
			}
		}else{
			for(int i = 1; i < 10; i++){

				double yPos = rand.nextDouble() * 8;

				int blockX = (int)this.posX - 2 + this.rand.nextInt(4);
				int blockZ = (int)this.posZ - 2 + this.rand.nextInt(4);

				BlockPos pos1 = new BlockPos(blockX, this.posY + 3, blockZ);

				Integer blockY = BlockUtils.getNearestSurface(world, pos1, Direction.UP, 5, true, BlockUtils.SurfaceCriteria.NOT_AIR_TO_AIR);

				if(blockY != null){

					blockY--;

					pos1 = new BlockPos(pos1.getX(), blockY, pos1.getZ());

					BlockState block = this.world.getBlockState(pos1);

					// If the block it found was air or something it can't pick up, it makes a best guess based on the biome
					if(!canTornadoPickUpBitsOf(block)){
						block = world.getBiome(pos1).topBlock;
					}

					Wizardry.proxy.spawnTornadoParticle(world, this.posX, this.posY + yPos, this.posZ, this.velX, this.velZ,
							yPos / 3 + 0.5d, 100, block, pos1);
					Wizardry.proxy.spawnTornadoParticle(world, this.posX, this.posY + yPos, this.posZ, this.velX, this.velZ,
							yPos / 3 + 0.5d, 100, block, pos1);

					// Sometimes spawns leaf particles if the block is leaves, or snow particles if the block is snow
					if(this.rand.nextInt(3) == 0){

						ResourceLocation type = null;

						if(block.getMaterial() == Material.LEAVES) type = Type.LEAF;
						if(block.getMaterial() == Material.SNOW || block.getMaterial() == Material.CRAFTED_SNOW)
							type = Type.SNOW;

						if(type != null){
							double yPos1 = rand.nextDouble() * 8;
							ParticleBuilder.create(type)
									.pos(this.posX + (rand.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d), this.posY + yPos1,
											this.posZ + (rand.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d))
									.time(40 + rand.nextInt(10))
									.spawn(world);
						}
					}
				}
			}
		}
	}

	private static boolean canTornadoPickUpBitsOf(BlockState block){
		Material material = block.getMaterial();
		return material == Material.CRAFTED_SNOW || material == Material.GROUND || material == Material.GRASS
				|| material == Material.LAVA || material == Material.SAND || material == Material.SNOW
				|| material == Material.WATER || material == Material.PLANTS || material == Material.LEAVES
				|| material == Material.VINE;
	}

	@Override
	protected void readEntityFromNBT(CompoundTag nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		velX = nbttagcompound.getDouble("velX");
		velZ = nbttagcompound.getDouble("velZ");
	}

	@Override
	protected void writeEntityToNBT(CompoundTag nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setDouble("velX", velX);
		nbttagcompound.setDouble("velZ", velZ);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeDouble(velX);
		data.writeDouble(velZ);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		this.velX = data.readDouble();
		this.velZ = data.readDouble();
	}

}
