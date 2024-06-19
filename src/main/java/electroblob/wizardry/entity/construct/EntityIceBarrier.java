package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.ICustomHitbox;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityIceBarrier extends EntityScaledConstruct implements ICustomHitbox {

	private static final double THICKNESS = 0.4;

	private int delay = 0;

	public EntityIceBarrier(Level world){
		this(WizardryEntities.ICE_BARRIER.get(), world);
	}
	
	public EntityIceBarrier(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
	}

	public void setDelay(int delay){
		this.delay = delay;
		this.lifetime += delay;
	}

	@Override
	public void setRot(float yaw, float pitch){
		super.setRot(yaw, pitch);
		float a = Mth.cos((float)Math.toRadians(getYRot()));
		float b = Mth.sin((float)Math.toRadians(getYRot()));
		double x = getBbWidth()/2 * a + THICKNESS/2 * b;
		double z = getBbWidth()/2 * b + THICKNESS/2 * a;
		setBoundingBox(new AABB(this.getX() - x, this.getY(), this.getZ() - z, this.getX() + x, this.getY() + getBbHeight(), this.getZ() + z));
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public void tick(){

		// Bit of a cheat but it's easier than trying to sync FrostBarrier#addConstructExtras
		if(level.isClientSide && firstTick){
			setSizeMultiplier(sizeMultiplier); // Do this first or it'll overwrite the bounding box
			setRot(getYRot(), getXRot());
		}

		this.xo = getX();
		this.yo = getY();
		this.zo = getZ();

		if(!level.isClientSide){

			double extensionSpeed = 0;

			if(lifetime - this.tickCount < 20){
				extensionSpeed = -0.01 * (this.tickCount - (lifetime - 20)) * sizeMultiplier;
			}else if(tickCount > 3 + delay){
				extensionSpeed = 0;
			}else if(tickCount > delay){
				extensionSpeed = 0.5 * sizeMultiplier;
			}

			this.move(MoverType.SELF, new Vec3(0, extensionSpeed, 0));
		}

		if(tickCount == delay + 1) this.playSound(WizardrySounds.ENTITY_ICE_BARRIER_EXTEND, 1, 1.5f);

		super.tick();

		Vec3 look = this.getLookAngle();

		if(!level.isClientSide){

			for(Entity entity : level.getEntities(this, getBoundingBox().inflate(2))){

				if(entity instanceof EntityMagicConstruct) continue;

				if(!entity.getBoundingBox().intersects(this.getBoundingBox())) continue;

				// For some reason the player position seems to be off by 1 block in x and z, no idea how so for now
				// I've just fudged it by adding 1 to x and z
				double perpendicularDist = getSignedPerpendicularDistance(entity.position().add(1, 0, 1));

				if(Math.abs(perpendicularDist) < entity.getBbWidth()/2 + THICKNESS/2){

					double velocity = 0.25 * Math.signum(perpendicularDist);
					entity.push(velocity * look.x, 0, velocity * look.z);
					// Player motion is handled on that player's client so needs packets
					if(entity instanceof ServerPlayer){
						((ServerPlayer)entity).connection.send(new ClientboundSetEntityMotionPacket(entity));
					}
				}
			}
		}

	}

	@Override
	public boolean isOnFire(){
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
	protected void readAdditionalSaveData(CompoundTag nbt){
		super.readAdditionalSaveData(nbt);
		delay = nbt.getInt("delay");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt){
		super.addAdditionalSaveData(nbt);
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
		return getBoundingBox().inflate(fuzziness).contains(intercept) ? intercept : null;
	}

	@Override
	public boolean contains(Vec3 point){
		return this.getBoundingBox().contains(point) && getPerpendicularDistance(point) < THICKNESS/2;
	}

	private double getPerpendicularDistance(Vec3 point){
		return Math.abs(getSignedPerpendicularDistance(point));
	}

	private double getSignedPerpendicularDistance(Vec3 point){
		Vec3 look = this.getLookAngle();
		Vec3 delta = new Vec3(point.x - this.getX(), 0, point.z - this.getZ());
		return delta.dot(look);
	}

}
