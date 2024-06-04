package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityMagicSlime;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class Slime extends SpellRay {

	public Slime(){
		super("slime", SpellActions.POINT, false);
		addProperties(DURATION);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createSoundsWithSuffixes("shoot", "squelch");
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target) && !(target instanceof EntityMagicSlime)){

			if(target instanceof EntitySlime){
				if(!level.isClientSide && caster instanceof Player) ((Player)caster).sendStatusMessage(
						Component.translatable("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			}else{

				if(!level.isClientSide){
					EntityMagicSlime slime = new EntityMagicSlime(world, caster, (LivingEntity)target,
							(int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
					world.spawnEntity(slime);
				}
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
		world.spawnParticle(ParticleTypes.SLIME, x, y, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.2f, 0.8f, 0.1f).spawn(world);
	}

}
