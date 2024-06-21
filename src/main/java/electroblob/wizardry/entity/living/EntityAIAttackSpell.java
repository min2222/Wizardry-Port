package electroblob.wizardry.entity.living;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.packet.PacketNPCCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

/**
 * Entity AI class for use by instances of {@link ISpellCaster}. This deals with pathing, the spell casting itself and
 * the attack cooldown. Also provides an automatic implementation of continuous spell casting using the methods
 * specified in {@code ISpellCaster}; all the entity class needs to do is implement those methods.
 * @param <T> The type of entity that this AI belongs to; must both extend EntityLiving <i>and</i> implement ISpellCaster
 */
// Mmmm generics...
public class EntityAIAttackSpell<T extends Mob & ISpellCaster> extends Goal {

	/** The entity the AI instance has been applied to. Thanks to type parameters, methods from both EntityLiving and
	 * ISummonedCreature may be invoked on this field. */
	private final T attacker;
	private LivingEntity target;
	/**
	 * Decremented each tick while greater than 0. When a spell is cast, this is set to that spell's cooldown plus the
	 * base cooldown.
	 */
	private int cooldown;
	/**
	 * The number of ticks between the entity finding a new target and when it first starts attacking, and also the
	 * amount that is added to the spell's cooldown between casting spells.
	 */
	private final int baseCooldown;
	/**
	 * Decremented each tick while greater than 0. When a continuous spell is first cast, this is set to the value of
	 * {@link EntityAIAttackSpell#continuousSpellDuration}.
	 */
	// I think that in this case this is only necessary on the server side. If any inconsistent behaviour
	// occurs, look into syncing this as well.
	private int continuousSpellTimer;
	/** The number of ticks that continuous spells will be cast for before cooling down. */
	private final int continuousSpellDuration;
	/** The speed that the entity should move when attacking. Only used when passed into the navigator. */
	private final double speed;
	private int seeTime;
	private final float maxAttackDistance;

	/**
	 * Creates a new spell attack AI with the given parameters.
	 * 
	 * @param attacker The entity that that uses this AI.
	 * @param speed The speed that the entity should move when attacking. Only used when passed into the navigator.
	 * @param maxDistance The maximum distance the entity should be from its target.
	 * @param baseCooldown The number of ticks between the entity finding a new target and when it first starts
	 *        attacking, and also the amount that is added to the cooldown of the spell that has just been cast.
	 * @param continuousSpellDuration The number of ticks that continuous spells will be cast for before cooling down.
	 */
	public EntityAIAttackSpell(T attacker, double speed, float maxDistance, int baseCooldown, int continuousSpellDuration){
		this.cooldown = -1;
		this.attacker = attacker;
		this.baseCooldown = baseCooldown;
		this.continuousSpellDuration = continuousSpellDuration;
		this.speed = speed;
		this.maxAttackDistance = maxDistance * maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
	}

	@Override
	public boolean canUse(){

		LivingEntity entitylivingbase = this.attacker.getTarget();

		if(entitylivingbase == null){
			return false;
		}else{
			this.target = entitylivingbase;
			return true;
		}
	}

	@Override
	public boolean canContinueToUse(){
		return this.canUse() || !this.attacker.getNavigation().isDone();
	}

	@Override
	public void stop(){
		this.target = null;
		this.seeTime = 0;
		this.cooldown = -1;
		this.setContinuousSpellAndNotify(Spells.NONE, new SpellModifiers());
		this.continuousSpellTimer = 0;
	}

