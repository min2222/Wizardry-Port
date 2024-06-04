package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.ResurrectionEvent;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.packet.PacketResurrection;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Arrays;
import java.util.Comparator;

@EventBusSubscriber
public class Resurrection extends Spell {

	public static final String WAIT_TIME = "wait_time";

	private static final String POTION_CORE_FIX_NBT_KEY = "Potion Core - Health Fix";

	public Resurrection(){
		super("resurrection", SpellActions.SUMMON, false);
		addProperties(EFFECT_RADIUS, WAIT_TIME);
	}

	@Override
	public boolean requiresPacket(){
		return false; // Has its own packets
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData data = WizardData.get(caster);

		double radius = getProperty(EFFECT_RADIUS).doubleValue() * modifiers.get(WizardryItems.range_upgrade);

		if(!world.isClientSide && caster.getServer() != null){
			// Potency reduces the time you have to wait to resurrect an ally
			int waitTime = (int)(getProperty(WAIT_TIME).floatValue() / modifiers.get(SpellModifiers.POTENCY));

			ServerPlayer nearestDeadAlly = caster.getServer().getPlayerList().getPlayers().stream()
					.filter(p -> !p.isEntityAlive() && p.deathTime > waitTime && (data.isPlayerAlly(p) || caster == p)
							&& p.distanceToSqr(caster) < radius * radius)
					.min(Comparator.comparingDouble(caster::distanceToSqr))
					.orElse(null);

			if(nearestDeadAlly != null){

				if(MinecraftForge.EVENT_BUS.post(new ResurrectionEvent(nearestDeadAlly, caster))) return false;

				// When the player entity dies, it is removed from world#loadedEntityList. However, it is NOT removed
				// from playerEntityList (and probably a few other places) until respawn is clicked, and since that
				// never happens here we need to clean up those references or the player will have duplicate entries
				// in some entity lists - and weirdness will ensue!
				world.removeEntity(nearestDeadAlly); // Clean up the old entity references
				resurrect(nearestDeadAlly); // Reset isDead, must be before spawning the player again
				world.addFreshEntity(nearestDeadAlly); // Re-add the player to all the relevant entity lists

				// Notify clients to reset the appropriate fields, spawn particles and play sounds
				IMessage msg = new PacketResurrection.Message(nearestDeadAlly.getEntityId());
				WizardryPacketHandler.net.sendToDimension(msg, caster.dimension);

				if(caster == nearestDeadAlly){
					caster.getServer().getPlayerList().sendMessage(Component.translatable(
							"spell." + this.getRegistryName() + ".resurrect_self", caster.getDisplayName()));
				}else{
					caster.getServer().getPlayerList().sendMessage(Component.translatable(
							"spell." + this.getRegistryName() + ".resurrect_ally", nearestDeadAlly.getDisplayName(), caster.getDisplayName()));
				}

				return true;
			}
		}

		return false;
	}

	/** Sets the given player back to alive, sets their health to half-full and (on the client) spawns particles. */
	public void resurrect(Player player){

		player.isDead = false;
		player.setHealth(player.getMaxHealth() / 2);
		// Experience doesn't normally get reset until respawn, so we need to do that here too
		player.deathTime = 0;
		player.experience = 0;
		player.experienceLevel = 0;
		player.experienceTotal = 0;
		// Not sure what potion core is 'fixing' but it breaks my resurrection, so let's unfix it!
		player.getPersistentData().removeTag(POTION_CORE_FIX_NBT_KEY);

		if(player.world.isClientSide){
			ParticleBuilder.spawnHealParticles(player.world, player);
			this.playSound(player.world, player, 0, -1, null); // We know the modifiers parameter isn't used
		}
	}

	public static int getRemainingWaitTime(int timeSinceDeath){
		return Math.max(0, Mth.ceil((Spells.resurrection.getProperty(Resurrection.WAIT_TIME).floatValue() - timeSinceDeath) / 20));
	}

	/** Helper method for detecting if a stack can be used to cast the resurrection spell. */
	public static boolean canStackResurrect(ItemStack stack, Player player){
		return stack.getItem() instanceof ISpellCastingItem
				&& Arrays.asList(((ISpellCastingItem)stack.getItem()).getSpells(stack)).contains(Spells.resurrection)
				&& ((ISpellCastingItem)stack.getItem()).canCast(stack, Spells.resurrection, player, InteractionHand.MAIN_HAND, 0, new SpellModifiers());
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(LivingEvent.LivingTickEvent event){

		if(event.getEntity() instanceof Player){

			Player player = (Player)event.getEntity();

			if(player.world.isClientSide && !player.isEntityAlive()){

				Player firstPersonPlayer = Wizardry.proxy.getThePlayer();

				if(WizardData.get(firstPersonPlayer).isPlayerAlly(player)){

					ItemStack wand = firstPersonPlayer.getMainHandItem();

					if(!(wand.getItem() instanceof ISpellCastingItem)){
						wand = firstPersonPlayer.getOffHandItem();
						if(!(wand.getItem() instanceof ISpellCastingItem)) return;
					}

					if(((ISpellCastingItem)wand.getItem()).getCurrentSpell(wand) == Spells.resurrection){

						// TODO: Find some way of getting at the modifiers before the spell is cast
						int waitTime = Spells.resurrection.getProperty(WAIT_TIME).intValue();

						if(player.deathTime > waitTime){
							ParticleBuilder.create(Type.SPARKLE, player.world.rand, player.getX(), player.getY() + 0.5, player.getZ(), 0.5, false)
									.clr(1, 1, 0.3f).vel(0, 0.02, 0).spawn(player.world);
						}else{
							ParticleBuilder.create(Type.DUST, player.world.rand, player.getX(), player.getY() + 0.5, player.getZ(), 0.5, false)
									.clr(1, 1, 0.3f).vel(0, 0.02, 0).time(50).spawn(player.world);
						}
					}
				}
			}
		}
	}

}