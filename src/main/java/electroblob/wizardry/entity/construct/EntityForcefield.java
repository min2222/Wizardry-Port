package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.ICustomHitbox;
import electroblob.wizardry.entity.projectile.EntityMagicArrow;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityXPOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.EntityThrowable;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// TODO: Possibly convert this to EntityScaledConstruct
@Mod.EventBusSubscriber
public class EntityForcefield extends EntityMagicConstruct implements ICustomHitbox {

	/** Extra radius to search around the forcefield for incoming entities. Any entities with a velocity greater than
	 * this could potentially penetrate the forcefield. */
	private static final double SEARCH_BORDER_SIZE = 4;

	private static final float BOUNCINESS = 0.2f;

	private float radius;

	public EntityForcefield(Level world){
		super(world);
		setRadius(3); // Shouldn't be needed but it's a good failsafe
		this.ignoreFrustumCheck = true;
		this.noClip = true;
	}

	public void setRadius(float radius){
		this.radius = radius;
		this.getBbHeight() = 2 * radius;
		this.width = 2 * radius;
		// y-3 because it needs to be centred on the given position
		this.setEntityBoundingBox(new AABB(getX() - radius, getY() - radius, getZ() - radius,
				getX() + radius, getY() + radius, getZ() + radius));
	}

	public float getRadius(){
		return radius;
	}

	@Nullable
	@Override
	public AABB getCollisionBoundingBox(){
		return super.getCollisionBoundingBox();
	}

	@Override
	public boolean shouldRenderInPass(int pass){
		return pass == 1;
	}

	@Override
	public void tick(){

		super.tick();

		if(tickCount == 1 && level.isClientSide){
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_FORCEFIELD_AMBIENT, WizardrySounds.SPELLS, 0.5f, 1, true);
		}

		// New forcefield repulsion system:
		// Searches for all entities near the forcefield and determines where they will be next tick.
		// If they will be inside the forcefield next tick, sets their position and velocity such that they appear to
		// bounce off the forcefield and creates impact particle effects and sounds where they hit it

		List<Entity> targets = EntityUtils.getEntitiesWithinRadius(radius + SEARCH_BORDER_SIZE, getX(), getY(), getZ(), world, Entity.class);

		targets.remove(this);
		targets.removeIf(t -> t instanceof EntityXPOrb); // Gets annoying since they're attracted to the player

		// Ring of the defender allows players to shoot through their own forcefields
		if(getCaster() instanceof Player && ItemArtefact.isArtefactActive((Player)getCaster(),
				WizardryItems.ring_defender)){
			targets.removeIf(t -> t instanceof EntityMagicArrow && !this.isValidTarget(((EntityMagicArrow)t).getCaster())
								|| t instanceof EntityThrowable && !this.isValidTarget(((EntityThrowable)t).getThrower())
								|| t instanceof Arrow && !this.isValidTarget(((Arrow)t).getOwner()));
		}

