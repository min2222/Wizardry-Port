package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.List;

public class Detonate extends SpellRay {

	// More descriptive/accurate than just "damage"
	public static final String MAX_DAMAGE = "max_damage";

	public Detonate(){
		super("detonate", SpellActions.POINT, false);
		this.soundValues(4, 0.7f, 0.14f);
		this.ignoreLivingEntities(true);
		addProperties(MAX_DAMAGE, BLAST_RADIUS);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(!world.isClientSide){
			
			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(getProperty(BLAST_RADIUS).doubleValue()
					* modifiers.get(WizardryItems.blast_upgrade), pos.getX(), pos.getY(), pos.getZ(), world);
			
			for(LivingEntity target : targets){
				target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
						// Damage decreases with distance but cannot be less than 0, naturally.
						Math.max(getProperty(MAX_DAMAGE).floatValue() - (float)target.getDistance(pos.getX() + 0.5,
								pos.getY() + 0.5, pos.getZ() + 0.5) * 4, 0) * modifiers.get(SpellModifiers.POTENCY));
			}
			
		}else{
			world.spawnParticle(ParticleTypes.EXPLOSION_HUGE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
		}
		
		return true;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
	}

}
