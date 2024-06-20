package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

		if(caster.isOnGround()){

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(
					getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.BLAST_UPGRADE.get()),
					caster.getX(), caster.getY(), caster.getZ(), world);

			for(LivingEntity target : targets){
				if(AllyDesignationSystem.isValidTarget(caster, target)){
					// Base damage is 4 hearts no matter where the target is.
					target.hurt(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
							getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

					if(!world.isClientSide){

						double dx = target.getX() - caster.getX();
						double dz = target.getZ() - caster.getZ();
						// Normalises the velocity.
						double vectorLength = Math.sqrt(dx * dx + dz * dz);
						dx /= vectorLength;
						dz /= vectorLength;

						target.setDeltaMovement(getProperty(REPULSION_VELOCITY).floatValue() * dx, 0, getProperty(REPULSION_VELOCITY).floatValue() * dz);

						// Player motion is handled on that player's client so needs packets
						if(target instanceof ServerPlayer){
							((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
						}
					}
				}
			}
			
			if(world.isClientSide){
				ParticleBuilder.create(Type.LIGHTNING_PULSE).pos(caster.getX(), caster.getY()
						+ GeometryUtils.ANTI_Z_FIGHTING_OFFSET, caster.getZ())
				.scale(modifiers.get(WizardryItems.BLAST_UPGRADE.get())).spawn(world);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}
		return false;
	}

}
