package xreliquary.common.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import xreliquary.init.ModItems;
import xreliquary.items.MobCharmRegistry;

import static xreliquary.init.ModItems.MOB_CHAR_BELT_CONTAINER_TYPE;

public class ContainerMobCharmBelt extends Container {
	public ContainerMobCharmBelt(int windowId, PlayerInventory playerInventory, ItemStack belt) {
		super(MOB_CHAR_BELT_CONTAINER_TYPE.get(), windowId);
		this.belt = belt;

		for (int i = 0; i < getFirstPlayerInventoryIndex(); i++) {
			addSlot(new SlotMobCharm(belt, i));
		}

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 113 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k) {
			addSlot(new Slot(playerInventory, k, 8 + k * 18, 171));
		}
	}

	private final ItemStack belt;

	private static int getFirstPlayerInventoryIndex() {
		return MobCharmRegistry.getRegisteredNames().size() + 1;
	}

	@Override

	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		Slot slot = null;
		if (slotId >= 0 && slotId < inventorySlots.size()) {
			slot = inventorySlots.get(slotId);
		}
		ItemStack slotStack = slot == null ? ItemStack.EMPTY : slot.getStack();

		//prevent moving belt out of its slot
		if (slot != null && !slotStack.isEmpty() && slotStack.getItem() == ModItems.MOB_CHARM_BELT.get() && slotStack == player.getHeldItemMainhand()) {
			return ItemStack.EMPTY;
		}

		//overriden here so that on shift click it doesn't retry and thus move more charms out of belt
		if (slotId >= 0 && slotId < getFirstPlayerInventoryIndex() && clickTypeIn == ClickType.QUICK_MOVE && (dragType == 0 || dragType == 1)) {
			ItemStack itemstack = ItemStack.EMPTY;

			if (slot != null && slot.canTakeStack(player)) {
				if (!slotStack.isEmpty()) {
					itemstack = slotStack.copy();
				}

				ItemStack transferredStack = transferStackInSlot(player, slotId);

				if (!transferredStack.isEmpty()) {
					itemstack = transferredStack.copy();
				}
			}
			return itemstack;
		}

		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack copiedStack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack originalStack = slot.getStack();
			copiedStack = originalStack.copy();

			int playerInvIndex = getFirstPlayerInventoryIndex();

			if (index < playerInvIndex) {
				if (!mergeItemStack(originalStack, playerInvIndex, playerInvIndex + 36, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(originalStack, copiedStack);
			} else if (index < playerInvIndex + 36) {
				if (originalStack.getItem() == ModItems.MOB_CHARM.get()) {
					if (!mergeItemStack(originalStack, 0, playerInvIndex, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index < playerInvIndex + 27) {
					if (!mergeItemStack(originalStack, playerInvIndex + 27, playerInvIndex + 36, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!mergeItemStack(originalStack, playerInvIndex, playerInvIndex + 27, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (originalStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (originalStack.getCount() == copiedStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, originalStack);
		}

		return copiedStack;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	public ItemStack getBelt() {
		return belt;
	}

	public static ContainerMobCharmBelt fromBuffer(int windowId, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
		Hand hand = packetBuffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
		return new ContainerMobCharmBelt(windowId, playerInventory, playerInventory.player.getHeldItem(hand));
	}
}
