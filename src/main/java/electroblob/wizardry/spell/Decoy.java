package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class Decoy extends Spell {

	public static final String DECOY_LIFETIME = "decoy_lifetime";
	public static final String MOB_TRICK_CHANCE = "mob_trick_chance";

	public Decoy(){
		super("decoy", SpellActions.SUMMON, false);
		this.soundValues(1, 0.9f, 0.2f);
		addProperties(DECOY_LIFETIME, MOB_TRICK_CHANCE);
	}

	@Override public boolean canBeCastBy(Mob npc, boolean override){ return true; }

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){
		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		// Uses the synchronised entity id to ensure it is consistent on client and server, but not always the same.
		double splitSpeed = caster.getEntityId() % 2 == 0 ? 0.3 : -0.3;
		spawnDecoy(world, caster, modifiers, splitSpeed);
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}
	

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target, SpellModifiers modifiers){
		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		double splitSpeed = world.random.nextBoolean() ? 0.3 : -0.3;
		spawnDecoy(world, caster, modifiers, splitSpeed);
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}
	
	private void spawnDecoy(Level world, LivingEntity caster, SpellModifiers modifiers, double splitSpeed){

		if(!level.isClientSide){
			EntityDecoy decoy = new EntityDecoy(world);
			decoy.setCaster(caster);
			decoy.setLifetime(getProperty(DECOY_LIFETIME).intValue());
			decoy.setLocationAndAngles(caster.getX(), caster.getY(), caster.getZ(), caster.rotationYaw, caster.rotationPitch);
			decoy.addVelocity(-caster.getLookVec().z * splitSpeed, 0, caster.getLookVec().x * splitSpeed);
			// Ignores the show names setting, since this would allow a player to easily detect a decoy
			// Instead, a decoy player has its caster's name tag shown permanently and non-player decoys have nothing
			if(caster instanceof Player) decoy.setCustomNameTag(caster.getName());
			world.spawnEntity(decoy);

			// Tricks any mobs that are targeting the caster into targeting the decoy instead.
			for(Mob creature : EntityUtils.getEntitiesWithinRadius(16, caster.getX(), caster.getY(),
					caster.getZ(), world, Mob.class)){
				// More likely to trick mobs the higher the damage multiplier
				// The default base value is 0.5, so modifiers of 2 or more will guarantee mobs are tricked
				if(creature.getAttackTarget() == caster && world.random.nextFloat()
						< getProperty(MOB_TRICK_CHANCE).floatValue() * modifiers.get(SpellModifiers.POTENCY)){
					creature.setAttackTarget(decoy);
				}
			}
		}

		caster.addVelocity(caster.getLookVec().z * splitSpeed, 0, -caster.getLookVec().x * splitSpeed);
	}

}
