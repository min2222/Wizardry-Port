package electroblob.wizardry.entity.construct;

import java.lang.ref.WeakReference;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Entrapment;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EntityBubble extends EntityMagicConstruct {

	public boolean isDarkOrb;

	private WeakReference<LivingEntity> rider;

	public EntityBubble(Level world){
		this(WizardryEntities.BUBBLE.get(), world);
	}
	
	public EntityBubble(EntityType<? extends EntityMagicConstruct> type, Level world){
		super(type, world);
	}

	@Override
	public double getPassengersRidingOffset(){
		return 0.1;
	}

	@Override
	public boolean shouldRiderSit(){
		return false;
	}

	public void tick(){

		super.tick();

		// Synchronises the rider field
		if((this.rider == null || this.rider.get() == null)
				&& EntityUtils.getRider(this) instanceof LivingEntity
				&& EntityUtils.getRider(this).isAlive()){
			this.rider = new WeakReference<>((LivingEntity)EntityUtils.getRider(this));
		}

		// Prevents dismounting
		if(EntityUtils.getRider(this) == null && this.rider != null && this.rider.get() != null
				&& this.rider.get().isAlive()){
			this.rider.get().startRiding(this);
		}

		// Stops the bubble bursting instantly.
		if(this.tickCount < 1 && !isDarkOrb) ((LivingEntity)EntityUtils.getRider(this)).hurtTime = 0;

		this.move(MoverType.SELF, new Vec3(0, 0.03, 0));

		if(isDarkOrb){

			if(EntityUtils.getRider(this) != null
					&& EntityUtils.getRider(this).tickCount % Spells.ENTRAPMENT.getProperty(Entrapment.DAMAGE_INTERVAL).intValue() == 0){
				if(this.getCaster() != null){
					EntityUtils.getRider(this).hurt(
							MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.MAGIC),
							1 * damageMultiplier);
				}else{
					EntityUtils.getRider(this).hurt(DamageSource.MAGIC, 1 * damageMultiplier);
				}
			}

			for(int i = 0; i < 5; i++){
				this.level.addParticle(ParticleTypes.PORTAL,
						this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(),
						this.getY() + this.random.nextDouble() * (double)this.getBbHeight() + 0.5d,
						this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(),
						(this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(),
						(this.random.nextDouble() - 0.5D) * 2.0D);
			}
			if(lifetime - this.tickCount == 75){
				this.playSound(WizardrySounds.ENTITY_ENTRAPMENT_VANISH, 1.5f, 1.0f);
			}else if(this.tickCount % 100 == 1 && this.tickCount < 150){
				this.playSound(WizardrySounds.ENTITY_ENTRAPMENT_AMBIENT, 1.5f, 1.0f);
			}
		}

		// Bubble bursts if the entity is hurt (see event handler) or killed, or if the bubble has existed for more than
		// 10 seconds.
		if(EntityUtils.getRider(this) == null && this.tickCount > 1){
			if(!this.isDarkOrb) this.playSound(WizardrySounds.ENTITY_BUBBLE_POP, 1.5f, 1.0f);
			this.discard();
		}
	}

	@Override
	public void despawn(){
		if(EntityUtils.getRider(this) != null){
			((LivingEntity)EntityUtils.getRider(this)).stopRiding();
		}
		if(!this.isDarkOrb) this.playSound(WizardrySounds.ENTITY_BUBBLE_POP, 1.5f, 1.0f);
		super.despawn();
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		isDarkOrb = nbttagcompound.getBoolean("isDarkOrb");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putBoolean("isDarkOrb", isDarkOrb);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		super.writeSpawnData(data);
		data.writeBoolean(this.isDarkOrb);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){
		super.readSpawnData(data);
		this.isDarkOrb = data.readBoolean();
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		// Bursts bubble when the creature inside takes damage
		if(event.getEntity().getVehicle() instanceof EntityBubble
				&& !((EntityBubble)event.getEntity().getVehicle()).isDarkOrb){
			event.getEntity().getVehicle().playSound(WizardrySounds.ENTITY_BUBBLE_POP, 1.5f, 1.0f);
			event.getEntity().getVehicle().discard();
		}
	}

}
