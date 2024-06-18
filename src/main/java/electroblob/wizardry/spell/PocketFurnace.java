package electroblob.wizardry.spell;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.ItemArmor;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.core.particles.ParticleTypes;
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

				result = FurnaceRecipes.instance().getSmeltingResult(stack);

				if(!result.isEmpty() && !(stack.getItem() instanceof ItemTool) && !(stack.getItem() instanceof SwordItem)
						&& !(stack.getItem() instanceof ArmorItem)
						&& !Settings.containsMetaItem(Wizardry.settings.pocketFurnaceItemBlacklist, stack)){

					if(stack.getCount() <= usesLeft){
						ItemStack stack2 = new ItemStack(result.getItem(), stack.getCount(), result.getItemDamage());
						if(InventoryUtils.doesPlayerHaveItem(caster, result.getItem())){
							caster.getInventory().addItemStackToInventory(stack2);
							caster.getInventory().setInventorySlotContents(i, ItemStack.EMPTY);
						}else{
							caster.getInventory().setInventorySlotContents(i, stack2);
						}
						usesLeft -= stack.getCount();
					}else{
						caster.getInventory().remove(i, usesLeft);
						caster.getInventory().addItemStackToInventory(
								new ItemStack(result.getItem(), usesLeft, result.getItemDamage()));
						usesLeft = 0;
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
