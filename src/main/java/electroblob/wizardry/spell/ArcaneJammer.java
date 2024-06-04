package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EntitySpellcasterIllager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.eventbus.api.EventPriority;

import java.lang.reflect.Field;
import java.util.Random;

@Mod.EventBusSubscriber
public class ArcaneJammer extends SpellRay {

	/** Random number generator used to coordinate whether spellcasting works or not. */
	private static final Random random = new Random();
	/** The number of ticks between updates of whether spellcasting works or not. */
	private static final int UPDATE_INTERVAL = 15;

	private static final Field spellTicks;

	static { // Yay more reflection
		spellTicks = ObfuscationReflectionHelper.findField(EntitySpellcasterIllager.class, "field_193087_b");
	}

	public ArcaneJammer(){
		super("arcane_jammer", SpellActions.POINT, false);
		this.soundValues(0.7f, 1, 0.4f);
		this.addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			if(!world.isClientSide){
				((LivingEntity)target).addEffect(new MobEffectInstance(WizardryPotions.arcane_jammer,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue() + (int)((modifiers.get(SpellModifiers.POTENCY) - 1)
								/ Constants.POTENCY_INCREASE_PER_TIER + 0.5f)));
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
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.random.nextInt(8)).clr(0.9f, 0.3f, 0.7f)
		.spawn(world);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) // Prevents all spells so it comes before everything else
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Arcane jammer has a chance to prevent spell casting
		// We can't just use a straight-up random number because otherwise people can spam it to get it to cast
		// Instead, we're using the world time to make blocks of time when spells will and won't work
		random.setSeed(event.getWorld().getTotalWorldTime() / UPDATE_INTERVAL);
		// For some unfathomable reason, the first call to this after setting the seed remains the same for long
		// sequences of consecutive seeds, so let's clear it out first to get to a more changeable value
		random.nextInt(2);

		if(event.getCaster() != null && event.getCaster().hasEffect(WizardryPotions.arcane_jammer)
				// Arcane jammer I has a 1/2 chance, level II has a 2/3 chance, and so on
				&& random.nextInt(event.getCaster().getActivePotionEffect(WizardryPotions.arcane_jammer).getAmplifier() + 2) > 0){

			event.setCanceled(true);

			// TODO: This currently doesn't play nicely with continuous or charge-up spells
			if(event.getSource() == Source.WAND || event.getSource() == Source.SCROLL){
				event.getCaster().setActiveHand(InteractionHand.MAIN_HAND);
			}

			// Because we're using a seed that should be consistent, we can do client-side stuff!
			event.getWorld().playSound(event.getCaster().getX(), event.getCaster().getY(), event.getCaster().getZ(),
					WizardrySounds.MISC_SPELL_FAIL, WizardrySounds.SPELLS, 1, 1, false);

			if(event.getWorld().isRemote){

				Vec3 centre = event.getCaster().getPositionEyes(1).add(event.getCaster().getLookVec());

				for(int i = 0; i < 5; i++){
					double x = centre.x + 0.5f * (event.getWorld().random.nextFloat() - 0.5f);
					double y = centre.y + 0.5f * (event.getWorld().random.nextFloat() - 0.5f);
					double z = centre.z + 0.5f * (event.getWorld().random.nextFloat() - 0.5f);
					event.getWorld().spawnParticle(ParticleTypes.SMOKE_LARGE, x, y, z, 0, 0, 0);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event){

		if(event.getEntity() instanceof EntitySpellcasterIllager
				&& event.getEntity().hasEffect(WizardryPotions.arcane_jammer)){

			((EntitySpellcasterIllager)event.getEntity()).setSpellType(EntitySpellcasterIllager.SpellType.NONE);

			try{
				spellTicks.set(event.getEntity(), 10);
			}catch(IllegalAccessException e){
				Wizardry.logger.error("Error setting evoker spell timer:", e);
			}
		}
	}

}
