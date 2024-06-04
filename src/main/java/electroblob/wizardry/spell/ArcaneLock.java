package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber
public class ArcaneLock extends SpellRay {

	/** The NBT tag name for storing the owner's UUID in the tile entity data. */
	public static final String NBT_KEY = "arcaneLockOwner";

	public ArcaneLock(){
		super("arcane_lock", SpellActions.POINT, false);
	}

	@Override public boolean requiresPacket(){ return true; }

	@Override public boolean canBeCastBy(DispenserBlockEntity dispenser){ return false; }

	@Override public boolean canBeCastBy(Mob npc, boolean override){ return false; }

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		
		if(caster instanceof Player){

			if(!world.isClientSide){
				if(toggleLock(world, pos, (Player)caster)){
					BlockPos otherHalf = BlockUtils.getConnectedChest(world, pos);
					if(otherHalf != null) toggleLock(world, otherHalf, (Player)caster);
					return true;
				}
			}
		}
		
		return false;
	}

	private boolean toggleLock(Level world, BlockPos pos, Player player){

		BlockEntity tileentity = level.getTileEntity(pos);

		if(tileentity != null){

			if(tileentity.getTileData().hasUniqueId(NBT_KEY)){
				// Unlocking
				if(level.getPlayerEntityByUUID(tileentity.getTileData().getUniqueId(NBT_KEY)) == player){
					NBTExtras.removeUniqueId(tileentity.getTileData(), NBT_KEY);
					world.markAndNotifyBlock(pos, null, level.getBlockState(pos), level.getBlockState(pos), 3);
					return true;
				}
			}else{
				// Locking
				tileentity.getTileData().setUniqueId(NBT_KEY, player.getUniqueID());
				world.markAndNotifyBlock(pos, null, level.getBlockState(pos), level.getBlockState(pos), 3);
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	// Event Handlers

	@SubscribeEvent
	public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event){

		if(!canBypassLocks(event.getEntity())){

			BlockEntity tileentity = event.getWorld().getTileEntity(event.getPos());

			// Prevents arcane-locked containers from being opened
			// Need to check if it has the unique id first because if it is absent getUniqueId will return the nil UUID
			if(tileentity != null && tileentity.getTileData().hasUniqueId(ArcaneLock.NBT_KEY)){
				// Why is getUniqueId marked @Nullable? It literally creates a UUID and returns it!
				Player owner = event.getWorld().getPlayerEntityByUUID(tileentity.getTileData().getUniqueId(ArcaneLock.NBT_KEY));
				// Only the player that owns the lock or an ally of that player may open the container
				// If nobody owns it (i.e. it's part of a shrine, or the owner logged out), player will be null
				// Unfortunately we can't get the owner's allies if the owner is offline
				// Perhaps if this crops up again we can store inverted 'ally of' maps as well?
				if(owner == null || (owner != event.getEntity() && !AllyDesignationSystem.isPlayerAlly(owner, event.getEntity()))){
					event.setCanceled(true);
				}
			}
		}
	}

	// This event is the ideal, because it's seamless. For normal mining it works great, but it won't work for things
	// like the mine spell, mining lasers, destruction gadgets, etc. so we also need to intercept the general
	// block break event below, just in case.
	@SubscribeEvent
	public static void onLeftClickBlockEvent(PlayerInteractEvent.LeftClickBlock event){
		event.setCanceled(checkForLockedBlockBreak(event.getEntity(), event.getWorld(), event.getPos()));
	}

	@SubscribeEvent
	public static void onBlockBreakEvent(BlockEvent.BreakEvent event){
		event.setCanceled(checkForLockedBlockBreak(event.getPlayer(), event.getWorld(), event.getPos()));
	}

	// Yup, this spell even protects your chests from the enderdragon now!
	@SubscribeEvent
	public static void onLivingDestroyBlockEvent(LivingDestroyBlockEvent event){
		event.setCanceled(checkForLockedBlockBreak(event.getEntity(), event.getEntity().world, event.getPos()));
	}

	private static boolean checkForLockedBlockBreak(LivingEntity breaker, Level world, BlockPos pos){

		if(!(breaker instanceof Player) || !canBypassLocks((Player)breaker)){

			BlockEntity tileentity = level.getTileEntity(pos);

			// Prevents arcane-locked containers from being broken
			// Need to check if it has the unique id first because if it is absent getUniqueId will return the nil UUID
			if(tileentity != null && tileentity.getTileData().hasUniqueId(ArcaneLock.NBT_KEY)){
				// Only the player that owns the lock may break the container
				// If nobody owns it (i.e. it's part of a shrine), player will be null
				// Why is getUniqueId marked @Nullable? It literally creates a UUID and returns it!
				if(breaker.getUniqueID() != tileentity.getTileData().getUniqueId(ArcaneLock.NBT_KEY)){
					return true;
				}
			}
		}

		return false;
	}

	private static boolean canBypassLocks(Player player){
		if(!player.isCreative()) return false;
		if(Wizardry.settings.creativeBypassesArcaneLock) return true;
		MinecraftServer server = player.level.getMinecraftServer();
		return server != null && EntityUtils.isPlayerOp(player, server);
	}

	@SubscribeEvent
	public static void onExplosionEvent(ExplosionEvent.Detonate event){
		// Prevents arcane-locked containers from being exploded
		event.getAffectedBlocks().removeIf(pos -> event.getWorld().getTileEntity(pos) != null
				&& event.getWorld().getTileEntity(pos).getTileData().hasUniqueId(NBT_KEY));
	}

}
