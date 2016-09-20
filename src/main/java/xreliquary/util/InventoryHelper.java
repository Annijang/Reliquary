package xreliquary.util;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InventoryHelper {

	public static void spawnItemStack(World worldIn, double x, double y, double z, ItemStack stack) {
		Random rnd = new Random();

		float f = rnd.nextFloat() * 0.8F + 0.1F;
		float f1 = rnd.nextFloat() * 0.8F + 0.1F;
		float f2 = rnd.nextFloat() * 0.8F + 0.1F;

		while(stack.stackSize > 0) {
			int i = rnd.nextInt(21) + 10;

			if(i > stack.stackSize) {
				i = stack.stackSize;
			}

			stack.stackSize -= i;

			ItemStack stackToDrop = stack.copy();
			stackToDrop.stackSize = i;
			EntityItem entityitem = new EntityItem(worldIn, x + (double) f, y + (double) f1, z + (double) f2, stackToDrop);

			float f3 = 0.05F;
			entityitem.motionX = rnd.nextGaussian() * (double) f3;
			entityitem.motionY = rnd.nextGaussian() * (double) f3 + 0.20000000298023224D;
			entityitem.motionZ = rnd.nextGaussian() * (double) f3;
			worldIn.spawnEntityInWorld(entityitem);
		}
	}

	public static ItemStack getTargetItem(ItemStack self, IInventory inventory, boolean disposeOfItem) {
		ItemStack targetItem = null;
		int itemQuantity = 0;
		for(int slot = 0; slot < inventory.getSizeInventory(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if(stack == null) {
				continue;
			}
			if(StackHelper.isItemAndNbtEqual(self, stack)) {
				continue;
			}
			if(stack.getMaxStackSize() == 1) {
				continue;
			}
			if(getItemQuantity(stack, inventory) > itemQuantity) {
				itemQuantity = getItemQuantity(stack, inventory);
				targetItem = stack.copy();

				if(disposeOfItem) {
					inventory.decrStackSize(slot, 1);
				}
			}
		}
		inventory.markDirty();
		return targetItem;
	}

	public static int getItemQuantity(ItemStack stack, IInventory inventory) {
		return InventoryHelper.getItemQuantity(stack, inventory, 0);
	}

	private static int getItemQuantity(ItemStack stack, IInventory inventory, int limit) {
		int itemQuantity = 0;
		for(int slot = 0; slot < Math.min(inventory.getSizeInventory(), (limit > 0 ? limit : inventory.getSizeInventory())); slot++) {
			ItemStack newStack = inventory.getStackInSlot(slot);
			if(newStack == null) {
				continue;
			}
			if(StackHelper.isItemAndNbtEqual(stack, newStack)) {
				itemQuantity += newStack.stackSize;
			}
		}
		return itemQuantity;
	}

	public static boolean consumeItem(Object item, EntityPlayer player) {
		return consumeItem(new Object[] {item}, player, 0, 1);
	}

	public static boolean consumeItem(Object item, EntityPlayer player, int minCount) {
		return consumeItem(new Object[] {item}, player, minCount, 1);
	}

	public static boolean consumeItem(Object item, EntityPlayer player, int minCount, int amountDecreased) {
		return consumeItem(new Object[] {item}, player, minCount, amountDecreased);
	}

	public static boolean consumeItem(Object[] itemList, EntityPlayer player, int minCount, int amountDecreased) {
		if(player.capabilities.isCreativeMode)
			return true;
		if(itemList.length == 0 || !(itemList[0] instanceof ItemStack || itemList[0] instanceof Item || itemList[0] instanceof Block))
			return false;
		List<Integer> suggestedSlots = new ArrayList<>();
		int itemCount = 0;
		for(int slot = 0; slot < player.inventory.mainInventory.length; slot++) {
			if(player.inventory.mainInventory[slot] == null) {
				continue;
			}

			ItemStack slotStack = player.inventory.mainInventory[slot];
			for(Object stack : itemList) {
				if((stack instanceof ItemStack && StackHelper.isItemAndNbtEqual(slotStack, (ItemStack) stack)) || (stack instanceof Block && RegistryHelper.itemsEqual(Item.getItemFromBlock((Block) stack), slotStack.getItem()) || (stack instanceof Item && RegistryHelper.itemsEqual((Item) stack, slotStack.getItem())))) {
					itemCount += player.inventory.mainInventory[slot].stackSize;
					suggestedSlots.add(slot);
				}
			}
		}
		int count = amountDecreased;
		if(suggestedSlots.size() > 0 && itemCount >= minCount + amountDecreased) {
			for(int slot : suggestedSlots) {
				//noinspection ConstantConditions
				int stackSize = player.inventory.getStackInSlot(slot).stackSize;
				if(stackSize >= count) {
					player.inventory.decrStackSize(slot, count);
					return true;
				} else {
					player.inventory.decrStackSize(slot, stackSize);
					count -= stackSize;
				}
			}
		}
		return false;
	}

	public static int tryToRemoveFromInventory(ItemStack contents, IInventory inventory, int maxToRemove) {
		int numberRemoved = 0;

		for(int slot = 0; slot < inventory.getSizeInventory(); slot++) {
			if(inventory.getStackInSlot(slot) == null) {
				continue;
			}
			//storage drawers compatibility loop
			while(inventory.getStackInSlot(slot) != null && inventory.getStackInSlot(slot).stackSize > 0 && StackHelper.isItemAndNbtEqual(inventory.getStackInSlot(slot), contents) && maxToRemove > numberRemoved) {
				//noinspection ConstantConditions
				numberRemoved += Math.min(maxToRemove - numberRemoved, inventory.getStackInSlot(slot).stackSize);
				//noinspection ConstantConditions
				inventory.decrStackSize(slot, Math.min(maxToRemove, inventory.getStackInSlot(slot).stackSize));
			}

			if(numberRemoved >= maxToRemove)
				return numberRemoved;
		}
		return numberRemoved;
	}

	public static int tryToAddToInventory(ItemStack contents, IInventory inventory, int maxToAdd) {
		return tryToAddToInventory(contents, inventory, maxToAdd, EnumFacing.UP);
	}

	public static int tryToAddToInventory(ItemStack contents, IInventory inventory, int maxToAdd, EnumFacing insertDirection) {
		int numberAdded = 0;

		if(inventory instanceof ISidedInventory) {
			ISidedInventory sidedInventory = (ISidedInventory) inventory;

			int[] slotsForFace = sidedInventory.getSlotsForFace(insertDirection);
			for(int slot : slotsForFace) {
				if(sidedInventory.getStackInSlot(slot) == null && sidedInventory.canInsertItem(slot, contents, insertDirection)) {
					numberAdded += insertIntoEmptySlot(contents, sidedInventory, slot, maxToAdd);
				} else if(StackHelper.isItemAndNbtEqual(inventory.getStackInSlot(slot), contents)) {
					//noinspection ConstantConditions
					if(inventory.getStackInSlot(slot).stackSize == Math.min(inventory.getStackInSlot(slot).getMaxStackSize(), inventory.getInventoryStackLimit())) {
						continue;
					}
					int stackAddition = addToNonEmptySlot(inventory, maxToAdd, numberAdded, slot);
					numberAdded += stackAddition;
				}
				if(numberAdded >= maxToAdd)
					break;
			}

		} else {
			for(int slot = 0; slot < Math.min(inventory.getSizeInventory(), inventory.getSizeInventory()); slot++) {
				if(inventory.getStackInSlot(slot) == null && inventory.isItemValidForSlot(slot, contents)) {
					numberAdded += insertIntoEmptySlot(contents, inventory, slot, maxToAdd);
				} else if(StackHelper.isItemAndNbtEqual(inventory.getStackInSlot(slot), contents)) {
					//noinspection ConstantConditions
					if(inventory.getStackInSlot(slot).stackSize == Math.min(inventory.getStackInSlot(slot).getMaxStackSize(), inventory.getInventoryStackLimit())) {
						continue;
					}
					int stackAddition = addToNonEmptySlot(inventory, maxToAdd, numberAdded, slot);
					numberAdded += stackAddition;
				}
				if(numberAdded >= maxToAdd)
					break;
			}
		}

		if(numberAdded > 0)
			inventory.markDirty();

		return numberAdded;
	}

	private static int addToNonEmptySlot(IInventory inventory, int maxToAdd, int numberAdded, int slot) {
		ItemStack slotStack = inventory.getStackInSlot(slot);
		//noinspection ConstantConditions
		int stackAddition = Math.min(Math.min(slotStack.getMaxStackSize(), inventory.getInventoryStackLimit()) - slotStack.stackSize, maxToAdd - numberAdded);
		slotStack.stackSize += stackAddition;
		return stackAddition;
	}

	private static int insertIntoEmptySlot(ItemStack contents, IInventory inventory, int slot, int maxToAdd) {
		int numberAdded = 0;
		//loop because of storage drawers like inventories
		while(inventory.getStackInSlot(slot) == null && maxToAdd > numberAdded) {
			int stackAddition = Math.min(Math.min(contents.getMaxStackSize(), inventory.getInventoryStackLimit()), maxToAdd - numberAdded);
			ItemStack newContents = contents.copy();
			newContents.stackSize = stackAddition;
			inventory.setInventorySlotContents(slot, newContents);
			numberAdded += stackAddition;
		}
		return numberAdded;
	}

	private static int insertIntoEmptySlot(ItemStack contents, ISidedInventory inventory, int slot, int maxToAdd) {
		int numberAdded = 0;
		ItemStack stackInSlot = inventory.getStackInSlot(slot);

		//loop because of storage drawers like inventories
		while((stackInSlot == null || stackInSlot.stackSize > stackInSlot.getMaxStackSize()) && maxToAdd > numberAdded) {
			int maxSlotAddition = stackInSlot == null ? contents.getMaxStackSize() : stackInSlot.getMaxStackSize() - stackInSlot.stackSize;
			int stackAddition = Math.min(maxSlotAddition, maxToAdd - numberAdded);
			ItemStack newContents = contents.copy();
			newContents.stackSize = stackAddition;
			inventory.setInventorySlotContents(slot, newContents);
			numberAdded += stackAddition;
			stackInSlot = inventory.getStackInSlot(slot);
		}
		return numberAdded;
	}

	public static void tryRemovingLastStack(IInventory inventory, World worldObj, BlockPos pos) {
		for(int i = inventory.getSizeInventory() - 1; i >= 0; i--) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack != null) {
				inventory.setInventorySlotContents(i, null);
				if(worldObj.isRemote)
					return;
				inventory.markDirty();
				EntityItem itemEntity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, stack);
				worldObj.spawnEntityInWorld(itemEntity);
				break;
			}
		}
	}

	public static boolean tryAddingPlayerCurrentItem(EntityPlayer player, IInventory inventory, EnumHand hand) {
		if(inventory.getStackInSlot(0) != null)
			return false;

		//noinspection ConstantConditions
		inventory.setInventorySlotContents(0, player.getHeldItem(hand).copy());

		player.getHeldItem(hand).stackSize--;

		if(player.getHeldItem(hand).stackSize == 0)
			player.setHeldItem(hand, null);

		player.inventory.markDirty();
		return true;
	}
}
