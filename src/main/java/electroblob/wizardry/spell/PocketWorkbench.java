package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
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

		if(!level.isClientSide){
			caster.openGui(Wizardry.instance, WizardryGuiHandler.PORTABLE_CRAFTING, world, (int)caster.getX(),
					(int)caster.getY(), (int)caster.getZ());
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
