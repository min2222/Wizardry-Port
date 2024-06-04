package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Bubble extends SpellRay {

	public Bubble(){
		super("bubble", SpellActions.POINT, false);
		this.soundValues(0.5f, 1.1f, 0.2f);
		addProperties(DURATION);
	}
	
	@Override
	protected SoundEvent[] createSounds(){
		return this.createSoundsWithSuffixes("shoot", "splash");
	}
 
	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			
			if(!world.isClientSide){
				// Deals a small amount damage so the target counts as being hit by the caster
				target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1);
				
				EntityBubble bubble = new EntityBubble(world);
				bubble.setPosition(target.getX(), target.getY(), target.getZ());
				bubble.setCaster(caster);
				bubble.lifetime = ((int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
				bubble.isDarkOrb = false;
				bubble.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				
				world.spawnEntity(bubble);
				target.startRiding(bubble);
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(ParticleTypes.WATER_SPLASH, x, y, z, 0, 0, 0);
		ParticleBuilder.create(Type.MAGIC_BUBBLE).pos(x, y, z).spawn(world);
	}

}
