package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class EntitySpark extends EntityMagicProjectile {

	public EntitySpark(Level world){
		super(world);
	}

	@Override
	protected void onImpact(HitResult rayTrace){
		
		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){

			float damage = Spells.homing_spark.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
			entityHit.hurt(MagicDamage.causeIndirectMagicDamage(this, this.getThrower(),
					DamageType.SHOCK), damage);

		}

		this.playSound(WizardrySounds.ENTITY_HOMING_SPARK_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

		// Particle effect
		if(level.isClientSide){
			for(int i = 0; i < 8; i++){
				double x = this.getX() + random.nextDouble() - 0.5;
				double y = this.getY() + this.getBbHeight() / 2 + random.nextDouble() - 0.5;
				double z = this.getZ() + random.nextDouble() - 0.5;
				ParticleBuilder.create(Type.SPARK).pos(x, y, z).spawn(world);
			}
		}

		this.discard();
	}

	@Override
	public float getSeekingStrength(){
		return Spells.homing_spark.getProperty(Spell.SEEKING_STRENGTH).floatValue();
	}

	@Override
	public int getLifetime(){
		return 50;
	}

	@Override
	public boolean hasNoGravity(){
		return true;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}
}
