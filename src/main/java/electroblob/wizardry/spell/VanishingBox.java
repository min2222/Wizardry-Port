package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.Level;

public class VanishingBox extends Spell {

	public VanishingBox(){
		super("vanishing_box", SpellActions.POINT_UP, false);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isClientSide){

			PlayerEnderChestContainer enderchest = caster.getEnderChestInventory();

			if(enderchest != null){
	            caster.openMenu(new SimpleMenuProvider((p_53124_, p_53125_, p_53126_) -> {
	                return ChestMenu.threeRows(p_53124_, p_53125_, enderchest);
	            }, Component.translatable("container.enderchest")));
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
