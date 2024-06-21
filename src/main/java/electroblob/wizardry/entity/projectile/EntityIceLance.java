package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityIceLance extends EntityMagicArrow {

	/** Creates a new ice lance in the given world. */
	public EntityIceLance(Level world){
		this(WizardryEntities.ICE_LANCE.get(), world);
		this.setKnockbackStrength(1);
	}
	
	public EntityIceLance(EntityType<? extends EntityMagicArrow> type, Level world){
		super(type, world);
		this.setKnockbackStrength(1);
	}

	@Override public double getDamage(){ return Spells.ICE_LANCE.getProperty(Spell.DAMAGE).floatValue(); }

	@Override public int getLifetime(){ return -1; }

	@Override public DamageType getDamageType(){ return DamageType.FROST; }

	@Override public boolean doGravity(){ return true; }

	@Override public boolean doDeceleration(){ return true; }

	@Override public boolean doOverpenetration(){ return true; }

	@Override public boolean displayFireAnimation(){ return false; }

	@Override
	public void onEntityHit(LivingEntity entityHit){

		// Adds a freeze effect to the target.
		if(!MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
			entityHit.addEffect(new MobEffectInstance(WizardryPotions.FROST.get(),
					Spells.ICE_LANCE.getProperty(Spell.EFFECT_DURATION).intValue(),
					Spells.ICE_LANCE.getProperty(Spell.EFFECT_STRENGTH).intValue()));

		this.playSound(WizardrySounds.ENTITY_ICE_LANCE_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void onBlockHit(HitResult hit){
		// Adds a particle effect when the ice lance hits a block.
		if(this.level.isClientSide){
			for(int j = 0; j < 10; j++){
				ParticleBuilder.create(Type.ICE, this.random, this.getX(), this.getY(), this.getZ(), 0.5, true)
				.time(20 + random.nextInt(10)).gravity(true).spawn(level);
			}
		}
		
		this.playSound(WizardrySounds.ENTITY_ICE_LANCE_SMASH, 1.0F, random.nextFloat() * 0.4F + 1.2F);

	}

	@Override
	protected void defineSynchedData(){}

}