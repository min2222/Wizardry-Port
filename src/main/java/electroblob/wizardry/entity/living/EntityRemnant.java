package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.IEntityLivingData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EntityMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EntityRemnant extends EntityMob {

	/** Data parameter for the remnant's element. */
	private static final EntityDataSerializer<Integer> ELEMENT = SynchedEntityData.createKey(EntityRemnant.class, EntityDataSerializers.VARINT);
	/** Data parameter that tracks whether the remnant is currently attacking (charging). */
	private static final EntityDataSerializer<Boolean> ATTACKING = SynchedEntityData.createKey(EntityRemnant.class, EntityDataSerializers.BOOLEAN);

	private ResourceLocation lootTable;

	@Nullable
	private BlockPos boundOrigin;

	public EntityRemnant(Level world){
		super(world);
		this.setSize(0.8f, 0.8f);
		this.moveHelper = new EntityRemnant.AIMoveControl(this);
		this.experienceValue = 8;
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(ELEMENT, 1); // Default to fire
		this.dataManager.register(ATTACKING, false); // Default to fire
	}

	@Override
	protected void initEntityAI(){
		super.initEntityAI();
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityRemnant.AIChargeAttack());
		this.tasks.addTask(8, new EntityRemnant.AIMoveRandom());
		this.tasks.addTask(9, new EntityAIWatchClosest(this, Player.class, 3.0F, 1.0F));
		this.tasks.addTask(10, new EntityAIWatchClosest(this, Mob.class, 8.0F));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityRemnant.class));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, Player.class, true));
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(Attributes.MAX_HEALTH).setBaseValue(16);
		this.getEntityAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
	}

	@Nullable
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata){
		this.setElement(Element.values()[1 + random.nextInt(Element.values().length - 1)]); // Exclude MAGIC
		this.setBoundOrigin(this.blockPosition());
		return super.onInitialSpawn(difficulty, livingdata);
	}

	public Element getElement(){
		return Element.values()[this.dataManager.get(ELEMENT)];
	}

	public void setElement(Element element){
		this.dataManager.set(ELEMENT, element.ordinal());
		this.lootTable = new ResourceLocation(Wizardry.MODID, "entities/remnant/" + element.getName());
	}

	public boolean isAttacking(){
		return this.dataManager.get(ATTACKING);
	}

	public void setAttacking(boolean attacking){
		this.dataManager.set(ATTACKING, attacking);
	}

	@Nullable
	public BlockPos getBoundOrigin(){
		return this.boundOrigin;
	}

	public void setBoundOrigin(@Nullable BlockPos boundOriginIn){
		this.boundOrigin = boundOriginIn;
	}

	@Override
	protected boolean isValidLightLevel(){
		return true;
	}

	@Override
	public float getBlockPathWeight(BlockPos pos){
		// Goddamnit Minecraft, stop imposing obscure spawning restrictions
		return 1; // This won't affect pathfinding since remnants are flying mobs anyway
	}

	@Override
	protected float applyPotionDamageCalculations(DamageSource source, float damage){
		damage = super.applyPotionDamageCalculations(source, damage);
		if(source.isMagicDamage()) damage *= 0.25f; // Remnants are 75% resistant to magic damage
		return damage;
	}

	@Override
	public void onUpdate(){

		// Use the same trick as EntityVex to fly through stuff
		this.noClip = true;
		super.onUpdate();
		this.noClip = false;

		this.setNoGravity(true);

		if(level.isClientSide){

			Vec3 centre = this.getPositionVector().add(0, height/2, 0);

			int[] colours = BlockReceptacle.PARTICLE_COLOURS.get(this.getElement());

			if(random.nextInt(10) == 0){
				ParticleBuilder.create(ParticleBuilder.Type.FLASH).entity(this).pos(0, height/2, 0).scale(width).time(48).clr(colours[0]).spawn(world);
			}

			double r = width/3;

			double x = r * (random.nextDouble() * 2 - 1);
			double y = r * (random.nextDouble() * 2 - 1);
			double z = r * (random.nextDouble() * 2 - 1);

			if(this.deathTime > 0){
				// Spew out particles on death
				for(int i = 0; i < 8; i++){
					ParticleBuilder.create(ParticleBuilder.Type.DUST, rand, centre.x + x, centre.y + y, centre.z + z, 0.1, true)
							.time(12).clr(colours[1]).fade(colours[2]).spawn(world);
				}
			}else{
				ParticleBuilder.create(ParticleBuilder.Type.DUST).pos(centre.x + x, centre.y + y, centre.z + z)
						.vel(x * -0.03, 0.02, z * -0.03).time(24 + random.nextInt(8)).clr(colours[1]).fade(colours[2]).spawn(world);
			}
		}

	}

	@Override
	protected SoundEvent getAmbientSound(){
		return WizardrySounds.ENTITY_REMNANT_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_REMNANT_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_REMNANT_HURT;
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable(){
		return lootTable;
	}

	@Override
	public void readEntityFromNBT(CompoundTag nbt){
		super.readEntityFromNBT(nbt);
		this.setElement(Element.values()[nbt.getInt("Element")]);
		if(nbt.hasKey("BoundOrigin")) boundOrigin = NbtUtils.readBlockPos(nbt.getCompoundTag("BoundOrigin"));
	}

	@Override
	public void writeEntityToNBT(CompoundTag nbt){
		super.writeEntityToNBT(nbt);
		nbt.putInt("Element", this.getElement().ordinal());
		if(boundOrigin != null) nbt.setTag("BoundOrigin", NbtUtils.writeBlockPos(boundOrigin));
	}

	// AI classes (copied from EntityVex)

	class AIChargeAttack extends EntityAIBase {

		public AIChargeAttack(){
			this.setMutexBits(1);
		}

		@Override
		public boolean shouldExecute(){
			if(EntityRemnant.this.getAttackTarget() != null && !EntityRemnant.this.getMoveHelper().isUpdating() && EntityRemnant.this.random.nextInt(7) == 0){
				return EntityRemnant.this.distanceToSqr(EntityRemnant.this.getAttackTarget()) > 4.0D;
			}else{
				return false;
			}
		}

		@Override
		public boolean shouldContinueExecuting(){
			return EntityRemnant.this.getMoveHelper().isUpdating() && EntityRemnant.this.isAttacking() && EntityRemnant.this.getAttackTarget() != null && EntityRemnant.this.getAttackTarget().isEntityAlive();
		}

		@Override
		public void startExecuting(){
			LivingEntity entitylivingbase = EntityRemnant.this.getAttackTarget();
			if(entitylivingbase == null) return;
			Vec3 vec3d = entitylivingbase.getPositionEyes(1.0F);
			EntityRemnant.this.moveHelper.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
			EntityRemnant.this.setAttacking(true);
//			EntityRemnant.this.playSound(SoundEvents.ENTITY_VEX_CHARGE, 1.0F, 1.0F);
		}

		@Override
		public void resetTask(){
			EntityRemnant.this.setAttacking(false);
		}

		@Override
		public void updateTask(){

			LivingEntity entitylivingbase = EntityRemnant.this.getAttackTarget();

			if(entitylivingbase == null) return;

			if(EntityRemnant.this.getBoundingBox().intersects(entitylivingbase.getBoundingBox())){
				EntityRemnant.this.attackEntityAsMob(entitylivingbase);
				EntityRemnant.this.setAttacking(false);
			}else{
				double d0 = EntityRemnant.this.distanceToSqr(entitylivingbase);

				if(d0 < 9.0D){
					Vec3 vec3d = entitylivingbase.getPositionEyes(1.0F);
					EntityRemnant.this.moveHelper.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
				}
			}
		}
	}

	class AIMoveControl extends EntityMoveHelper {

		public AIMoveControl(EntityRemnant host){
			super(host);
		}

		@Override
		public void onUpdateMoveHelper(){

			if(this.action == EntityMoveHelper.Action.MOVE_TO){

				double d0 = this.getX() - EntityRemnant.this.getX();
				double d1 = this.getY() - EntityRemnant.this.getY();
				double d2 = this.getZ() - EntityRemnant.this.getZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				d3 = Math.sqrt(d3);

				if(d3 < EntityRemnant.this.getBoundingBox().getAverageEdgeLength()){

					this.action = EntityMoveHelper.Action.WAIT;
					EntityRemnant.this.motionX *= 0.5D;
					EntityRemnant.this.motionY *= 0.5D;
					EntityRemnant.this.motionZ *= 0.5D;

				}else{

					EntityRemnant.this.motionX += d0 / d3 * 0.05D * this.speed;
					EntityRemnant.this.motionY += d1 / d3 * 0.05D * this.speed;
					EntityRemnant.this.motionZ += d2 / d3 * 0.05D * this.speed;

					if(EntityRemnant.this.getAttackTarget() == null){
						EntityRemnant.this.rotationYaw = -((float) Mth.atan2(EntityRemnant.this.motionX, EntityRemnant.this.motionZ)) * (180F / (float)Math.PI);
					}else{
						double d4 = EntityRemnant.this.getAttackTarget().getX() - EntityRemnant.this.getX();
						double d5 = EntityRemnant.this.getAttackTarget().getZ() - EntityRemnant.this.getZ();
						EntityRemnant.this.rotationYaw = -((float) Mth.atan2(d4, d5)) * (180F / (float)Math.PI);
					}

					EntityRemnant.this.renderYawOffset = EntityRemnant.this.rotationYaw;
				}
			}
		}
	}

	class AIMoveRandom extends EntityAIBase {

		public AIMoveRandom(){
			this.setMutexBits(1);
		}

		@Override
		public boolean shouldExecute(){
			return !EntityRemnant.this.getMoveHelper().isUpdating() && EntityRemnant.this.random.nextInt(7) == 0;
		}

		@Override
		public boolean shouldContinueExecuting(){
			return false;
		}

		@Override
		public void updateTask(){

			BlockPos blockpos = EntityRemnant.this.getBoundOrigin();

			if(blockpos == null){
				blockpos = new BlockPos(EntityRemnant.this);
			}

			for(int i = 0; i < 3; ++i){
				BlockPos blockpos1 = blockpos.add(EntityRemnant.this.random.nextInt(15) - 7, EntityRemnant.this.random.nextInt(11) - 5, EntityRemnant.this.random.nextInt(15) - 7);

				if(EntityRemnant.this.world.isAirBlock(blockpos1)){
					EntityRemnant.this.moveHelper.setMoveTo((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 0.25D);

					if(EntityRemnant.this.getAttackTarget() == null){
						EntityRemnant.this.getLookHelper().setLookPosition((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
					}

					break;
				}
			}
		}
	}

}
