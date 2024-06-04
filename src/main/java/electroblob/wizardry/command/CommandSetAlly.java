package electroblob.wizardry.command;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.command.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class CommandSetAlly extends CommandBase {

	@Override
	public String getName(){
		return Wizardry.settings.allyCommandName;
	}

	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 0;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender p_71519_1_){
		return true;
	}

	@Override
	public String getUsage(ICommandSender p_71518_1_){
		// Not ideal, but the way this is implemented means I have no choice. Only used in the help command, so in there
		// the custom command name will not display.
		return "commands." + Wizardry.MODID + ":ally.usage";
		// return I18n.format("commands." + Wizardry.MODID + ":ally.usage", Wizardry.settings.allyCommandName);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] arguments,
			BlockPos pos){
		switch(arguments.length){
		case 1:
			return getListOfStringsMatchingLastWord(arguments, server.getOnlinePlayerNames());
		case 2:
			return getListOfStringsMatchingLastWord(arguments, server.getOnlinePlayerNames());
		}
		return super.getTabCompletions(server, sender, arguments, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException{

		if(arguments.length < 1){
			throw new WrongUsageException("commands." + Wizardry.MODID + ":ally.usage", Wizardry.settings.allyCommandName);
		}else{

			ServerPlayer allyOf = null;

			try{
				allyOf = getCommandSenderAsPlayer(sender);
			}catch (PlayerNotFoundException exception){
				// Nothing here since the player specifying is done later, I just don't want it to throw an exception
				// here.
			}

			boolean executeAsOtherPlayer = false;

			ServerPlayer ally = getPlayer(server, sender, arguments[0]);
			// Don't want to catch the exception here, because the first player argument is always required.

			if(arguments.length > 1){

				allyOf = getPlayer(server, sender, arguments[1]);
				// Don't want to catch the exception here either, because there can be no other second argument.

				if(allyOf != sender && sender instanceof Player
						&& !EntityUtils.isPlayerOp((Player)sender, server)){
					// Displays a chat message if a non-op tries to modify another player's allies.
					if(server.sendCommandFeedback()){
						TextComponentTranslation TextComponentTranslation2 = Component.translatable(
								"commands." + Wizardry.MODID + ":ally.permission");
						TextComponentTranslation2.getStyle().setColor(ChatFormatting.RED);
						allyOf.sendMessage(TextComponentTranslation2);
					}
					return;
				}

				if(allyOf != sender) executeAsOtherPlayer = true;
			}

			// If, after this point, allyOf is still null, the sender must be a command block or the console and two
			// players must not have been specified, meaning an exception should be thrown.
			if(allyOf == null)
				throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");

			if(allyOf == ally) throw new NumberInvalidException("commands." + Wizardry.MODID + ":ally.self");

			if(server.sendCommandFeedback()){
				if(WizardData.get(allyOf) != null){
					String string = WizardData.get(allyOf).toggleAlly(ally) ? "add" : "remove";
					if(executeAsOtherPlayer){
						sender.sendMessage(Component.translatable("commands." + Wizardry.MODID + ":ally." + string + "ally",
								ally.getName(), allyOf.getName()));
						// In this case, the player whose allies have been modified is also notified.
						allyOf.sendMessage(Component.translatable("item.wand." + string + "ally", ally.getName()));
					}else{
						sender.sendMessage(Component.translatable("item.wand." + string + "ally", ally.getName()));
					}
				}
			}

		}
	}

}
