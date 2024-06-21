package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EntityMagicMissile extends EntityMagicArrow {

	/** Creates a new magic missile in the given world. */
	public EntityMagicMissile(Level world){
		this(WizardryEntities.MAGIC_MISSILE.get(), world);
	}
	
	public EntityMagicMissile(EntityType<? extends EntityMagicArrow> type, Level world){
		super(type, world);
	}

	@Override public double getDamage(){ return Spells.MAGIC_MISSILE.getProperty(Spell.DAMAGE).floatValue(); }

	@Override public int getLifetime(){ return 12; }

	@Override public boolean doGravity(){ return false; }

	@Override public boolean doDeceleration(){ return false; }

	@Override
	public void onEntityHit(LivingEntity entityHit){
		this.playSound(WizardrySounds.ENTITY_MAGIC_MISSILE_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
		if(this.level.isClientSide) ParticleBuilder.create(Type.FLASH).pos(getX(), getY(), getZ()).clr(1, 1, 0.65f).spawn(level);
	}
	
	@Override
	public void onBlockHit(BlockHitResult hit){
		if(this.level.isClientSide){
			// Gets a position slightly away from the block hit so the particle doesn't get cut in half by the block face
			Vec3 vec = hit.getLocation().add(new Vec3(hit.getDirection().step()).scale(0.15));
			ParticleBuilder.create(Type.FLASH).pos(vec).clr(1, 1, 0.65f).fade(0.85f, 0.5f, 0.8f).spawn(level);
		}
	}

	@Override
	public void tickInAir(){

		if(this.level.isClientSide){

			if(Wizardry.tisTheSeason){

				ParticleBuilder.create(Type.SPARKLE, random, getX(), getY(), getZ(), 0.03, true).clr(0.8f, 0.15f, 0.15f)
						.time(20 + random.nextInt(10)).spawn(level);

				ParticleBuilder.create(Type.SNOW).pos(getX(), getY(), getZ()).spawn(level);

				if(this.tickCount > 1){ // Don't spawn particles behind where it started!
					double x = getX() - getDeltaMovement().x / 2;
					double y = getY() - getDeltaMovement().y / 2;
					double z = getZ() - getDeltaMovement().z / 2;
					ParticleBuilder.create(Type.SPARKLE, random, x, y, z, 0.03, true).clr(0.15f, 0.7f, 0.15f)
							.time(20 + random.nextInt(10)).spawn(level);
				}

			}else{

				ParticleBuilder.create(Type.SPARKLE, random, getX(), getY(), getZ(), 0.03, true).clr(1, 1, 0.65f).fade(0.7f, 0, 1)
						.time(20 + random.nextInt(10)).spawn(level);

				if(this.tickCount > 1){ // Don't spawn particles behind where it started!
					double x = getX() - getDeltaMovement().x / 2;
					double y = getY() - getDeltaMovement().y / 2;
					double z = getZ() - getDeltaMovement().z / 2;
					ParticleBuilder.create(Type.SPARKLE, random, x, y, z, 0.03, true).clr(1, 1, 0.65f).fade(0.7f, 0, 1)
							.time(20 + random.nextInt(10)).spawn(level);
				}
			}
		}
	}

	@Override
	protected void defineSynchedData(){ }

}