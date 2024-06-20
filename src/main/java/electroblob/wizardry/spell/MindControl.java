package electroblob.wizardry.spell;

import java.util.Arrays;
import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags.EntityTypes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class MindControl extends SpellRay {

	/** The NBT tag name for storing the controlling entity's UUID in the target's tag compound. */
	public static final String NBT_KEY = "controllingEntity";

	public MindControl(){
		super("mind_control", SpellActions.POINT, false);
		addProperties(EFFECT_DURATION);
	}
	
	@Override public boolean canBeCastBy(Mob npc, boolean override) { return false; }
	@Override public boolean canBeCastBy(DispenserBlockEntity dispenser) { return false; }
	
	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
				
			if(!canControl(target)){
				if(!world.isClientSide){
					if(caster instanceof Player){
						// Adds a message saying that the player/boss entity/wizard resisted mind control
						((Player)caster).displayClientMessage(Component.translatable("spell.resist", target.getName(),
								this.getNameForTranslationFormatted()), true);
					}
				}

			}else if(target instanceof Mob){

				if(!world.isClientSide){
					if(!MindControl.findMindControlTarget((Mob)target, caster, world)){
						// If no valid target was found, this just acts like mind trick.
						((Mob)target).setTarget(null);
					}
				}

				if(target instanceof Sheep && ((Sheep)target).getColor() == DyeColor.BLUE
						&& EntityUtils.canDamageBlocks(caster, world)){
					if(!world.isClientSide) ((Sheep)target).setColor(DyeColor.RED); // Wololo!
					world.playLocalSound(caster.getX(), caster.getY(), caster.getZ(), SoundEvents.EVOKER_PREPARE_WOLOLO, WizardrySounds.SPELLS, 1, 1, false);
				}

				if(!world.isClientSide) startControlling((Mob)target, caster,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.DURATION_UPGRADE.get())));
			}

			if(world.isClientSide){
				
				for(int i=0; i<10; i++){
					ParticleBuilder.create(Type.DARK_MAGIC, world.random, target.getX(),
							target.getY() + target.getEyeHeight(), target.getZ(), 0.25, false)
					.clr(0.8f, 0.2f, 1.0f).spawn(world);
					ParticleBuilder.create(Type.DARK_MAGIC, world.random, target.getX(),
							target.getY() + target.getEyeHeight(), target.getZ(), 0.25, false)
					.clr(0.2f, 0.04f, 0.25f).spawn(world);
				}
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

	/** Returns true if the given entity can be mind controlled (i.e. is not a player, npc, evil wizard or boss). */
	public static boolean canControl(Entity target){

		// TODO: Add a max health limit that scales with potency

		return target instanceof Mob && !target.getType().is(EntityTypes.BOSSES) && !(target instanceof Npc)
				&& !(target instanceof EntityEvilWizard) && !Arrays.asList(Wizardry.settings.mindControlTargetsBlacklist)
				.contains(ForgeRegistries.ENTITY_TYPES.getKey(target.getType()));
	}

	public static void startControlling(Mob target, LivingEntity controller, int duration){
		target.getPersistentData().putUUID(NBT_KEY, controller.getUUID());
		target.addEffect(new MobEffectInstance(WizardryPotions.MIND_CONTROL.get(), duration, 0));
	}

	/**
	 * Finds the nearest creature to the given target which it is allowed to attack according to the given caster and
	 * sets it as the target's attack target. Handles both new and old AI and takes follow range into account. Defined
	 * here so it can be used both in the spell itself and in the potion effect (event handler).
	 * 
	 * @param target The entity being mind controlled
	 * @param caster The entity doing the controlling
	 * @param world The world to look for targets in
	 * @return True if a new target was found and set, false if not.
	 */
	public static boolean findMindControlTarget(Mob target, LivingEntity caster, Level world){

		// As of 1.1, this now uses the creature's follow range, like normal targeting. It also
		// no longer lasts until the creature dies; instead it is a potion effect which continues to
		// set the target until it wears off.
		List<LivingEntity> possibleTargets = EntityUtils.getLivingWithinRadius(
				target.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue(),
				target.getX(), target.getY(), target.getZ(), world);

		possibleTargets.remove(target);
		possibleTargets.remove(target.getVehicle());
		possibleTargets.removeIf(e -> e instanceof ArmorStand);

		LivingEntity newAITarget = null;

		for(LivingEntity possibleTarget : possibleTargets){
			if(AllyDesignationSystem.isValidTarget(caster, possibleTarget) && (newAITarget == null
					|| target.distanceTo(possibleTarget) < target.distanceTo(newAITarget))){
				newAITarget = possibleTarget;
			}
		}

		if(newAITarget != null){
			target.setTarget(newAITarget);
			return true;
		}

		return false;

	}
	
	/** Retrieves the given entity's controller and decides whether it needs to search for a target. */
	private static void processTargeting(Level world, Mob entity, LivingEntity currentTarget){
		
		if(entity.hasEffect(WizardryPotions.MIND_CONTROL.get()) && MindControl.canControl(entity)){

			CompoundTag entityNBT = entity.getPersistentData();

			if(entityNBT != null && entityNBT.hasUUID(MindControl.NBT_KEY)){

				Entity caster = EntityUtils.getEntityByUUID(world, entityNBT.getUUID(MindControl.NBT_KEY));

				if(caster instanceof LivingEntity){

					// If the current target is already a valid mind control target, nothing happens.
					if(AllyDesignationSystem.isValidTarget(caster, currentTarget)) return;

					if(MindControl.findMindControlTarget(entity, (LivingEntity)caster, world)){
						// If it worked, skip setting the target to null.
						return;
					}
				}
			}
			// If the caster couldn't be found or no valid target was found, this just acts like mind trick.
			entity.setTarget(null);
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){
		// Tries to find a new target for mind-controlled creatures that do not currently have one
		// When the mind-controlled creature does have a target, LivingSetAttackTargetEvent is used instead since it is
		// more efficient (because it only fires when the entity tries to set a target)
		// Of course, in survival this code is unlikely to be used much because the entity will always try to target the
		// player and hence will rarely have no target.
		// No need to do it every tick either!
		if(event.getEntity().tickCount % 50 == 0 && event.getEntity().hasEffect(WizardryPotions.MIND_CONTROL.get())
				&& event.getEntity() instanceof Mob){
			
			Mob entity = (Mob)event.getEntity();
			
			// Processes targeting if the current target is null or has died
			if(((Mob)event.getEntity()).getTarget() == null
				|| !((Mob)event.getEntity()).getTarget().isAlive()){
			
				processTargeting(entity.level, entity, null);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		// The != null check prevents infinite loops with mind trick
		if(event.getTarget() != null && event.getEntity() instanceof Mob)
			processTargeting(event.getEntity().level, (Mob)event.getEntity(), event.getTarget());
	}

	@SubscribeEvent
	public static void onPotionExpiryEvent(MobEffectEvent.Expired event){
		onEffectEnd(event.getEffectInstance(), event.getEntity());
	}

	@SubscribeEvent
	public static void onPotionExpiryEvent(MobEffectEvent.Remove event){
		onEffectEnd(event.getEffectInstance(), event.getEntity());
	}

	private static void onEffectEnd(MobEffectInstance effect, Entity entity){
		if(effect != null && effect.getEffect() == WizardryPotions.MIND_CONTROL.get() && entity instanceof Mob){
			((Mob)entity).setTarget(null); // End effect
			((Mob)entity).setLastHurtByMob(null); // End effect
		}
	}

}