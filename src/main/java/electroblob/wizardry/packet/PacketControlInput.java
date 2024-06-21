package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.ResurrectionEvent;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Possession;
import electroblob.wizardry.spell.Resurrection;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Client -> Server]</b> This packet is for control events such as buttons in GUIs and key presses. */
public class PacketControlInput {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isServer()){

			final ServerPlayer player = ctx.get().getSender();

			ctx.get().enqueueWork(() -> {
				ItemStack wand = player.getMainHandItem();

				if(!(wand.getItem() instanceof ISpellCastingItem)){
					wand = player.getOffhandItem();
				}

				switch(message.controlType){

				case APPLY_BUTTON:

					if(!(player.containerMenu instanceof ContainerArcaneWorkbench)){
						Wizardry.logger.warn("Received a PacketControlInput, but the player that sent it was not " +
								"currently using an arcane workbench. This should not happen!");
					}else{
						((ContainerArcaneWorkbench)player.containerMenu).onApplyButtonPressed(player);
					}

					break;

				case CLEAR_BUTTON:

					if(!(player.containerMenu instanceof ContainerArcaneWorkbench)){
						Wizardry.logger.warn("Received a PacketControlInput, but the player that sent it was not " +
								"currently using an arcane workbench. This should not happen!");
					}else{
						((ContainerArcaneWorkbench)player.containerMenu).onClearButtonPressed(player);
					}

					break;

				case NEXT_SPELL_KEY:

					if(wand.getItem() instanceof ISpellCastingItem){

						((ISpellCastingItem)wand.getItem()).selectNextSpell(wand);
						// This line fixes the bug with continuous spells casting when they shouldn't be
						player.stopUsingItem();
					}

					break;

				case PREVIOUS_SPELL_KEY:

					if(wand.getItem() instanceof ISpellCastingItem){

						((ISpellCastingItem)wand.getItem()).selectPreviousSpell(wand);
						// This line fixes the bug with continuous spells casting when they shouldn't be
						player.stopUsingItem();
					}

					break;

				case RESURRECT_BUTTON:

					if(!player.isAlive() && Resurrection.getRemainingWaitTime(player.deathTime) == 0){

						ItemStack stack = InventoryUtils.getHotbar(player).stream()
								.filter(s -> Resurrection.canStackResurrect(s, player)).findFirst().orElse(null);

						if(stack != null){
							if(MinecraftForge.EVENT_BUS.post(new ResurrectionEvent(player, player))) break;
							// This should suffice, since this is the only way a player can cast resurrection when dead!
							((ISpellCastingItem)stack.getItem()).cast(stack, Spells.RESURRECTION, player, InteractionHand.MAIN_HAND, 0, new SpellModifiers());
							break;
						}
					}

					Wizardry.logger.warn("Received a resurrect button packet, but the player that sent it was not" +
							" currently able to resurrect. This should not happen!");

					break;

				case CANCEL_RESURRECT:

					if(player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) break; // Shouldn't even receive this

					if(!player.isAlive()){

						ItemStack stack = InventoryUtils.getHotbar(player).stream()
								.filter(s -> Resurrection.canStackResurrect(s, player)).findFirst().orElse(null);

						if(stack != null){
							player.drop(stack, true, false);
							player.getInventory().removeItem(stack); // Might as well
							break;
						}

						Wizardry.logger.warn("Received a cancel resurrect packet, but the player that sent it was not" +
								" holding a wand with the resurrection spell. This should not happen!");
					}

					Wizardry.logger.warn("Received a cancel resurrect packet, but the player that sent it was not" +
							" currently dead. This should not happen!");

					break;

				case POSSESSION_PROJECTILE:

					if(!Possession.isPossessing(player)) Wizardry.logger.warn("Received a possession projectile packet, " +
							"but the player that sent it is not currently possessing anything!");

					Possession.shootProjectile(player);

					break;
				}
			});
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public enum ControlType {
		APPLY_BUTTON, NEXT_SPELL_KEY, PREVIOUS_SPELL_KEY, RESURRECT_BUTTON, CANCEL_RESURRECT, POSSESSION_PROJECTILE, CLEAR_BUTTON
	}

	public static class Message {

		private ControlType controlType;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(ControlType type){
			this.controlType = type;
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			this.controlType = ControlType.values()[buf.readInt()];
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(controlType.ordinal());
		}
	}
}
