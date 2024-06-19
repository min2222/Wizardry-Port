package electroblob.wizardry.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public class ContainerPortableWorkbench extends CraftingMenu {

	public ContainerPortableWorkbench(int p_39356_, Inventory p_39357_){
		super(p_39356_, p_39357_, ContainerLevelAccess.NULL);
	}

	// Overriden to stop the crafting gui from closing when there is no crafting table.
	@Override
	public boolean stillValid(Player player){
		return true;
	}

}
