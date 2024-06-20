package electroblob.wizardry.spell;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class Heal extends SpellBuff {

	public Heal(){
		super("heal", 1, 1, 0.3f);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(HEALTH);
	}
	
	@Override
	protected boolean applyEffects(LivingEntity caster, SpellModifiers modifiers){
		
		if(caster.getHealth() < caster.getMaxHealth() && caster.getHealth() > 0){
			heal(caster, getProperty(HEALTH).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			return true;
		}
		
		return false;
	}

	/**
	 * Heals the given entity by the given amount, accounting for special behaviour from artefacts. This does not check
	 * whether the entity is already on full health or not.
	 * @param entity The entity to heal
	 * @param health The number of half-hearts to heal
	 */
	public static void heal(LivingEntity entity, float health){

		float excessHealth = entity.getHealth() + health - entity.getMaxHealth();

		entity.heal(health);

		// If the player is able to heal, they can't possibly have absorption hearts, so no need to check!
		if(excessHealth > entity.getAbsorptionAmount() && entity instanceof Player
				&& ItemArtefact.isArtefactActive((Player)entity, WizardryItems.AMULET_ABSORPTION.get())){
			entity.setAbsorptionAmount(excessHealth);
		}

	}

}
