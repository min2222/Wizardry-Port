package electroblob.wizardry.command;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.command.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class CommandDiscoverSpell extends CommandBase {

	@Override
	public String getName(){
		return Wizardry.settings.discoverspellCommandName;
	}

	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 2;
	}

	/* @Override public boolean checkPermission(MinecraftServer server, ICommandSender sender){ // Only ops
	 * (multiplayer) or players with cheats enabled (singleplayer/LAN) can use /discoverspell. return !(sender
	 * instanceof EntityPlayer) ||
	 * server.getServer().getConfigurationManager().func_152596_g(((EntityPlayer)sender).getGameProfile()); } */

	@Override
	public String getUsage(ICommandSender sender){
		// Not ideal, but the way this is implemented means I have no choice. Only used in the help command, so in there
		// the custom command name will not display.
		return "commands." + Wizardry.MODID + ":discoverspell.usage";
		// return I18n.format("commands." + Wizardry.MODID + ":discoverspell.usage", Wizardry.settings.discoverspellCommandName);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] arguments,
			BlockPos pos){
		switch(arguments.length){
		case 1:
			return getListOfStringsMatchingLastWord(arguments, Spell.getSpellNames());
		case 2:
			return getListOfStringsMatchingLastWord(arguments, server.getOnlinePlayerNames());
		}
		return super.getTabCompletions(server, sender, arguments, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException{

		if(arguments.length < 1){
			throw new WrongUsageException("commands." + Wizardry.MODID + ":discoverspell.usage",
					Wizardry.settings.discoverspellCommandName);
		}else{

			int i = 0;
			boolean clear = false;
			boolean all = false;

			ServerPlayer player = null;

			try{
				player = getCommandSenderAsPlayer(sender);
			}catch (PlayerNotFoundException exception){
				// Nothing here since the player specifying is done later, I just don't want it to throw an exception
				// here.
			}

			Spell spell = Spells.none;

			if(arguments[i].equals("clear")){
				clear = true;
				i++;
			}else if(arguments[i].equals("all")){
				all = true;
				i++;
			}else{

				spell = Spell.get(arguments[i++]);

				if(spell == null){
					throw new NumberInvalidException("commands." + Wizardry.MODID + ":discoverspell.not_found",
							arguments[i - 1]);
				}
			}

			if(i < arguments.length){
				// If the second argument is a player and is not the player that gave the command, the spell is
				// discovered as the given player rather than the command sender.
				ServerPlayer entityplayermp = getPlayer(server, sender, arguments[i++]);
				if(player != entityplayermp){
					player = entityplayermp;
				}
			}

			// If, after this point, the player is still null, the sender must be a command block or the console and the
			// player must not have been specified, meaning an exception should be thrown.
			if(player == null)
				throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");

			WizardData data = WizardData.get(player);

			if(data != null){
				if(clear){
					data.spellsDiscovered.clear();
					if(server.sendCommandFeedback()) sender.sendMessage(
							Component.translatable("commands." + Wizardry.MODID + ":discoverspell.clear", player.getName()));
				}else if(all){
					data.spellsDiscovered.addAll(Spell.getAllSpells());
					if(server.sendCommandFeedback()) sender.sendMessage(
							Component.translatable("commands." + Wizardry.MODID + ":discoverspell.all", player.getName()));
				}else{
					if(data.hasSpellBeenDiscovered(spell)){
						data.spellsDiscovered.remove(spell);
						if(server.sendCommandFeedback()) sender.sendMessage(Component.translatable("commands." + Wizardry.MODID + ":discoverspell.removespell",
								spell.getNameForTranslationFormatted(), player.getName()));
					}else{
						if(!MinecraftForge.EVENT_BUS
								.post(new DiscoverSpellEvent(player, spell, DiscoverSpellEvent.Source.COMMAND))){
							data.discoverSpell(spell);
							if(server.sendCommandFeedback()) sender.sendMessage(Component.translatable("commands." + Wizardry.MODID + ":discoverspell.addspell",
									spell.getNameForTranslationFormatted(), player.getName()));
						}
					}
				}
				data.sync();
			}
		}
	}

}
