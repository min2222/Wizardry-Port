package electroblob.wizardry.spell;

import electroblob.wizardry.entity.projectile.EntityEmber;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

public class Disintegration extends SpellRay {

	public static final String EMBER_COUNT = "ember_count";
	public static final String EMBER_LIFETIME = "ember_lifetime";

	public static final String NBT_KEY = "disintegrating";

	public Disintegration(){
		super("disintegration", SpellActions.POINT, false);
		addProperties(DAMAGE, BURN_DURATION, EMBER_LIFETIME, EMBER_COUNT);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
			if(!world.isClientSide && caster instanceof Player) ((Player)caster).sendStatusMessage(
					Component.translatable("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
		}else{

			target.setSecondsOnFire((int)(getProperty(BURN_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
			EntityUtils.attackEntityWithoutKnockback(target, caster == null ? DamageSource.MAGIC :
					MagicDamage.causeDirectMagicDamage(caster, DamageType.FIRE),
					getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

			if(target instanceof LivingEntity && ((LivingEntity)target).getHealth() <= 0){
				spawnEmbers(world, caster, target, getProperty(EMBER_COUNT).intValue());
			}
		}
		
		return true;
	}

	public static void spawnEmbers(Level world, LivingEntity caster, Entity target, int count){

		target.extinguish();

		if(world.isClientSide){ // FIXME: Various syncing issues here!
			// Set NBT client-side, it's only for rendering
			// Normally dying entities are ignored but we're fiddling with them here so double-check
			if(!target.getEntityData().hasKey(NBT_KEY)){
				target.getEntityData().putInt(NBT_KEY, target.tickCount);
			}
		}else{
			for(int i = 0; i < count; i++){
				EntityEmber ember = new EntityEmber(world, caster);
				double x = (world.random.nextDouble() - 0.5) * target.getBbWidth();
				double y = world.random.nextDouble() * target.getBbHeight();
				double z = (world.random.nextDouble() - 0.5) * target.getBbWidth();
				ember.setPosition(target.getX() + x, target.getY() + y, target.getZ() + z);
				ember.tickCount = world.random.nextInt(20);
				float speed = 0.2f;
				ember.motionX = x * speed;
				ember.motionY = y * 0.5f * speed;
				ember.motionZ = z * speed;
				world.addFreshEntity(ember);
			}
		}
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(world.isClientSide){
			
			for(int i = 0; i < 8; i++){
				world.spawnParticle(ParticleTypes.LAVA, hit.x, hit.y, hit.z, 0, 0, 0);
			}
			
			if(world.getBlockState(pos).getMaterial().isSolid()){
				Vec3 vec = hit.add(new Vec3(side.getDirectionVec()).scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET));
				ParticleBuilder.create(Type.SCORCH).pos(vec).face(side).clr(1, 0.2f, 0).spawn(world);
			}
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticleRay(Level world, Vec3 origin, Vec3 direction, LivingEntity caster, double distance){
		Vec3 endpoint = origin.add(direction.scale(distance));
		ParticleBuilder.create(Type.BEAM).clr(1, 0.4f, 0).fade(1, 0.1f, 0).time(4).pos(origin).target(endpoint).spawn(world);
	}

}
