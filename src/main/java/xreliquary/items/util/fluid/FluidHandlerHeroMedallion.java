package xreliquary.items.util.fluid;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import xreliquary.init.ModFluids;
import xreliquary.init.ModItems;
import xreliquary.util.XpHelper;

import javax.annotation.Nonnull;

public class FluidHandlerHeroMedallion implements IFluidHandlerItem, ICapabilityProvider {

	private ItemStack heroMedallion;

	public FluidHandlerHeroMedallion(ItemStack heroMedallion) {

		this.heroMedallion = heroMedallion;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability,  EnumFacing facing) {
		//noinspection unchecked
		return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY ? (T) this : null;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {new FluidTankProperties(new FluidStack(ModFluids.xpJuice(), Fluid.BUCKET_VOLUME), Integer.MAX_VALUE)};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource.getFluid() != ModFluids.xpJuice())
			return 0;

		if(doFill) {
			ModItems.heroMedallion.setExperience(heroMedallion, ModItems.heroMedallion.getExperience(heroMedallion) + XpHelper.liquidToExperience(resource.amount));
		}

		return resource.amount;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (resource.getFluid() != ModFluids.xpJuice())
			return null;

		int experienceToRemove = Math.min(XpHelper.liquidToExperience(resource.amount), ModItems.heroMedallion.getExperience(heroMedallion));

		if(doDrain) {
			ModItems.heroMedallion.setExperience(heroMedallion, ModItems.heroMedallion.getExperience(heroMedallion) - experienceToRemove);
		}

		return new FluidStack(ModFluids.xpJuice(), XpHelper.experienceToLiquid(experienceToRemove));
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return drain(new FluidStack(ModFluids.xpJuice(), maxDrain), doDrain);
	}

	@Nonnull
	@Override
	public ItemStack getContainer() {
		return heroMedallion;
	}
}
