package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.ICustomHitbox;
import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class EntityIceBarrier extends EntityScaledConstruct implements ICustomHitbox {

	private static final double THICKNESS = 0.4;

	private int delay = 0;

	public EntityIceBarrier(Level world){
		super(world);
		this.setSize(1.8f, 1.05f);
	}

	public void setDelay(int delay){
		this.delay = delay;
		this.lifetime += delay;
	}

	@Override
	public void setRotation(float yaw, float pitch){
		super.setRotation(yaw, pitch);
		float a = Mth.cos((float)Math.toRadians(rotationYaw));
		float b = Mth.sin((float)Math.toRadians(rotationYaw));
		double x = width/2 * a + THICKNESS/2 * b;
		double z = width/2 * b + THICKNESS/2 * a;
		setEntityBoundingBox(new AABB(this.getX() - x, this.getY(), this.getZ() - z, this.getX() + x, this.getY() + height, this.getZ() + z));
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public void onUpdate(){

		// Bit of a cheat but it's easier than trying to sync FrostBarrier#addConstructExtras
		if(level.isClientSide && firstUpdate){
			setSizeMultiplier(sizeMultiplier); // Do this first or it'll overwrite the bounding box
			setRotation(rotationYaw, rotationPitch);
		}

		this.prevgetX() = getX();
		this.prevgetY() = getY();
		this.prevgetZ() = getZ();

		if(!level.isClientSide){

			double extensionSpeed = 0;

			if(lifetime - this.tickCount < 20){
				extensionSpeed = -0.01 * (this.tickCount - (lifetime - 20)) * sizeMultiplier;
			}else if(tickCount > 3 + delay){
				extensionSpeed = 0;
			}else if(tickCount > delay){
				extensionSpeed = 0.5 * sizeMultiplier;
			}

			this.move(MoverType.SELF, 0, extensionSpeed, 0);
		}

		if(tickCount == delay + 1) this.playSound(WizardrySounds.ENTITY_ICE_BARRIER_EXTEND, 1, 1.5f);

		super.onUpdate();

		Vec3 look = this.getLookVec();

		if(!level.isClientSide){

			for(Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().grow(2))){

				if(entity instanceof EntityMagicConstruct) continue;

				if(!entity.getEntityBoundingBox().intersects(this.getEntityBoundingBox())) continue;

				// For some reason the player position seems to be off by 1 block in x and z, no idea how so for now
				// I've just fudged it by adding 1 to x and z
				double perpendicularDist = getSignedPerpendicularDistance(entity.getPositionVector().add(1, 0, 1));

				if(Math.abs(perpendicularDist) < entity.width/2 + THICKNESS/2){

					double velocity = 0.25 * Math.signum(perpendicularDist);
					entity.addVelocity(velocity * look.x, 0, velocity * look.z);
					// Player motion is handled on that player's client so needs packets
					if(entity instanceof ServerPlayer){
						((ServerPlayer)entity).connection.sendPacket(new SPacketEntityVelocity(entity));
					}
				}
			}
		}

	}

	@Override
	public boolean isBurning(){
		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount){
		this.playSound(WizardrySounds.ENTITY_ICE_BARRIER_DEFLECT, 0.7f, 2.5f);
		return super.hurt(source, amount);
	}

//	@Override
//	public int getBrightnessForRender(){
//		return 15728880;
//	}

	@Override
	protected void readEntityFromNBT(CompoundTag nbt){
		super.readEntityFromNBT(nbt);
		delay = nbt.getInt("delay");
	}

	@Override
	protected void writeEntityToNBT(CompoundTag nbt){
		super.writeEntityToNBT(nbt);
		nbt.putInt("delay", delay);
	}

	@Override
	public Vec3 calculateIntercept(Vec3 origin, Vec3 endpoint, float fuzziness){
		// Calculate the point at which the line intersects the barrier plane
		Vec3 vec = endpoint.subtract(origin);

		double perpendicularDist = getPerpendicularDistance(origin);
		double perpendicularDist2 = getPerpendicularDistance(endpoint);

		Vec3 intercept = origin.add(vec.scale(perpendicularDist / (perpendicularDist + perpendicularDist2)));

		// This seems to be all over the palce, but the calculation MUST be right because it works for entity collisions!
//		world.spawnParticle(EnumParticleTypes.END_ROD, intercept.x, intercept.y, intercept.z, 0, 0, 0);

		// If the point is within the hitbox (expanded by the fuzziness), it was a hit
		return getEntityBoundingBox().grow(fuzziness).contains(intercept) ? intercept : null;
	}

	@Override
	public boolean contains(Vec3 point){
		return this.getEntityBoundingBox().contains(point) && getPerpendicularDistance(point) < THICKNESS/2;
	}

	private double getPerpendicularDistance(Vec3 point){
		return Math.abs(getSignedPerpendicularDistance(point));
	}

	private double getSignedPerpendicularDistance(Vec3 point){
		Vec3 look = this.getLookVec();
		Vec3 delta = new Vec3(point.x - this.getX(), 0, point.z - this.getZ());
		return delta.dotProduct(look);
	}

}
