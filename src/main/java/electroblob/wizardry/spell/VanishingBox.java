package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.world.level.Level;

public class VanishingBox extends Spell {

	public VanishingBox(){
		super("vanishing_box", SpellActions.POINT_UP, false);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isClientSide){

			InventoryEnderChest enderchest = caster.getInventoryEnderChest();

			if(enderchest != null){
				caster.displayGUIChest(enderchest);
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