		for(Entity target : targets){

			if(this.isValidTarget(target)){

				Vec3 currentPos = Arrays.stream(GeometryUtils.getVertices(target.getBoundingBox()))
						.min(Comparator.comparingDouble(v -> v.distanceTo(this.position())))
						.orElse(target.position()); // This will never happen, it's just here to make the compiler happy

				double currentDistance = target.getDistance(this);

				// Estimate the target's position next tick
				// We have to assume the same vertex is closest or the velocity will be wrong
				Vec3 nextTickPos = currentPos.add(target.motionX, target.motionY, target.motionZ);
				double nextTickDistance = nextTickPos.distanceTo(this.position());

				boolean flag;

				if(EntityUtils.isLiving(target)){
					// Non-allied living entities shouldn't be inside at all
					flag = nextTickDistance <= radius;
				}else{
					// Non-living entities will bounce off if they hit the forcefield within the next tick...
					flag = (currentDistance > radius && nextTickDistance <= radius) // ...from the outside...
							|| (currentDistance < radius && nextTickDistance >= radius); // ...or from the inside
				}

				if(flag){

					// Ring of interdiction
					if(getCaster() instanceof Player && ItemArtefact.isArtefactActive((Player)getCaster(),
							WizardryItems.ring_interdiction) && EntityUtils.isLiving(target)){
						target.hurt(MagicDamage.causeIndirectMagicDamage(this, getCaster(),
								MagicDamage.DamageType.MAGIC), 1);
					}

					Vec3 targetRelativePos = currentPos.subtract(this.position());

					double nudgeVelocity = this.contains(target) ? -0.1 : 0.1;
					if(EntityUtils.isLiving(target)) nudgeVelocity = 0.25;
					Vec3 extraVelocity = targetRelativePos.normalize().scale(nudgeVelocity);

					// ...make it bounce off!
					target.motionX = target.motionX * -BOUNCINESS + extraVelocity.x;
					target.motionY = target.motionY * -BOUNCINESS + extraVelocity.y;
					target.motionZ = target.motionZ * -BOUNCINESS + extraVelocity.z;

					// Prevents the forcefield bouncing things into the floor
					if(target.onGround && target.motionY < 0) target.motionY = 0.1;

					// How far the target needs to move towards the centre (negative means away from the centre)
					double distanceTowardsCentre = -(targetRelativePos.length() - radius) - (radius - nextTickDistance);
					Vec3 targetNewPos = target.position().add(targetRelativePos.normalize().scale(distanceTowardsCentre));
					target.setPosition(targetNewPos.x, targetNewPos.y, targetNewPos.z);

					world.playSound(target.getX(), target.getY(), target.getZ(), WizardrySounds.ENTITY_FORCEFIELD_DEFLECT,
							WizardrySounds.SPELLS, 0.3f, 1.3f, false);

					if(!level.isClientSide){
						// Player motion is handled on that player's client so needs packets
						if(target instanceof ServerPlayer){
							((ServerPlayer)target).connection.sendPacket(new SPacketEntityVelocity(target));
						}

						if (this.tickCount % 5 == 0) {
							this.lifetime = (int) (lifetime * 0.99);
						}

					}else{

						Vec3 relativeImpactPos = targetRelativePos.normalize().scale(radius);

						float yaw = (float)Math.atan2(relativeImpactPos.x, -relativeImpactPos.z);
						float pitch = (float)Math.asin(relativeImpactPos.y/ radius);

						ParticleBuilder.create(Type.FLASH).pos(this.position().add(relativeImpactPos))
						.time(6).face((float)(yaw * 180/Math.PI), (float)(pitch * 180/Math.PI))
						.clr(0.9f, 0.95f, 1).spawn(world);

						for(int i = 0; i < 12; i++){

							float yaw1 = yaw + 0.3f * (random.nextFloat() - 0.5f) - (float)Math.PI/2;
							float pitch1 = pitch + 0.3f * (random.nextFloat() - 0.5f);

							float brightness = random.nextFloat();

							double r = radius + 0.05;
							double x = this.getX() + r * Mth.cos(yaw1) * Mth.cos(pitch1);
							double y = this.getY() + r * Mth.sin(pitch1);
							double z = this.getZ() + r * Mth.sin(yaw1) * Mth.cos(pitch1);

							ParticleBuilder.create(Type.DUST).pos(x, y, z).time(6 + random.nextInt(6))
							.face((float)(yaw1 * 180/Math.PI) + 90, (float)(pitch1 * 180/Math.PI)).scale(1.5f)
							.clr(0.7f + 0.3f * brightness, 0.85f + 0.15f * brightness, 1).spawn(world);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean contains(Vec3 vec){
		return vec.distanceTo(this.position()) < radius; // The surface counts as outside
	}

	/** Returns true if the given bounding box is completely inside this forcefield (the surface counts as outside). */
	public boolean contains(AABB box){
		return Arrays.stream(GeometryUtils.getVertices(box)).allMatch(this::contains);
	}

	/** Returns true if the given entity is completely inside this forcefield (the surface counts as outside). */
	public boolean contains(Entity entity){
		return contains(entity.getBoundingBox());
	}

	@Override
	public Vec3 calculateIntercept(Vec3 origin, Vec3 endpoint, float fuzziness){

		// We want the intercept between the line and a sphere
		// First we need to find the point where the line is closest to the centre
		// Then we can use a bit of geometry to find the intercept

		// Find the closest point to the centre
		// http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
		Vec3 line = endpoint.subtract(origin);
		double t = -origin.subtract(this.position()).dotProduct(line) / line.lengthSquared();
		Vec3 closestPoint = origin.add(line.scale(t));
		// Now calculate the distance from that point to the centre (squared because that's all we need)
		double dsquared = closestPoint.squareDistanceTo(this.position());
		double rsquared = Math.pow(radius + fuzziness, 2);
		// If the minimum distance is outside the radius (plus fuzziness) then there is no intercept
		if(dsquared > rsquared) return null;
		// Now do pythagoras to find the other side of the triangle, which is the distance along the line from
		// the closest point to the edge of the sphere, and go that far back towards the origin - and that's it!
		return closestPoint.subtract(line.normalize().scale(Math.sqrt(rsquared - dsquared)));
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeFloat(getRadius());
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		setRadius(data.readFloat());
	}

	@Override
	protected void writeEntityToNBT(CompoundTag nbt){
		super.writeEntityToNBT(nbt);
		nbt.setFloat("radius", radius);
	}

	@Override
	protected void readEntityFromNBT(CompoundTag nbt){
		super.readEntityFromNBT(nbt);
		radius = nbt.getFloat("radius");
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

	// Prevents any kind of interactions or attacks through the forcefield
	// We may as well include projectile damage for this, then it will act as a failsafe

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){

		if(event.getSource().getEntity() instanceof Player && event.getSource().isProjectile()
				&& ItemArtefact.isArtefactActive((Player)event.getSource().getEntity(), WizardryItems.ring_defender)){
				return; // Players wearing a ring of the defender can shoot stuff as normal, so don't cancel the event
		}

		if(!event.getSource().isUnblockable() && event.getSource().getEntity() != null && event.getEntity() != null
			&& !(event.getSource().getDirectEntity() instanceof EntityForcefield)){ // If the damage was from a forcefield that's ok
			// This condition will be false if both entities are outside a forcefield or both are in the same one
			if(getSurroundingForcefield(event.getEntity()) != getSurroundingForcefield(event.getSource().getEntity())){
				event.setCanceled(true);
			}
		}
	}

	@Nullable
	private static EntityForcefield getSurroundingForcefield(Level world, Vec3 vec){

		double searchRadius = 20;

		List<EntityForcefield> forcefields = EntityUtils.getEntitiesWithinRadius(searchRadius, vec.x,
				vec.y, vec.z, world, EntityForcefield.class);

		forcefields.removeIf(f -> !f.contains(vec));
		// There should only be one left at this point since we now have anti-overlap, but commands might bypass that
		return forcefields.stream().min(Comparator.comparingDouble(f -> vec.squareDistanceTo(f.position())))
				.orElse(null);
	}

	@Nullable
	private static EntityForcefield getSurroundingForcefield(Level world, AABB box, Vec3 vec){

		double searchRadius = 20;

		List<EntityForcefield> forcefields = EntityUtils.getEntitiesWithinRadius(searchRadius, vec.x,
				vec.y, vec.z, world, EntityForcefield.class);

		forcefields.removeIf(f -> !f.contains(box));
		// There should only be one left at this point since we now have anti-overlap, but commands might bypass that
		return forcefields.stream().min(Comparator.comparingDouble(f -> vec.squareDistanceTo(f.position())))
				.orElse(null);
	}

	@Nullable
	private static EntityForcefield getSurroundingForcefield(Entity entity){
		return getSurroundingForcefield(entity.world, entity.getBoundingBox(), entity.position());
	}

	@SubscribeEvent
	public static void onPlayerInteractEvent(PlayerInteractEvent event){

		if(!event.isCancelable()) return; // We don't care about clicking empty space

		// For some reason block bounding boxes are relative whereas entity bounding boxes are absolute
		AABB box = event.getWorld().getBlockState(event.getPos()).getBoundingBox(event.getWorld(), event.getPos())
				.offset(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());

		if(event instanceof PlayerInteractEvent.EntityInteract){
			box = ((PlayerInteractEvent.EntityInteract)event).getTarget().getBoundingBox();
		}else if(event instanceof PlayerInteractEvent.EntityInteractSpecific){
			box = ((PlayerInteractEvent.EntityInteractSpecific)event).getTarget().getBoundingBox();
		}

		// If the player is trying to interact across a forcefield boundary, cancel the event
		// The most pragmatic solution here is to use the centres - it's not perfect, but it's simple!
		if(getSurroundingForcefield(event.getWorld(), GeometryUtils.getCentre(box))
				!= getSurroundingForcefield(event.getWorld(), event.getEntity().position())){
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onExplosionEvent(ExplosionEvent event){

		EntityForcefield forcefield = getSurroundingForcefield(event.getWorld(), event.getExplosion().getPosition());
		// Not a particularly efficient way of doing it but explosions are laggy anyway, and the code is neat :P
		event.getExplosion().getAffectedBlockPositions().removeIf(p -> getSurroundingForcefield(event.getWorld(),
				new Vec3(p).add(0.5, 0.5, 0.5)) != forcefield);

		event.getExplosion().getPlayerKnockbackMap().keySet().removeIf(p -> getSurroundingForcefield(p) != forcefield);
	}

}
