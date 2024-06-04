package electroblob.wizardry.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.world.level.Level;

public class ContainerPortableWorkbench extends ContainerWorkbench {

	public ContainerPortableWorkbench(InventoryPlayer inventory, Level world, BlockPos pos){

		super(inventory, world, pos);
	}

	// Overriden to stop the crafting gui from closing when there is no crafting table.
	@Override
	public boolean canInteractWith(Player player){
		return true;
	}

}
