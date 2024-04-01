package reliquary.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import reliquary.items.ToggleableItem;
import reliquary.items.util.ICuriosItem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class InventoryHelper {
	private InventoryHelper() {
	}

	private static final Set<BiFunction<Player, ICuriosItem.Type, IItemHandler>> baublesItemHandlerFactories = new HashSet<>();

	public static void addBaublesItemHandlerFactory(BiFunction<Player, ICuriosItem.Type, IItemHandler> factory) {
		baublesItemHandlerFactories.add(factory);
	}

	public static void spawnItemStack(Level world, BlockPos pos, ItemStack stack) {
		Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
	}

	public static ItemStack getTargetItem(ItemStack self, IItemHandler inventory) {
		if (self.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack targetItem = ItemStack.EMPTY;
		int itemQuantity = 0;
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (ItemHandlerHelper.canItemStacksStack(self, stack) || stack.getMaxStackSize() == 1) {
				continue;
			}
			if (getItemQuantity(stack, inventory) > itemQuantity) {
				itemQuantity = getItemQuantity(stack, inventory);
				targetItem = stack.copy();
			}
		}
		return targetItem;
	}

	public static int getItemQuantity(ItemStack stack, IItemHandler inventory) {
		if (stack.isEmpty()) {
			return 0;
		}

		int itemQuantity = 0;
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack newStack = inventory.getStackInSlot(slot);
			if (ItemHandlerHelper.canItemStacksStack(stack, newStack)) {
				itemQuantity += newStack.getCount();
			}
		}
		return itemQuantity;
	}

	public static ItemStack consumeItemStack(Predicate<ItemStack> itemMatches, Player player, int count) {
		return extractFromInventory(itemMatches, count, getItemHandlerFrom(player), false);
	}

	public static ItemStack extractFromInventory(Predicate<ItemStack> itemMatches, int count, IItemHandler inventory, boolean simulate) {
		ItemStack ret = ItemStack.EMPTY;
		int slots = inventory.getSlots();
		for (int slot = 0; slot < slots && ret.getCount() < count; slot++) {
			ItemStack slotStack = inventory.getStackInSlot(slot);
			if (itemMatches.test(slotStack) && (ret.isEmpty() || ItemHandlerHelper.canItemStacksStack(ret, slotStack))) {
				int toExtract = Math.min(slotStack.getCount(), count - ret.getCount());
				ItemStack extractedStack = inventory.extractItem(slot, toExtract, simulate);
				if (ret.isEmpty()) {
					ret = extractedStack;
				} else {
					ret.setCount(ret.getCount() + extractedStack.getCount());
				}
			}
		}
		return ret;
	}

	public static boolean consumeItem(ItemStack itemStack, Player player, int minCount, int countToConsume) {
		if (player.isCreative()) {
			return true;
		}
		if (itemStack.isEmpty() || countToConsume <= 0) {
			return false;
		}

		int itemCount = 0;

		List<Map.Entry<Integer, Integer>> slotCounts = new ArrayList<>();
		for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
			ItemStack slotStack = player.getInventory().items.get(slot);
			if (ItemHandlerHelper.canItemStacksStack(slotStack, itemStack)) {
				int stackSize = slotStack.getCount();
				itemCount += stackSize;
				slotCounts.add(new AbstractMap.SimpleEntry<>(slot, stackSize));
			}
		}

		if (itemCount - countToConsume < minCount) {
			return false;
		}

		//fill stacks based on which ones have the highest sizes
		if (itemCount >= countToConsume) {
			slotCounts.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

			int countToFill = itemCount - countToConsume;

			for (Map.Entry<Integer, Integer> slotCount : slotCounts) {
				int slot = slotCount.getKey();

				//fill stack sizes up to remaining value
				if (countToFill > 0) {
					int stackSizeToFill = Math.min(itemStack.getMaxStackSize(), countToFill);

					player.getInventory().getItem(slot).setCount(stackSizeToFill);

					countToFill -= stackSizeToFill;
				} else {
					player.getInventory().removeItem(slot, player.getInventory().getItem(slot).getCount());
				}
			}
			return true;
		}

		return false;
	}

	public static int tryToRemoveFromInventory(ItemStack contents, IItemHandler inventory, int maxToRemove) {
		int remaining = maxToRemove;

		ItemStack stackToExtract = contents.copy();
		int currentStackCount = Math.min(remaining, stackToExtract.getMaxStackSize());
		stackToExtract.setCount(currentStackCount);

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			if (inventory.getStackInSlot(slot).isEmpty()) {
				continue;
			}

			//storage drawers compatibility loop
			while (inventory.getStackInSlot(slot).getCount() > 0 && ItemHandlerHelper.canItemStacksStack(inventory.getStackInSlot(slot), contents) && remaining > 0) {
				ItemStack extractedStack = inventory.extractItem(slot, Math.min(maxToRemove, inventory.getStackInSlot(slot).getCount()), false);
				if (extractedStack.getCount() == 0) {
					break; //just in case some item handler shows stacks that can't be extracted
				}

				remaining -= extractedStack.getCount();

				stackToExtract = contents.copy();
				currentStackCount = Math.min(remaining, stackToExtract.getMaxStackSize());
				stackToExtract.setCount(currentStackCount);
			}

			if (remaining <= 0) {
				break;
			}
		}
		return maxToRemove - remaining;
	}

	public static void runOnInventoryAt(Level world, BlockPos pos, Consumer<IItemHandler> run) {
		IItemHandler itemHandler = getInventoryAtPos(world, pos, null);
		if (itemHandler == null) {
			return;
		}
		run.accept(itemHandler);
	}

	@Nullable
	public static IItemHandler getInventoryAtPos(Level level, BlockPos pos, @Nullable Direction side) {
		return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
	}

	@Nullable
	public static IItemHandler getItemHandlerFrom(Player player, @Nullable Direction side) {
		return player.getCapability(Capabilities.ItemHandler.ENTITY);
	}

	public static IItemHandler getItemHandlerFrom(Player player) {
		return new PlayerMainInvWrapper(player.getInventory());
	}

	public static void executeOnItemHandlerAt(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Consumer<IItemHandler> run) {
		executeOnItemHandlerAt(level, pos, state, blockEntity, handler -> {
			run.accept(handler);
			return null;
		}, null);
	}

	public static <T> T executeOnItemHandlerAt(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Function<IItemHandler, T> run, @Nullable T defaultReturnValue) {
		return executeOnItemHandlerAt(level, pos, state, blockEntity, null, run, defaultReturnValue);
	}

	private static <T> T executeOnItemHandlerAt(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side, Function<IItemHandler, T> run, @Nullable T defaultReturnValue) {
		IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, blockEntity, side);

		if (itemHandler != null) {
			return run.apply(itemHandler);
		}

		//noinspection DataFlowIssue - sometimes null may be produced based on default value being null, ignoring here not to have to deal with nullability check everywhere this is used
		return defaultReturnValue;
	}

	public static int insertIntoInventory(ItemStack contents, IItemHandler inventory) {
		return tryToAddToInventory(contents, inventory, contents.getCount());
	}

	public static int tryToAddToInventoryAtPos(ItemStack contents, Level level, BlockPos pos, Direction side, int maxToAdd) {
		IItemHandler inventory = getInventoryAtPos(level, pos, side);
		if (inventory == null) {
			return 0;
		}

		return tryToAddToInventory(contents, inventory, maxToAdd);
	}

	public static int tryToAddToInventory(ItemStack contents, IItemHandler inventory, int maxToAdd) {
		int inventorySize = inventory.getSlots();

		int remaining = maxToAdd;
		ItemStack stackToInsert = contents.copy();
		int currentStackCount = Math.min(remaining, stackToInsert.getMaxStackSize());
		stackToInsert.setCount(currentStackCount);
		for (int slot = 0; slot < inventorySize; slot++) {
			//storage drawers and similar storage blocks support
			while (inventory.insertItem(slot, stackToInsert, true).getCount() < stackToInsert.getCount()) {
				ItemStack remainingStack = inventory.insertItem(slot, stackToInsert, false);
				if (remainingStack.getCount() < currentStackCount) {
					remaining -= (currentStackCount - remainingStack.getCount());
					if (remaining <= 0) {
						return maxToAdd;
					}
					stackToInsert = contents.copy();
					currentStackCount = Math.min(remaining, stackToInsert.getMaxStackSize());
					stackToInsert.setCount(currentStackCount);
				}
			}
		}

		return maxToAdd - remaining;
	}

	public static void tryRemovingLastStack(IItemHandler inventory, Level world, BlockPos pos) {
		for (int i = inventory.getSlots() - 1; i >= 0; i--) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
				ItemStack stack = inventory.getStackInSlot(i).copy();
				inventory.extractItem(i, stack.getCount(), false);
				if (world.isClientSide) {
					return;
				}
				ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, stack);
				world.addFreshEntity(itemEntity);
				break;
			}
		}
	}

	public static boolean tryAddingPlayerCurrentItem(Player player, IItemHandler inventory, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand).copy();
		stack.setCount(1);

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack remainingStack = inventory.insertItem(slot, stack, false);
			if (remainingStack.isEmpty()) {
				player.getItemInHand(hand).shrink(1);

				if (player.getItemInHand(hand).getCount() == 0) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}

				player.getInventory().setChanged();
				return true;
			}
		}

		return false;
	}

	public static boolean playerHasItem(Player player, Item item) {
		return playerHasItem(player, item, false, ICuriosItem.Type.NONE);
	}

	public static boolean playerHasItem(Player player, Item item, boolean checkEnabled, ICuriosItem.Type baubleType) {
		for (ItemStack stack : player.getInventory().items) {
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.getItem() == item && (!(checkEnabled && stack.getItem() instanceof ToggleableItem) || ((ToggleableItem) stack.getItem()).isEnabled(stack))) {
				return true;
			}
		}

		return baubleType != ICuriosItem.Type.NONE && hasItemInBaubleInventories(player, item, checkEnabled, baubleType);
	}

	private static boolean hasItemInBaubleInventories(Player player, Item item, boolean checkEnabled, ICuriosItem.Type baubleType) {
		for (BiFunction<Player, ICuriosItem.Type, IItemHandler> factory : baublesItemHandlerFactories) {
			IItemHandler handler = factory.apply(player, baubleType);
			for (int i = 0; i < handler.getSlots(); i++) {
				ItemStack baubleStack = handler.getStackInSlot(i);
				if (!baubleStack.isEmpty() && baubleStack.getItem() == item &&
						(!(checkEnabled && baubleStack.getItem() instanceof ToggleableItem) || ((ToggleableItem) baubleStack.getItem()).isEnabled(baubleStack))) {
					return true;
				}
			}
		}
		return false;
	}

	public static ItemStack getCorrectItemFromEitherHand(Player player, Item item) {
		return getHandHoldingCorrectItem(player, item).map(player::getItemInHand).orElse(ItemStack.EMPTY);
	}

	private static Optional<InteractionHand> getHandHoldingCorrectItem(Player player, Item item) {
		if (player.getMainHandItem().getItem() == item) {
			return Optional.of(InteractionHand.MAIN_HAND);
		}

		if (player.getOffhandItem().getItem() == item) {
			return Optional.of(InteractionHand.OFF_HAND);
		}
		return Optional.empty();
	}

	public static void addItemToPlayerInventory(Player player, ItemStack stack) {
		for (int i = 0; i < player.getInventory().items.size(); ++i) {
			if (player.getInventory().getItem(i).isEmpty()) {
				player.getInventory().setItem(i, stack);
				return;
			}
		}
		player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack));
	}

	public static NonNullList<ItemStack> getItemStacks(IItemHandler inventory) {
		NonNullList<ItemStack> ret = NonNullList.create();

		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ret.add(inventory.getStackInSlot(slot));
		}
		return ret;
	}

	public static void dropInventoryItems(Level world, BlockPos pos, IItemHandler inventory) {
		dropInventoryItems(world, pos.getX(), pos.getY(), pos.getZ(), inventory);
	}

	private static void dropInventoryItems(Level world, double x, double y, double z, IItemHandler inventory) {
		for (int i = 0; i < inventory.getSlots(); ++i) {
			ItemStack itemstack = inventory.getStackInSlot(i);

			if (!itemstack.isEmpty()) {
				Containers.dropItemStack(world, x, y, z, itemstack);
			}
		}
	}

	public static boolean hasItemHandler(Level world, BlockPos pos) {
		return executeOnItemHandlerAt(world, pos, world.getBlockState(pos), null, handler -> true, false);
	}

	public static <T extends IItemHandler> void runOnItemHandler(ItemStack stack, Consumer<T> run, Class<T> itemHandlerClass) {
		T itemHandler = getItemHandler(stack, itemHandlerClass);
		if (itemHandler != null) {
			run.accept(itemHandler);
		}
	}

	@Nullable
	private static <T extends IItemHandler> T getItemHandler(ItemStack stack, Class<T> itemHandlerClass) {
		return itemHandlerClass.cast(stack.getCapability(Capabilities.ItemHandler.ITEM));
	}

	public static <R, T extends IItemHandler> Optional<R> getFromHandler(ItemStack stack, Function<T, R> get, Class<T> itemHandlerClass) {
		T itemHandler = getItemHandler(stack, itemHandlerClass);
		if (itemHandler != null) {
			return Optional.of(get.apply(itemHandler));
		}
		return Optional.empty();
	}
}
