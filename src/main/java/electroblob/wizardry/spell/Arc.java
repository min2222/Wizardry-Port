package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class Arc extends SpellRay {

	public Arc(){
		super("arc", SpellActions.POINT, false);
		this.aimAssist(0.6f);
		this.soundValues(1, 1.7f, 0.2f);
		this.addProperties(DAMAGE);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
		
			if(level.isClientSide){
				// Rather neatly, the entity can be set here and if it's null nothing will happen.
				ParticleBuilder.create(Type.LIGHTNING).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
				ParticleBuilder.spawnShockParticles(world, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ());
			}
	
			// This is a lot neater than it was, thanks to the damage type system.
			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!level.isClientSide && caster instanceof Player) ((Player)caster).sendStatusMessage(
						Component.translatable("spell.resist",
						target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
