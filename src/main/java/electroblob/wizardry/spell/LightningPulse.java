package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

import java.util.List;

public class LightningPulse extends Spell {

	public static final String REPULSION_VELOCITY = "repulsion_velocity";

	public LightningPulse(){
		super("lightning_pulse", SpellActions.POINT_DOWN, false);
		addProperties(EFFECT_RADIUS, DAMAGE, REPULSION_VELOCITY);
		this.soundValues(2, 1, 0);
	}
	
	// TODO: NPC casting support

	@Override
	protected SoundEvent[] createSounds(){
		return createSoundsWithSuffixes("spark", "explosion");
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.onGround){

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(
					getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade),
					caster.getX(), caster.getY(), caster.getZ(), world);

			for(LivingEntity target : targets){
				if(AllyDesignationSystem.isValidTarget(caster, target)){
					// Base damage is 4 hearts no matter where the target is.
					target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
							getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

					if(!level.isClientSide){

						double dx = target.getX() - caster.getX();
						double dz = target.getZ() - caster.getZ();
						// Normalises the velocity.
						double vectorLength = Math.sqrt(dx * dx + dz * dz);
						dx /= vectorLength;
						dz /= vectorLength;

						target.motionX = getProperty(REPULSION_VELOCITY).floatValue() * dx;
						target.motionY = 0;
						target.motionZ = getProperty(REPULSION_VELOCITY).floatValue() * dz;

						// Player motion is handled on that player's client so needs packets
						if(target instanceof ServerPlayer){
							((ServerPlayer)target).connection.sendPacket(new SPacketEntityVelocity(target));
						}
					}
				}
			}
			
			if(level.isClientSide){
				ParticleBuilder.create(Type.LIGHTNING_PULSE).pos(caster.getX(), caster.getY()
						+ GeometryUtils.ANTI_Z_FIGHTING_OFFSET, caster.getZ())
				.scale(modifiers.get(WizardryItems.blast_upgrade)).spawn(world);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}
		return false;
	}

}
