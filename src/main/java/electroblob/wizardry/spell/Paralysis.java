package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Paralysis extends SpellRay {

	/** Creatures with this much health or less will snap out of paralysis - but only when they take damage, so a
	 * creature on critical health may still be paralysed, but if it takes any damage at all the paralysis effect
	 * will end. */
	private static final String CRITICAL_HEALTH = "critical_health";

	/**
	 * Multiplier for affecting players
	 */
	private static final String PLAYER_EFFECT_DURATION_MULTIPLIER = "player_effect_duration_multiplier";

	public Paralysis(){
		super("paralysis", SpellActions.POINT, false);
		addProperties(DAMAGE, EFFECT_DURATION, CRITICAL_HEALTH, PLAYER_EFFECT_DURATION_MULTIPLIER);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
		
			if(world.isClientSide){
				// Rather neatly, the entity can be set here and if it's null nothing will happen.
				ParticleBuilder.create(Type.BEAM).entity(caster).clr(0.2f, 0.6f, 1)
				.pos(caster != null ? origin.subtract(caster.position()) : origin).target(target).spawn(world);
				ParticleBuilder.create(Type.LIGHTNING).entity(caster)
				.pos(caster != null ? origin.subtract(caster.position()) : origin).target(target).spawn(world);
			}
	
			// This is a lot neater than it was, thanks to the damage type system.
			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isClientSide && caster instanceof Player) ((Player)caster).displayClientMessage(
						Component.translatable("spell.resist",
						target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			}

			float durationMultiplier = target instanceof Player ? modifiers.get(PLAYER_EFFECT_DURATION_MULTIPLIER) : 1.0f;
			((LivingEntity)target).addEffect(new MobEffectInstance(WizardryPotions.PARALYSIS.get(),
					(int)(getProperty(EFFECT_DURATION).floatValue() * durationMultiplier * modifiers.get(WizardryItems.DURATION_UPGRADE.get())), 0));
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(world.isClientSide){
			
			if(world.getBlockState(pos).getMaterial().isSolid()){
				Vec3 vec = hit.add(new Vec3(side.step()).scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET));
				ParticleBuilder.create(Type.SCORCH).pos(vec).face(side).clr(0.4f, 0.8f, 1).spawn(world);
			}
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		// This is first because we want the endpoint to be unaffected by the offset
		Vec3 endpoint = origin.add(direction.scale(getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.RANGE_UPGRADE.get())));

		if(world.isClientSide){
			ParticleBuilder.create(Type.LIGHTNING).time(4).pos(origin).target(endpoint).scale(0.5f).spawn(world);
			ParticleBuilder.create(Type.BEAM).clr(0.2f, 0.6f, 1).time(4).pos(origin)
			.target(endpoint).spawn(world);
		}
		
		return true;
	}
	
	// See WizardryClientEventHandler for prevention of players' movement under the effects of paralysis
	
	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){
		// Disables entities' AI when under the effects of paralysis and re-enables it on the last update of the effect
		// - this can't be in the potion class because it requires access to the duration and hence the actual
		// PotionEffect instance
		if(event.getEntity() instanceof Mob && event.getEntity().hasEffect(WizardryPotions.PARALYSIS.get())){
			int timeLeft = event.getEntity().getEffect(WizardryPotions.PARALYSIS.get()).getDuration();
			((Mob)event.getEntity()).setNoAi(timeLeft > 1);
		}
	}
	
	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){
		// Paralysed creatures snap out of paralysis when they take critical damage
		if(event.getEntity().hasEffect(WizardryPotions.PARALYSIS.get()) && event.getEntity().getHealth()
				- event.getAmount() <= Spells.paralysis.getProperty(CRITICAL_HEALTH).floatValue()){
			event.getEntity().removeEffect(WizardryPotions.PARALYSIS.get());
		}
	}

}
