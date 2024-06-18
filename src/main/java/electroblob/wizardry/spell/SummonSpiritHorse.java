package electroblob.wizardry.spell;

import java.util.UUID;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SummonSpiritHorse extends Spell {

	/** The string identifier for the potency attribute modifier. */
	private static final String POTENCY_ATTRIBUTE_MODIFIER = "potency";

	public static final IStoredVariable<UUID> UUID_KEY = IStoredVariable.StoredVariable.ofUUID("spiritHorseUUID", Persistence.ALWAYS);

	public SummonSpiritHorse(){
		super("summon_spirit_horse", SpellActions.SUMMON, false);
		addProperties(SpellMinion.SUMMON_RADIUS);
		soundValues(0.7f, 1.2f, 0.4f);
		WizardData.registerStoredVariables(UUID_KEY);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData data = WizardData.get(caster);

		if(!world.isClientSide){

			Entity oldHorse = EntityUtils.getEntityByUUID(world, data.getVariable(UUID_KEY));

			if(oldHorse != null) oldHorse.discard();

			BlockPos pos = BlockUtils.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntitySpiritHorse horse = new EntitySpiritHorse(world);
			horse.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			horse.tameWithName(caster);
			horse.equipSaddle(SoundSource.PLAYERS);
			world.addFreshEntity(horse);

			horse.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
					new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.get(SpellModifiers.POTENCY) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
			// Jump strength increases ridiculously fast, so we're reducing the effect of the modifier by 75%
			horse.getAttribute(EntitySpiritHorse.JUMP_STRENGTH).addTransientModifier(new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER,
					modifiers.amplified(SpellModifiers.POTENCY, 0.25f) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));

			data.setVariable(UUID_KEY, horse.getUUID());
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
