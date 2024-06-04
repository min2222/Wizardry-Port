package electroblob.wizardry;

import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.inventory.ContainerPortableWorkbench;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWizardHandbook;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.tileentity.TileEntityLectern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class WizardryGuiHandler implements IGuiHandler {

	/** Incrementable index for the gui ID */
	private static int nextGuiId = 0;

	public static final int SPELL_BOOK = nextGuiId++;
	public static final int ARCANE_WORKBENCH = nextGuiId++;
	public static final int WIZARD_HANDBOOK = nextGuiId++;
	public static final int PORTABLE_CRAFTING = nextGuiId++;
	public static final int BOOKSHELF = nextGuiId++;
	public static final int LECTERN = nextGuiId++;

	@Override
	public Object getServerGuiElement(int id, Player player, Level world, int x, int y, int z){

		if(id == ARCANE_WORKBENCH){

			BlockEntity tileEntity = level.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new ContainerArcaneWorkbench(player.inventory, (TileEntityArcaneWorkbench)tileEntity);
			}

		}else if(id == PORTABLE_CRAFTING){
			return new ContainerPortableWorkbench(player.inventory, world, new BlockPos(x, y, z));

		}else if(id == BOOKSHELF){

			BlockEntity tileEntity = level.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityBookshelf){
				return new ContainerBookshelf(player.inventory, (TileEntityBookshelf)tileEntity);
			}
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, Player player, Level world, int x, int y, int z){

		if(id == ARCANE_WORKBENCH){

			BlockEntity tileEntity = level.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new electroblob.wizardry.client.gui.GuiArcaneWorkbench(player.inventory,
						(TileEntityArcaneWorkbench)tileEntity);
			}

		}else if(id == WIZARD_HANDBOOK && (player.getMainHandItem().getItem() instanceof ItemWizardHandbook
				|| player.getOffHandItem().getItem() instanceof ItemWizardHandbook)){

			return new electroblob.wizardry.client.gui.handbook.GuiWizardHandbook();

		}else if(id == SPELL_BOOK){

			if(player.getMainHandItem().getItem() instanceof ItemSpellBook){
				return new electroblob.wizardry.client.gui.GuiSpellBook(player.getMainHandItem());
			}else if(player.getOffHandItem().getItem() instanceof ItemSpellBook){
				return new electroblob.wizardry.client.gui.GuiSpellBook(player.getOffHandItem());
			}

		}else if(id == PORTABLE_CRAFTING){
			return new electroblob.wizardry.client.gui.GuiPortableCrafting(player.inventory, world, new BlockPos(x, y, z));

		}else if(id == BOOKSHELF){

			BlockEntity tileEntity = level.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityBookshelf){
				return new electroblob.wizardry.client.gui.GuiBookshelf(player.inventory,
						(TileEntityBookshelf)tileEntity);
			}

		}else if(id == LECTERN){

			BlockEntity tileEntity = level.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityLectern){
				return new electroblob.wizardry.client.gui.GuiLectern((TileEntityLectern)tileEntity);
			}

		}

		return null;
	}
}