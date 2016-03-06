package xreliquary.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface IPedestal {
	World getWorld();
	BlockPos getPos();
	int addToConnectedInventory(ItemStack stack);
	int addToConnectedTank(FluidStack fluidStack);
	void setActionCoolDown(int coolDownTicks);
}
