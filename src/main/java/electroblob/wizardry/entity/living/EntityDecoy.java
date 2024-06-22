package electroblob.wizardry.entity.living;

import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.EntityAILookIdle;
import net.minecraft.world.entity.ai.EntityAISwimming;
import net.minecraft.world.entity.ai.EntityAIWander;
import net.minecraft.world.entity.ai.EntityAIWatchClosest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityDecoy extends EntitySummonedCreature {

	// TODO: These guys need sounds!

	/** Creates a new decoy in the given world. */
	public EntityDecoy(Level world){
		super(world);
	}
	
	@Override
	public void setCaster(LivingEntity caster){
		super.setCaster(caster);
		this.setCustomNameVisible(caster instanceof Player);
	}

	@Override
	protected void registerGoals(){
		// Decoys just wander around aimlessly, watching anything living.
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, LivingEntity.class, 6.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	@Override
	public void onDespawn(){
		super.onDespawn();
		
		if(level.isClientSide){
			for(int i = 0; i < 20; i++){
				ParticleBuilder.create(Type.DUST)
				.pos(this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), this.getY()
						+ this.random.nextDouble() * this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth())
				.time(40)
				.clr(0.2f, 1.0f, 0.8f)
				.shaded(true)
				.spawn(level);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return 0xffc600;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source){
		return true;
	}

	@Override
	public boolean isShiftKeyDown(){
		return false;
	}

	@Override
	public void tick(){
		super.tick();
		if(this.getCaster() == null || !this.getCaster().isAlive()){
			this.discard();
			this.onDespawn();
		}
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}

	@Override
	public Component getDisplayName(){
		if(getCaster() instanceof Player){
			return this.getCaster().getDisplayName();
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		return getCaster() instanceof Player;
	}

	@Override
	public boolean hasRangedAttack(){
		return false;
	}

	// TESTME: Why was this here? It gets done in ISummonedCreature anyway
//	@Override
//	public void writeSpawnData(ByteBuf data){
//		super.writeSpawnData(data);
//		if(this.getCaster() != null) data.writeInt(this.getCaster().getEntityId());
//	}
//
//	@Override
//	public void readSpawnData(ByteBuf data){
//		super.readSpawnData(data);
//		if(!data.isReadable()) return;
//		this.setCasterReference(
//				new WeakReference<EntityLivingBase>((EntityLivingBase)this.level.getEntityByID(data.readInt())));
//	}

}