	private void setContinuousSpellAndNotify(Spell spell, SpellModifiers modifiers){
		attacker.setContinuousSpell(spell);
		// Particles are usually only visible from 16 blocks away, so 128 is more than far enough.
		// TODO: Why is this one a 128 block radius, whilst the other one is all in dimension?
		WizardryPacketHandler.net.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(128, attacker.getX(), attacker.getY(), attacker.getZ(), attacker.level.dimension())),
				new PacketNPCCastSpell.Message(attacker.getId(), target == null ? -1 : target.getId(),
						InteractionHand.MAIN_HAND, spell, modifiers));
	}

	@Override
	public void tick(){

		// Only executed server side.

		double distanceSq = this.attacker.distanceToSqr(this.target.getX(), this.target.getY(),
				this.target.getZ());
		boolean targetIsVisible = this.attacker.getSensing().hasLineOfSight(this.target);

		if(targetIsVisible){
			++this.seeTime;
		}else{
			this.seeTime = 0;
		}

		if(distanceSq <= (double)this.maxAttackDistance && this.seeTime >= 20){
			this.attacker.getNavigation().stop();
		}else{
			this.attacker.getNavigation().moveTo(this.target, this.speed);
		}

		this.attacker.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

		if(this.continuousSpellTimer > 0){

			this.continuousSpellTimer--;

			// If the target goes out of range or out of sight...
			if(distanceSq > (double)this.maxAttackDistance || !targetIsVisible
			// ...or the spell is cancelled via events...
					|| MinecraftForge.EVENT_BUS
							.post(new SpellCastEvent.Tick(Source.NPC, attacker.getContinuousSpell(), attacker,
									attacker.getModifiers(), this.continuousSpellDuration - this.continuousSpellTimer))
					// ...or the spell no longer succeeds...
					|| !attacker.getContinuousSpell().cast(attacker.level, attacker, InteractionHand.MAIN_HAND,
							this.continuousSpellDuration - this.continuousSpellTimer, target, attacker.getModifiers())
					// ...or the time has elapsed...
					|| this.continuousSpellTimer == 0){

				// ...reset the continuous spell timer and start the cooldown.
				this.continuousSpellTimer = 0;
				this.cooldown = attacker.getContinuousSpell().getCooldown() + this.baseCooldown;
				setContinuousSpellAndNotify(Spells.NONE, new SpellModifiers());
				return;

			}else if(this.continuousSpellDuration - this.continuousSpellTimer == 1){
				// On the first tick, if the spell did succeed, fire SpellCastEvent.Post.
				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.NPC, attacker.getContinuousSpell(),
						attacker, attacker.getModifiers()));
			}

		}else if(--this.cooldown == 0){

			if(distanceSq > (double)this.maxAttackDistance || !targetIsVisible){
				return;
			}

			double dx = target.getX() - attacker.getX();
			double dz = target.getZ() - attacker.getZ();

			List<Spell> spells = new ArrayList<Spell>(attacker.getSpells());

			if(spells.size() > 0){

				if(!attacker.level.isClientSide){

					// New way of choosing a spell; keeps trying until one works or all have been tried

					Spell spell;

					while(!spells.isEmpty()){

						spell = spells.get(attacker.level.random.nextInt(spells.size()));

						SpellModifiers modifiers = attacker.getModifiers();

						if(spell != null && attemptCastSpell(spell, modifiers)){
							// The spell worked, so we're done!
							attacker.setYRot((float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F);
							return;
						}else{
							spells.remove(spell);
						}
					}
				}
			}

		}else if(this.cooldown < 0){
			// This should only be reached when the entity first starts attacking. Stops it attacking instantly.
			this.cooldown = this.baseCooldown;
		}
	}

	/** Attempts to cast the given spell (including event firing) and returns true if it succeeded. */
	private boolean attemptCastSpell(Spell spell, SpellModifiers modifiers){

		// If anything stops the spell working at this point, nothing else happens.
		if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(Source.NPC, spell, attacker, modifiers))){
			return false;
		}

		// This is only called when spell casting starts so ticksInUse is always zero
		if(spell.cast(attacker.level, attacker, InteractionHand.MAIN_HAND, 0, target, modifiers)){

			if(spell.isContinuous){
				// -1 because the spell has been cast once already!
				this.continuousSpellTimer = this.continuousSpellDuration - 1;
				setContinuousSpellAndNotify(spell, modifiers);

			}else{

				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.NPC, spell, attacker, modifiers));

				// For now, the cooldown is just added to the constant base cooldown. I think this
				// is a reasonable way of doing things; it's certainly better than before.
				this.cooldown = this.baseCooldown + spell.getCooldown();

				if(spell.requiresPacket()){
					// Sends a packet to all players in dimension to tell them to spawn particles.
					PacketNPCCastSpell.Message msg = new PacketNPCCastSpell.Message(attacker.getId(), target.getId(),
							InteractionHand.MAIN_HAND, spell, modifiers);
					WizardryPacketHandler.net.send(PacketDistributor.DIMENSION.with(() -> attacker.level.dimension()), msg);
				}
			}

			return true;
		}

		return false;
	}
}
