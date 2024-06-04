package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class EntityFlamecatcherArrow extends EntityMagicArrow {

	public static final float SPEED = 3;

	/** Creates a new magic missile in the given world. */
	public EntityFlamecatcherArrow(Level world){
		super(world);
	}

	@Override public double getDamage(){ return Spells.flamecatcher.getProperty(Spell.DAMAGE).floatValue(); }

	@Override public int getLifetime(){ return (int)(Spells.flamecatcher.getProperty(Spell.RANGE).floatValue() / SPEED); }

	@Override public boolean doGravity(){ return false; } // Zero gravity arrows!

	@Override public boolean doDeceleration(){ return false; }

	@Override
	public void onEntityHit(LivingEntity entityHit){
		entityHit.setFire(Spells.flamecatcher.getProperty(Spell.BURN_DURATION).intValue());
		this.playSound(WizardrySounds.ENTITY_FLAMECATCHER_ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
		if(this.level.isClientSide) ParticleBuilder.create(Type.FLASH).pos(getX(), getY(), getZ()).clr(0xff6d00).spawn(world);
	}
	
	@Override
	public void onBlockHit(HitResult hit){
		if(this.level.isClientSide){
			// Gets a position slightly away from the block hit so the particle doesn't get cut in half by the block face
			Vec3 vec = hit.hitVec.add(new Vec3(hit.sideHit.getDirectionVec()).scale(0.15));
			ParticleBuilder.create(Type.FLASH).pos(vec).clr(0xff6d00).fade(0.85f, 0.5f, 0.8f).spawn(world);
		}
	}

	@Override
	public void tickInAir(){

		if(this.level.isClientSide){

			ParticleBuilder.create(Type.MAGIC_FIRE, rand, getX(), getY(), getZ(), 0.03, false)
					.time(20 + random.nextInt(10)).spawn(world);

			if(this.ticksExisted > 1){ // Don't spawn particles behind where it started!
				double x = getX() - motionX / 2;
				double y = getY() - motionY / 2;
				double z = getZ() - motionZ / 2;
				ParticleBuilder.create(Type.MAGIC_FIRE, rand, x, y, z, 0.03, false)
						.time(20 + random.nextInt(10)).spawn(world);
			}
		}
	}

	@Override
	protected void entityInit(){
		if(world != null && level.isClientSide){
			ParticleBuilder.create(Type.FLASH).entity(this).time(this.getLifetime()).scale(1.5f).clr(0xffb800).spawn(world);
		}
	}

}