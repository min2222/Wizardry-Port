package electroblob.wizardry.spell;

import java.util.UUID;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SummonSpiritWolf extends Spell {

	/** The string identifier for the potency attribute modifier. */
	private static final String POTENCY_ATTRIBUTE_MODIFIER = "potency";

	public static final IStoredVariable<UUID> UUID_KEY = IStoredVariable.StoredVariable.ofUUID("spiritWolfUUID", Persistence.ALWAYS);

	public SummonSpiritWolf(){
		super("summon_spirit_wolf", SpellActions.SUMMON, false);
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

			Entity oldWolf = EntityUtils.getEntityByUUID(world, data.getVariable(UUID_KEY));

			if(oldWolf != null) oldWolf.discard();

			BlockPos pos = BlockUtils.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntitySpiritWolf wolf = new EntitySpiritWolf(world);
			wolf.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			wolf.setTame(true);
			wolf.setOwnerUUID(caster.getUUID());
			// Potency gives the wolf more strength AND more health
			wolf.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
					new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.get(SpellModifiers.POTENCY) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
			wolf.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
					new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.amplified(SpellModifiers.POTENCY, 1.5f) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
			wolf.setHealth(wolf.getMaxHealth());

			world.addFreshEntity(wolf);

			data.setVariable(UUID_KEY, wolf.getUUID());
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;

	}

}
