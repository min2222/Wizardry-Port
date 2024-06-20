package electroblob.wizardry.client;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.gui.GuiSpellDisplay;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.PacketSpellQuickAccess;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Event handler class responsible for handling wizardry's controls. */
//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class WizardryControlHandler {

	static boolean NkeyPressed = false;
	static boolean BkeyPressed = false;
	static boolean[] quickAccessKeyPressed = new boolean[ClientProxy.SPELL_QUICK_ACCESS.length];

	// Changed to a tick event to allow mouse button keybinds
	// The 'lag' that happened previously was actually because the code only fired when a keyboard key was pressed!
	@SubscribeEvent
	public static void onTickEvent(TickEvent.ClientTickEvent event){

		if(event.phase == TickEvent.Phase.END) return; // Only really needs to be once per tick

		if(Wizardry.proxy instanceof ClientProxy){

			Player player = Minecraft.getInstance().player;

			if(player != null){

				ItemStack wand = getWandInUse(player);
				if(wand == null) return;

				if(ClientProxy.NEXT_SPELL.isDown() && Minecraft.getInstance().mouseHandler.isMouseGrabbed()){
					if(!NkeyPressed){
						NkeyPressed = true;
						selectNextSpell(wand);
					}
				}else{
					NkeyPressed = false;
				}

				if(ClientProxy.PREVIOUS_SPELL.isDown() && Minecraft.getInstance().mouseHandler.isMouseGrabbed()){
					if(!BkeyPressed){
						BkeyPressed = true;
						// Packet building
						selectPreviousSpell(wand);
					}
				}else{
					BkeyPressed = false;
				}

				for(int i = 0; i < ClientProxy.SPELL_QUICK_ACCESS.length; i++){
					if(ClientProxy.SPELL_QUICK_ACCESS[i].isDown() && Minecraft.getInstance().mouseHandler.isMouseGrabbed()){
						if(!quickAccessKeyPressed[i]){
							quickAccessKeyPressed[i] = true;
							// Packet building
							selectSpell(wand, i);
						}
					}else{
						quickAccessKeyPressed[i] = false;
					}
				}

			}
		}
	}
	
	// Shift-scrolling to change spells
	@SubscribeEvent
	public static void onMouseEvent(InputEvent.MouseScrollingEvent event){

		Player player = Minecraft.getInstance().player;
		ItemStack wand = getWandInUse(player);
		if(wand == null) return;

		if(Minecraft.getInstance().mouseHandler.isMouseGrabbed() && !wand.isEmpty() && event.getScrollDelta() != 0 && player.isShiftKeyDown()
				&& Wizardry.settings.shiftScrolling){

			event.setCanceled(true);
			
			int d = (int) (Wizardry.settings.reverseScrollDirection ? -event.getScrollDelta() : event.getScrollDelta());

			if(d > 0){
				selectNextSpell(wand);
			}else if(d < 0){
				selectPreviousSpell(wand);
			}
		}
	}

	private static ItemStack getWandInUse(Player player){

		ItemStack wand = player.getMainHandItem();

		// Only bother sending packets if the player is holding a spellcasting item with more than one spell slot
		if(!(wand.getItem() instanceof ISpellCastingItem) || ((ISpellCastingItem)wand.getItem()).getSpells(wand).length < 2){
			wand = player.getOffhandItem();
			if(!(wand.getItem() instanceof ISpellCastingItem) || ((ISpellCastingItem)wand.getItem()).getSpells(wand).length < 2) return null;
		}

		return wand;
	}
	
	private static void selectNextSpell(ItemStack wand){
		// Packet building
		PacketControlInput.Message msg = new PacketControlInput.Message(PacketControlInput.ControlType.NEXT_SPELL_KEY);
		WizardryPacketHandler.net.sendToServer(msg);
		// GUI switch animation
		((ISpellCastingItem)wand.getItem()).selectNextSpell(wand); // Makes sure the spell is set immediately for the client
		GuiSpellDisplay.playSpellSwitchAnimation(true);
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(WizardrySounds.ITEM_WAND_SWITCH_SPELL, 1));
	}
	
	private static void selectPreviousSpell(ItemStack wand){
		// Packet building
		PacketControlInput.Message msg = new PacketControlInput.Message(PacketControlInput.ControlType.PREVIOUS_SPELL_KEY);
		WizardryPacketHandler.net.sendToServer(msg);
		// GUI switch animation
		((ISpellCastingItem)wand.getItem()).selectPreviousSpell(wand); // Makes sure the spell is set immediately for the client
		GuiSpellDisplay.playSpellSwitchAnimation(false);
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(WizardrySounds.ITEM_WAND_SWITCH_SPELL, 1));
	}

	private static void selectSpell(ItemStack wand, int index){
		// GUI switch animation
		if(((ISpellCastingItem)wand.getItem()).selectSpell(wand, index)){ // Makes sure the spell is set immediately for the client
			// Packet building (no point sending it unless the client-side spell selection succeeded
			PacketSpellQuickAccess.Message msg = new PacketSpellQuickAccess.Message(index);
			WizardryPacketHandler.net.sendToServer(msg);

			GuiSpellDisplay.playSpellSwitchAnimation(true); // This will do, it's only an animation
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(WizardrySounds.ITEM_WAND_SWITCH_SPELL, 1));
		}
	}

}
