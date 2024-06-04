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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Entrapment extends SpellRay {

	public static final String DAMAGE_INTERVAL = "damage_interval";

	public Entrapment(){
		super("entrapment", SpellActions.POINT, false);
		this.soundValues(1, 0.85f, 0.3f);
		addProperties(EFFECT_DURATION, DAMAGE_INTERVAL);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			
			if(!world.isRemote){
				// Deals a small amount damage so the target counts as being hit by the caster
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1);
				
				EntityBubble bubble = new EntityBubble(world);
				bubble.setPosition(target.posX, target.posY, target.posZ);
				bubble.setCaster(caster);
				bubble.lifetime = ((int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
				bubble.isDarkOrb = true;
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
		world.spawnParticle(ParticleTypes.PORTAL, x, y - 0.5, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0, 0).spawn(world);
	}

}
