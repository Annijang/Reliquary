package reliquary.items.util.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import reliquary.init.ModItems;
import reliquary.reference.Config;

public class FluidHandlerInfernalChalice extends FluidHandlerItemStack {
	private static final String FLUID_STACKS_TAG = "fluidStacks";

	public FluidHandlerInfernalChalice(ItemStack chalice) {
		super(chalice, Config.COMMON.items.infernalChalice.fluidLimit.get());
	}

	@Override
	public boolean canDrainFluidType(FluidStack fluid) {
		return !ModItems.INFERNAL_CHALICE.get().isEnabled(container) && fluid.getFluid().is(FluidTags.LAVA) && getFluid().getAmount() >= fluid.getAmount();
	}

	@Override
	protected void setContainerToEmpty() {
		setFluid(new FluidStack(Fluids.LAVA, 0));
	}

	@Override
	public boolean canFillFluidType(FluidStack fluid) {
		return ModItems.INFERNAL_CHALICE.get().isEnabled(container) && fluid.getFluid() == Fluids.LAVA;
	}

	@Override
	protected void setFluid(FluidStack fluid) {
		if (!container.hasTag()) {
			container.setTag(new CompoundTag());
		}

		//noinspection ConstantConditions
		container.getTag().putInt(FLUID_STACKS_TAG, fluid.getAmount());
	}

	@Override
	public FluidStack getFluid() {
		CompoundTag tagCompound = container.getTag();
		if (tagCompound == null || !tagCompound.contains(FLUID_STACKS_TAG)) {
			return new FluidStack(Fluids.LAVA, 0);
		}
		return new FluidStack(Fluids.LAVA, tagCompound.getInt(FLUID_STACKS_TAG));
	}
}
