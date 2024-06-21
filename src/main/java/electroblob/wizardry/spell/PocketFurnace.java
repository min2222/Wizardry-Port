package electroblob.wizardry.spell;

import java.util.Optional;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

public class PocketFurnace extends Spell {

	public static final String ITEMS_SMELTED = "items_smelted";

	public PocketFurnace(){
		super("pocket_furnace", SpellActions.IMBUE, false);
		addProperties(ITEMS_SMELTED);
		soundValues(1, 0.75f, 0);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		int usesLeft = (int)(getProperty(ITEMS_SMELTED).floatValue() * modifiers.get(SpellModifiers.POTENCY));

		ItemStack stack, result;

		for(int i = 0; i < caster.getInventory().getContainerSize() && usesLeft > 0; i++){

			stack = caster.getInventory().getItem(i);

			if(!stack.isEmpty() && !world.isClientSide){

				Container dummyInv = new SimpleContainer(1);
				dummyInv.setItem(0, stack);
				Optional<SmeltingRecipe> optionalSmeltingRecipe = world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, dummyInv, world);

				if (optionalSmeltingRecipe.isPresent()) {
					optionalSmeltingRecipe.get().assemble(dummyInv);

					result = optionalSmeltingRecipe.get().getResultItem();

					if (!result.isEmpty() && !(stack.getItem() instanceof TieredItem) && !(stack.getItem() instanceof ArmorItem) && !Settings.containsMetaItem(Wizardry.settings.pocketFurnaceItemBlacklist, stack)) {
						if (stack.getCount() <= usesLeft) {
							ItemStack stack2 = new ItemStack(result.getItem(), stack.getCount());
							if (InventoryUtils.doesPlayerHaveItem(caster, result.getItem())) {
								caster.addItem(stack2);
								caster.getInventory().setItem(i, ItemStack.EMPTY);
							} else {
								caster.getInventory().setItem(i, stack2);
							}
							usesLeft -= stack.getCount();
						} else {
							ItemStack copy = caster.getInventory().getItem(i).copy();
							copy.shrink(usesLeft);
							caster.getInventory().setItem(i, copy);
							caster.getInventory().add(
									new ItemStack(result.getItem(), usesLeft));
							usesLeft = 0;
						}
					}
				}
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		if(world.isClientSide){
			for(int i = 0; i < 10; i++){
				double x1 = (double)((float)caster.getX() + world.random.nextFloat() * 2 - 1.0F);
				double y1 = (double)((float)caster.getY() + caster.getEyeHeight() - 0.5F + world.random.nextFloat());
				double z1 = (double)((float)caster.getZ() + world.random.nextFloat() * 2 - 1.0F);
				world.addParticle(ParticleTypes.FLAME, x1, y1, z1, 0, 0.01F, 0);
			}
		}

		return usesLeft < 5;
	}

}
