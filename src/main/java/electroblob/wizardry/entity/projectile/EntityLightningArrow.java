package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityLightningArrow extends EntityMagicArrow {

	/** Creates a new lightning arrow in the given world. */
	public EntityLightningArrow(Level world){
		super(world);
	}

	@Override public double getDamage(){ return Spells.lightning_arrow.getProperty(Spell.DAMAGE).doubleValue(); }

	@Override public int getLifetime(){ return 20; }

	@Override public DamageType getDamageType(){ return DamageType.SHOCK; }

	@Override public boolean doGravity(){ return false; }

	@Override public boolean doDeceleration(){ return false; }

	@Override
	public void onEntityHit(LivingEntity entityHit){

		if(level.isClientSide){
			for(int j = 0; j < 8; j++){
				ParticleBuilder.create(Type.SPARK, rand, getX(), getY() + height / 2, getZ(), 1, false).spawn(world);
			}
		}
		
		this.playSound(WizardrySounds.ENTITY_LIGHTNING_ARROW_HIT, 1.0F, 1.0F);
	}
	
//	@Override
//	public void onBlockHit(RayTraceResult hit){
//		if(this.level.isClientSide){
//			Vec3d vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(WizardryUtilities.ANTI_Z_FIGHTING_OFFSET));
//			ParticleBuilder.create(Type.SCORCH).pos(vec).face(hit.sideHit).clr(0.4f, 0.8f, 1).scale(0.6f).spawn(world);
//		}
//	}

	@Override
	public void tickInAir(){
		if(level.isClientSide){
			ParticleBuilder.create(Type.SPARK).pos(getX(), getY(), getZ()).spawn(world);
		}
	}

	@Override
	protected void entityInit(){}

}