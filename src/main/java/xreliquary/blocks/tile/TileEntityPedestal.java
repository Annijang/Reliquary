package xreliquary.blocks.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import xreliquary.api.IPedestal;
import xreliquary.api.IPedestalActionItem;
import xreliquary.util.InventoryHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileEntityPedestal extends TileEntityInventory implements IPedestal {

	private boolean tickable = false;
	private int[] actionCooldowns;
	private int currentItemIndex;
	private List<ItemStack> actionItems = new ArrayList<>();

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);

		NBTTagList items = tag.getTagList("Items", 10);

		this.inventory = new ItemStack[this.getSizeInventory()];

		for(int i = 0; i < items.tagCount(); ++i) {
			NBTTagCompound item = items.getCompoundTagAt(i);
			byte b0 = item.getByte("Slot");

			if(b0 >= 0 && b0 < this.inventory.length) {
				this.inventory[b0] = ItemStack.loadItemStackFromNBT(item);
			}
		}

		updateSpecialItems();
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		NBTTagList items = new NBTTagList();

		for(int i = 0; i < this.inventory.length; ++i) {
			if(this.inventory[i] != null) {
				NBTTagCompound item = new NBTTagCompound();
				this.inventory[i].writeToNBT(item);
				item.setByte("Slot", (byte) i);
				items.appendTag(item);
			}
		}
		tag.setTag("Items", items);
	}

	public TileEntityPedestal() {
		super(1);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		super.setInventorySlotContents(slot, stack);
		updateSpecialItems();
	}

	private void updateSpecialItems() {
		tickable = false;
		actionItems.clear();
		for (ItemStack item : inventory) {
			if(item != null && item.getItem() instanceof IPedestalActionItem) {
				tickable = true;
				actionItems.add(item);
			}
		}
		actionCooldowns = new int[actionItems.size()];
		Arrays.fill(actionCooldowns, 0);
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void update() {
		if (tickable) {
			for(currentItemIndex =0; currentItemIndex<actionItems.size(); currentItemIndex++) {
				if (actionCooldowns[currentItemIndex] > 0) {
					actionCooldowns[currentItemIndex]--;
				} else {
					ItemStack item = actionItems.get(currentItemIndex);
					IPedestalActionItem actionItem = (IPedestalActionItem) item.getItem();
					actionItem.update(item, this);
				}
			}
		}
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		return null;
	}

	@Override
	public int addToConnectedInventory(ItemStack stack) {
		List<IInventory> adjacentInventories = getAdjacentInventories();

		int sizeAdded = 0;
		for(IInventory inventory : adjacentInventories) {
			sizeAdded += InventoryHelper.tryToAddToInventory(stack, inventory, 0, stack.stackSize);

			if(sizeAdded >= stack.stackSize)
				break;
		}

		return sizeAdded;
	}

	@Override
	public void setActionCoolDown(int coolDownTicks) {
		actionCooldowns[currentItemIndex] = coolDownTicks;
	}

	public List<IInventory> getAdjacentInventories() {
		BlockPos south = this.getPos().add(EnumFacing.SOUTH.getDirectionVec());
		BlockPos north = this.getPos().add(EnumFacing.NORTH.getDirectionVec());
		BlockPos east = this.getPos().add(EnumFacing.EAST.getDirectionVec());
		BlockPos west = this.getPos().add(EnumFacing.WEST.getDirectionVec());

		List<IInventory> adjacentInventories = new ArrayList<>();

		IInventory inventory = (IInventory) worldObj.getTileEntity(south);
		if (inventory != null)
			adjacentInventories.add(inventory);

		inventory = (IInventory) worldObj.getTileEntity(north);
		if (inventory != null)
			adjacentInventories.add(inventory);

		inventory = (IInventory) worldObj.getTileEntity(east);
		if (inventory != null)
			adjacentInventories.add(inventory);

		inventory = (IInventory) worldObj.getTileEntity(west);
		if (inventory != null)
			adjacentInventories.add(inventory);

		return adjacentInventories;
	}
}
