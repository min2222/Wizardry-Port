package electroblob.wizardry.spell;

import javax.annotation.Nullable;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Intimidate extends SpellAreaEffect {

	/** The NBT tag name for storing the feared entity's UUID in the target's tag compound. */
	public static final String NBT_KEY = "fearedEntity";

	// These aren't spell properties because they're part fo the actual potion effect, not the spell itself.
	// However, the avoid distance can be modified using the potion amplifier.
	private static final double BASE_AVOID_DISTANCE = 16;
	private static final double AVOID_DISTANCE_PER_LEVEL = 4;

	public Intimidate(){
		super("intimidate", SpellActions.SUMMON, false);
		this.alwaysSucceed(true);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	public boolean canBeCastBy(DispenserBlockEntity dispenser){
		return false;
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(caster != null && target instanceof PathfinderMob){

			int bonusAmplifier = SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));

			CompoundTag entityNBT = target.getPersistentData();
			if(entityNBT != null) entityNBT.putUUID(NBT_KEY, caster.getUUID());

			target.addEffect(new MobEffectInstance(WizardryPotions.fear,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					getProperty(EFFECT_STRENGTH).intValue() + bonusAmplifier));
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){

		if(caster != null) origin = caster.getEyePosition(1);

		for(int i = 0; i < 30; i++){
			double x = origin.x - 1 + world.random.nextDouble() * 2;
			double y = origin.y - 0.25 + world.random.nextDouble() * 0.5;
			double z = origin.z - 1 + world.random.nextDouble() * 2;
			ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.9f, 0.1f, 0).spawn(world);
		}
	}

	/**
	 * Finds a random position away from the caster and sets the given target's AI path to that location. Defined here
	 * so it can be used both in the spell itself and in the potion effect (event handler).
	 * 
	 * @param target The entity running away
	 * @param caster The entity that is being run away from
	 * @param distance How far the entity will run from the caster
	 * @return True if a new path was found and set, false if not.
	 */
	public static boolean runAway(PathfinderMob target, LivingEntity caster, double distance){

		if(target.distanceTo(caster) < distance){

			Vec3 Vec3d = DefaultRandomPos.getPosAway(target, (int)distance, (int)(distance/2),
					new Vec3(caster.getX(), caster.getY(), caster.getZ()));

			if(Vec3d == null){
				return false;

			}else{
				// In both cases it is necessary to check if the entity already has a path so it doesn't change
				// direction every tick, unless that path is towards the caster.
				// Path path = target.getNavigator().getPathToXYZ(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord);

				boolean flag = true;

				if(!target.getNavigation().isDone()){
					Node point = target.getNavigation().getPath().getEndNode();
					if(point != null) flag = caster.distanceToSqr(point.x, point.y, point.z) < distance;
				}
				// Has a built in mind trick effect because for whatever reason this makes it work with skeletons.
				target.setTarget(null);

				if(flag) return target.getNavigation().moveTo(Vec3d.x, Vec3d.y, Vec3d.z, 1.25);// target.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
			}
		}

		return false;
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){

		// No need to do this every tick either
		if(event.getEntity().tickCount % 50 == 0 && event.getEntity().hasEffect(WizardryPotions.fear)
				&& event.getEntity() instanceof PathfinderMob){

			CompoundTag entityNBT = event.getEntity().getPersistentData();
			PathfinderMob creature = (PathfinderMob)event.getEntity();

			if(entityNBT != null && entityNBT.hasUUID(NBT_KEY)){

				Entity caster = EntityUtils.getEntityByUUID(creature.level, entityNBT.getUUID(NBT_KEY));

				if(caster instanceof LivingEntity){
					double distance = BASE_AVOID_DISTANCE + AVOID_DISTANCE_PER_LEVEL
							* event.getEntity().getEffect(WizardryPotions.fear).getAmplifier();
					runAway(creature, (LivingEntity)caster, distance);
				}
			}
		}
	}

}
