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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EntityIceShard extends EntityMagicArrow {

	/** Creates a new ice shard in the given world. */
	public EntityIceShard(Level world){
		this(WizardryEntities.ICE_SHARD.get(), world);
	}
	
	public EntityIceShard(EntityType<? extends EntityMagicArrow> type, Level world){
		super(type, world);
	}

	@Override public double getDamage(){ return Spells.ICE_SHARD.getProperty(Spell.DAMAGE).floatValue(); }

	@Override public int getLifetime(){ return -1; }

	@Override public DamageType getDamageType(){ return DamageType.FROST; }

	@Override public boolean doGravity(){ return true; }

	@Override public boolean doDeceleration(){ return true; }

	@Override public boolean displayFireAnimation(){ return false; }

	@Override
	public void onEntityHit(LivingEntity entityHit){

		// Adds a freeze effect to the target.
		if(!MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
			entityHit.addEffect(new MobEffectInstance(WizardryPotions.FROST.get(),
					Spells.ICE_SHARD.getProperty(Spell.EFFECT_DURATION).intValue(),
					Spells.ICE_SHARD.getProperty(Spell.EFFECT_STRENGTH).intValue()));

		this.playSound(WizardrySounds.ENTITY_ICE_SHARD_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void onBlockHit(BlockHitResult hit){
		
		// Adds a particle effect when the ice shard hits a block.
		if(this.level.isClientSide){
			// Gets a position slightly away from the block hit so the particle doesn't get cut in half by the block face
			Vec3 vec = hit.getLocation().add(new Vec3(hit.getDirection().step()).scale(0.15));
			ParticleBuilder.create(Type.FLASH).pos(vec).clr(0.75f, 1, 1).spawn(level);
			
			for(int j = 0; j < 10; j++){
				ParticleBuilder.create(Type.ICE, this.random, this.getX(), this.getY(), this.getZ(), 0.5, true)
				.time(20 + random.nextInt(10)).gravity(true).spawn(level);
			}
		}
		// Parameters for sound: sound event name, volume, pitch.
		this.playSound(WizardrySounds.ENTITY_ICE_SHARD_SMASH, 1.0F, random.nextFloat() * 0.4F + 1.2F);

	}

	@Override
	protected void defineSynchedData(){}

}