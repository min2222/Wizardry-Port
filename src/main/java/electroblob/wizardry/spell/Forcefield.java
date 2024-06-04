package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;

public class Forcefield extends SpellConstruct<EntityForcefield> {

	public Forcefield(){
		super("forcefield", SpellActions.THRUST, EntityForcefield::new, false);
		addProperties(Spell.EFFECT_RADIUS);
	}

	@Override
	protected void addConstructExtras(EntityForcefield construct, Direction side, LivingEntity caster, SpellModifiers modifiers){
		construct.setRadius(getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade));
	}
}
