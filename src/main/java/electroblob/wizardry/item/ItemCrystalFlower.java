package electroblob.wizardry.item;

import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ItemCrystalFlower extends ItemBlock {

	public ItemCrystalFlower(){
		super(WizardryBlocks.crystal_flower);
	}

	@Override
	public EnumActionResult onItemUseFirst(Player player, Level world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, InteractionHand hand){

		if(world.getTileEntity(pos) instanceof TileEntityFlowerPot){

			TileEntityFlowerPot tileEntity = (TileEntityFlowerPot)world.getTileEntity(pos);

			if(tileEntity.getFlowerPotItem() == null || tileEntity.getFlowerPotItem() == Items.AIR){
				player.addStat(StatList.FLOWER_POTTED);
				if(!player.capabilities.isCreativeMode) player.getHeldItem(hand).shrink(1);
				world.setBlockState(pos, WizardryBlocks.crystal_flower_pot.getDefaultState());
				return EnumActionResult.SUCCESS;
			}
		}

		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

}
