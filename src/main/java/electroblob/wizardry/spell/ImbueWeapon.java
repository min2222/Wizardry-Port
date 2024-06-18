package electroblob.wizardry.spell;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class ImbueWeapon extends Spell {

	public ImbueWeapon(){
		super("imbue_weapon", SpellActions.IMBUE, false);
		addProperties(EFFECT_DURATION);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		// Won't work if the weapon already has the enchantment
		if(WizardData.get(caster) != null){

			for(ItemStack stack : InventoryUtils.getPrioritisedHotbarAndOffhand(caster)){

				if(isSword(stack)
						&& !EnchantmentHelper.getEnchantments(stack).containsKey(WizardryEnchantments.magic_sword)
						&& WizardData.get(caster).getImbuementDuration(WizardryEnchantments.magic_sword) <= 0){
					// The enchantment level as determined by the damage multiplier. The + 0.5f is so that
					// weird float processing doesn't incorrectly round it down.
					stack.enchant(WizardryEnchantments.magic_sword, modifiers.get(SpellModifiers.POTENCY) == 1.0f
							? 1
							: (int)((modifiers.get(SpellModifiers.POTENCY) - 1.0f) / Constants.POTENCY_INCREASE_PER_TIER
									+ 0.5f));
					WizardData.get(caster).setImbuementDuration(WizardryEnchantments.magic_sword,
							(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));

				}else if(isBow(stack)
						&& !EnchantmentHelper.getEnchantments(stack).containsKey(WizardryEnchantments.magic_bow)
						&& WizardData.get(caster).getImbuementDuration(WizardryEnchantments.magic_bow) <= 0){
					// The enchantment level as determined by the damage multiplier. The + 0.5f is so that
					// weird float processing doesn't incorrectly round it down.
					stack.enchant(WizardryEnchantments.magic_bow, modifiers.get(SpellModifiers.POTENCY) == 1.0f
							? 1
							: (int)((modifiers.get(SpellModifiers.POTENCY) - 1.0f) / Constants.POTENCY_INCREASE_PER_TIER
									+ 0.5f));
					WizardData.get(caster).setImbuementDuration(WizardryEnchantments.magic_bow,
							(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));

				}else{
					continue;
				}

				if(world.isClientSide){
					for(int i=0; i<10; i++){
						double x = caster.getX() + world.random.nextDouble() * 2 - 1;
						double y = caster.getY() + caster.getEyeHeight() - 0.5 + world.random.nextDouble();
						double z = caster.getZ() + world.random.nextDouble() * 2 - 1;
						ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(0.9f, 0.7f, 1).spawn(world);
					}
				}

				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}
		return false;
	}

	/** Returns true if the given item counts as a sword, i.e. it extends {@link SwordItem} or is in the whitelist. */
	public static boolean isSword(ItemStack stack){
		return stack.getItem() instanceof SwordItem || Settings.containsMetaItem(Wizardry.settings.swordItemWhitelist, stack);
	}

	/** Returns true if the given item counts as a bow, i.e. it extends {@link BowItem} or is in the whitelist. */
	public static boolean isBow(ItemStack stack){
		return stack.getItem() instanceof BowItem || Settings.containsMetaItem(Wizardry.settings.bowItemWhitelist, stack);
	}

}
