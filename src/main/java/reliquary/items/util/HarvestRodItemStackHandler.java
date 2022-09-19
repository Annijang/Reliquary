package reliquary.items.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraftforge.common.IPlantable;
import reliquary.reference.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HarvestRodItemStackHandler extends FilteredItemStackHandler {
	public static final int BONEMEAL_SLOT = 0;

	private static List<FilteredItemStack> getDefaultStacks() {
		List<FilteredItemStack> stacks = new ArrayList<>();
		stacks.add(new FilteredItemStack(Items.BONE_MEAL, Settings.COMMON.items.harvestRod.boneMealLimit.get(),
				Settings.COMMON.items.harvestRod.boneMealWorth.get(), false));
		return stacks;
	}

	public HarvestRodItemStackHandler() {
		super(getDefaultStacks());
		setDynamicSlotNumber();
	}

	@Override
	protected int getDynamicStackLimit() {
		return Settings.COMMON.items.harvestRod.maxCapacityPerPlantable.get();
	}

	@Override
	protected boolean isValidForStackSlot(ItemStack stack, int stackSlot) {
		if (stackSlot == BONEMEAL_SLOT) {
			return stack.getItem() == Items.BONE_MEAL;
		}
		return super.isValidForStackSlot(stack, stackSlot);
	}

	@Override
	protected boolean isValidForDynamicStack(ItemStack stack) {
		return isPlantable(stack);
	}

	private boolean isPlantable(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof IPlantable || (item instanceof ItemNameBlockItem itemNameBlockItem && itemNameBlockItem.getBlock() instanceof BonemealableBlock);
	}

	public int getBoneMealCount() {
		return getTotalAmount(BONEMEAL_SLOT);
	}

	public void setBoneMealCount(int boneMealCount) {
		setTotalCount(BONEMEAL_SLOT, boneMealCount);
	}

	public Optional<PlantableSlotInserted> insertPlantable(ItemStack stack) {
		for (int slot = 1; slot < getSlots(); slot++) {
			ItemStack result = insertItem(slot, stack, false);
			if (result.getCount() < stack.getCount()) {
				return Optional.of(new PlantableSlotInserted(slot, stack.getCount() - result.getCount()));
			}
		}
		return Optional.empty();
	}

	public record PlantableSlotInserted(int slot, int countInserted) {
		public int getSlot() {
			return slot;
		}

		public int getCountInserted() {
			return countInserted;
		}
	}

	public int getCountPlantable() {
		return getStackSlots() - 1;
	}
}
