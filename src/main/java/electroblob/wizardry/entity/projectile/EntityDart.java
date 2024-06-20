package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class EntityDart extends EntityMagicArrow {
	
	/** Creates a new dart in the given world. */
	public EntityDart(Level world){
		super(WizardryEntities.DART.get(), world);
	}

	public EntityDart(EntityType<? extends EntityMagicArrow> type, Level world){
		super(type, world);
	}

	@Override public double getDamage(){ return Spells.DART.getProperty(Spell.DAMAGE).doubleValue(); }

	@Override public boolean doGravity(){ return true; }

	@Override public boolean doDeceleration(){ return true; }

	@Override
	public void onEntityHit(LivingEntity entityHit){
		// Adds a weakness effect to the target.
		entityHit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, Spells.DART.getProperty(Spell.EFFECT_DURATION).intValue(),
				Spells.DART.getProperty(Spell.EFFECT_STRENGTH).intValue(), false, false));
		this.playSound(WizardrySounds.ENTITY_DART_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void onBlockHit(HitResult hit){
		this.playSound(WizardrySounds.ENTITY_DART_HIT_BLOCK, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void tickInAir(){
		if(this.level.isClientSide){
			ParticleBuilder.create(Type.LEAF, this).time(10 + random.nextInt(5)).spawn(level);
		}
	}

	// Replicates the original behaviour of staying stuck in block for a few seconds before disappearing.
	@Override
	public void tickInGround(){
		if(this.ticksInGround > 60){
			this.discard();
		}
	}

	@Override
	protected void defineSynchedData(){}

	@Override
	public int getLifetime(){
		return -1;
	}
}