package electroblob.wizardry.tileentity;

import java.util.HashSet;
import java.util.Set;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.item.ItemCrystal;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityArcaneWorkbench extends BaseContainerBlockEntity {

	/** The inventory of the arcane workbench. */
	private NonNullList<ItemStack> inventory;
	/** Controls the rotating rune and floating wand animations. */
	public float timer = 0;

	private boolean doNotSync;

	public TileEntityArcaneWorkbench(BlockPos pos, BlockState state){
        super(WizardryBlocks.ARCANE_WORKBENCH_BLOCK_ENTITY.get(), pos, state);
		inventory = NonNullList.withSize(ContainerArcaneWorkbench.UPGRADE_SLOT + 1, ItemStack.EMPTY);
		// Prevent sync() happening when loading from NBT the first time or weirdness ensues when loading a world
		// Normally I'd pass this as a flag to setInventorySlotContents but we can't change the method signature
		this.doNotSync = true;
	}

	@Override
	public void onLoad(){
		timer = 0;
	}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		if(!doNotSync) this.level.markAndNotifyBlock(worldPosition, this.level.getChunkAt(worldPosition), level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3, 512);
	}

	public static void update(Level world, BlockPos pos, BlockState state, TileEntityArcaneWorkbench tileEntity) {

		tileEntity.doNotSync = false;

		ItemStack stack = tileEntity.getItem(ContainerArcaneWorkbench.CENTRE_SLOT);

		// Decrements wand damage (increases mana) every 1.5 seconds if it has a condenser upgrade
		if(stack.getItem() instanceof IManaStoringItem && !world.isClientSide && !((IManaStoringItem)stack.getItem()).isManaFull(stack)
				&& world.getGameTime() % electroblob.wizardry.constants.Constants.CONDENSER_TICK_INTERVAL == 0){
			// If the upgrade level is 0, this does nothing anyway.
			((IManaStoringItem)stack.getItem()).rechargeMana(stack, WandHelper.getUpgradeLevel(stack, WizardryItems.CONDENSER_UPGRADE.get()));
		}

		// The server doesn't care what these are, and there's no need for them to be synced or saved.
		if(world.isClientSide){
			tileEntity.timer++;
		}
	}

	@Override
	public int getContainerSize(){
		return inventory.size();
	}

	@Override
	public ItemStack getItem(int slot){
		return inventory.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount){
		
		ItemStack stack = getItem(slot);
		
		if(!stack.isEmpty()){
			if(stack.getCount() <= amount){
				setItem(slot, ItemStack.EMPTY);
			}else{
				stack = stack.split(amount);
				if(stack.getCount() == 0){
					setItem(slot, ItemStack.EMPTY);
				}
			}
			this.setChanged();
		}
		
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot){
		
		ItemStack stack = getItem(slot);
		
		if(!stack.isEmpty()){
			setItem(slot, ItemStack.EMPTY);
		}
		
		return stack;
	}

	@Override
	public void setItem(int slot, ItemStack stack){

		ItemStack previous = inventory.set(slot, stack);

		// Only the central slot affects the in-world rendering, so only sync if that changes
		// This must be done in the tile entity because containers only exist for player interaction, not hoppers etc.
		if(slot == ContainerArcaneWorkbench.CENTRE_SLOT && previous.isEmpty() != stack.isEmpty()) this.sync();
		
		if(!stack.isEmpty() && stack.getCount() > getMaxStackSize()){
			stack.setCount(getMaxStackSize());
		}
	}

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container." + Wizardry.MODID + ".arcane_workbench");
    }

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public int getMaxStackSize(){
		return 64;
	}

	@Override
	public boolean stillValid(Player player){
		return level.getBlockEntity(worldPosition) == this && player.distanceToSqr(Vec3.atCenterOf(worldPosition)) < 64;
	}

	@Override
	public void startOpen(Player player){

	}

	@Override
	public void stopOpen(Player player){

	}

	@Override
	public boolean canPlaceItem(int slotNumber, ItemStack itemstack){

		if(itemstack == ItemStack.EMPTY) return true;

		if(slotNumber >= 0 && slotNumber < ContainerArcaneWorkbench.CRYSTAL_SLOT){

			if(!(itemstack.getItem() instanceof ItemSpellBook)) return false;

			ItemStack centreStack = getItem(ContainerArcaneWorkbench.CENTRE_SLOT);

			if(centreStack.getItem() instanceof IWorkbenchItem){
				int spellSlots = ((IWorkbenchItem)centreStack.getItem()).getSpellSlotCount(centreStack);
				return slotNumber < spellSlots;
			}

			return false;

		}else if(slotNumber == ContainerArcaneWorkbench.CRYSTAL_SLOT){
			return itemstack.getItem() instanceof ItemCrystal;

		}else if(slotNumber == ContainerArcaneWorkbench.CENTRE_SLOT){
			return itemstack.getItem() instanceof IWorkbenchItem;

		}else if(slotNumber == ContainerArcaneWorkbench.UPGRADE_SLOT){
			Set<Item> upgrades = new HashSet<>(WandHelper.getSpecialUpgrades());
			upgrades.add(WizardryItems.ARCANE_TOME.get());
			upgrades.add(WizardryItems.RESPLENDENT_THREAD.get());
			upgrades.add(WizardryItems.CRYSTAL_SILVER_PLATING.get());
			upgrades.add(WizardryItems.ETHEREAL_CRYSTALWEAVE.get());
			return upgrades.contains(itemstack.getItem());
		}

		return true;

	}

	@Override
	public void load(CompoundTag tagCompound){

		super.load(tagCompound);

		ListTag tagList = tagCompound.getList("Inventory", Tag.TAG_LIST);
		for(int i = 0; i < tagList.size(); i++){
			CompoundTag tag = tagList.getCompound(i);
			byte slot = tag.getByte("Slot");
			if(slot >= 0 && slot < getContainerSize()){
				setItem(slot,ItemStack.of(tag));
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag tagCompound){

		super.saveAdditional(tagCompound);

		ListTag itemList = new ListTag();
		for(int i = 0; i < getContainerSize(); i++){
			ItemStack stack = getItem(i);
			CompoundTag tag = new CompoundTag();
			tag.putByte("Slot", (byte)i);
			stack.save(tag);
			itemList.add(tag);
		}

		NBTExtras.storeTagSafely(tagCompound, "Inventory", itemList);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public AABB getRenderBoundingBox(){
		AABB bb = INFINITE_EXTENT_AABB;
		Block type = getBlockState().getBlock();
		if(type == WizardryBlocks.ARCANE_WORKBENCH.get()){
			bb = new AABB(worldPosition, worldPosition.offset(1, 1, 1));
		}else if(type != null){
			AABB cbb = this.getLevel().getBlockState(worldPosition).getCollisionShape(level, worldPosition).bounds();
			if(cbb != null){
				bb = cbb;
			}
		}
		return bb;
	}
	
    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new ContainerArcaneWorkbench(id, inventory, this);
    }

	@Override
	public void clearContent(){
		for(int i = 0; i < getContainerSize(); i++){
			setItem(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean isEmpty(){
		for(int i = 0; i < getContainerSize(); i++){
			if(!getItem(i).isEmpty()){
				return false;
			}
		}
		return true;
	}

}
