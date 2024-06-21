package electroblob.wizardry.spell;

import electroblob.wizardry.inventory.ContainerPortableWorkbench;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PocketWorkbench extends Spell {

	public PocketWorkbench(){
		super("pocket_workbench", SpellActions.IMBUE, false);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isClientSide){
			caster.openMenu(new SimpleMenuProvider((p_52229_, p_52230_, p_52231_) -> {
		         return new ContainerPortableWorkbench(p_52229_, p_52230_);
		      }, Component.translatable("container.crafting")));
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
