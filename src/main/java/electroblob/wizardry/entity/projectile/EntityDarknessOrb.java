package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityDarknessOrb extends EntityMagicProjectile {
	
	public EntityDarknessOrb(EntityType<? extends EntityMagicProjectile> type, Level world){
		super(type, world);
	}
	
	public EntityDarknessOrb(Level world){
		this(WizardryEntities.DARKNESS_ORB.get(), world);
	}

	@Override
	protected void onHit(HitResult rayTrace){
		
		Entity target = rayTrace.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) rayTrace).getEntity() : null;

		if(target != null && !MagicDamage.isEntityImmune(DamageType.WITHER, target)){

			float damage = Spells.darkness_orb.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			target.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.WITHER).setProjectile(),
					damage);

			if(target instanceof LivingEntity && !MagicDamage.isEntityImmune(DamageType.WITHER, target))
				((LivingEntity)target).addEffect(new MobEffectInstance(MobEffects.WITHER,
						Spells.darkness_orb.getProperty(Spell.EFFECT_DURATION).intValue(),
						Spells.darkness_orb.getProperty(Spell.EFFECT_STRENGTH).intValue()));

			this.playSound(WizardrySounds.ENTITY_DARKNESS_ORB_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
		}

		this.discard();
	}

	public void tick(){

		super.tick();

		if(level.isClientSide){
			
			float brightness = random.nextFloat() * 0.2f;
			
			ParticleBuilder.create(Type.SPARKLE, this).time(20 + random.nextInt(10))
			.clr(brightness, 0.0f, brightness).spawn(level);
			
			ParticleBuilder.create(Type.DARK_MAGIC, this).clr(0.1f, 0.0f, 0.0f).spawn(level);
		}

		// Cancels out the slowdown effect in EntityThrowable
		this.setDeltaMovement(this.getDeltaMovement().x / 0.99, this.getDeltaMovement().y / 0.99, this.getDeltaMovement().z / 0.99);
	}

	@Override
	public boolean isNoGravity(){
		return true;
	}

	@Override
	public int getLifetime(){
		return 60;
	}
}
