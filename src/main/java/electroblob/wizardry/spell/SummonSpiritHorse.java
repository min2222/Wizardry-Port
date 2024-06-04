package electroblob.wizardry.spell;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.util.UUID;

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

		if(!world.isRemote){

			Entity oldHorse = EntityUtils.getEntityByUUID(world, data.getVariable(UUID_KEY));

			if(oldHorse != null) oldHorse.setDead();

			BlockPos pos = BlockUtils.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntitySpiritHorse horse = new EntitySpiritHorse(world);
			horse.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			horse.setTamedBy(caster);
			horse.setHorseSaddled(true);
			world.spawnEntity(horse);

			horse.getEntityAttribute(Attributes.MOVEMENT_SPEED).applyModifier(
					new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.get(SpellModifiers.POTENCY) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
			// Jump strength increases ridiculously fast, so we're reducing the effect of the modifier by 75%
			horse.getEntityAttribute(EntitySpiritHorse.JUMP_STRENGTH).applyModifier(new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER,
					modifiers.amplified(SpellModifiers.POTENCY, 0.25f) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));

			data.setVariable(UUID_KEY, horse.getUniqueID());
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
