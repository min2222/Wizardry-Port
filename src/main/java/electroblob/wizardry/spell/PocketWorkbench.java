package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.EnumHand;
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
	public boolean cast(Level world, Player caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			caster.openGui(Wizardry.instance, WizardryGuiHandler.PORTABLE_CRAFTING, world, (int)caster.posX,
					(int)caster.posY, (int)caster.posZ);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
