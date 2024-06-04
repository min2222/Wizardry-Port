package electroblob.wizardry.entity.living;

import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
		this.setAlwaysRenderNameTag(caster instanceof Player);
	}

	@Override
	protected void initEntityAI(){
		// Decoys just wander around aimlessly, watching anything living.
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(2, new EntityAIWatchClosest(this, LivingEntity.class, 6.0F));
		this.tasks.addTask(3, new EntityAILookIdle(this));
	}

	@Override
	public void onDespawn(){
		super.onDespawn();
		
		if(level.isClientSide){
			for(int i = 0; i < 20; i++){
				ParticleBuilder.create(Type.DUST)
				.pos(this.getX() + (this.random.nextDouble() - 0.5) * this.width, this.getY()
						+ this.random.nextDouble() * this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * this.width)
				.time(40)
				.clr(0.2f, 1.0f, 0.8f)
				.shaded(true)
				.spawn(world);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return 0xffc600;
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source){
		return true;
	}

	@Override
	public boolean isShiftKeyDown(){
		return false;
	}

	@Override
	public void tick(){
		super.tick();
		if(this.getCaster() == null || this.getCaster().isDead){
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
