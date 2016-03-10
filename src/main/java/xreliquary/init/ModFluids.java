package xreliquary.init;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xreliquary.reference.Compatibility;
import xreliquary.reference.Reference;
import xreliquary.util.LogHelper;

public class ModFluids {
	private static final String XP_JUICE_FLUID_NAME = "xpjuice";
	private static final String MILK_FLUID_NAME = "milk";

	public static Fluid fluidXpJuice;
	public static Fluid milk;

	public static void init() {
		FluidContainerRegistry.registerFluidContainer(FluidRegistry.WATER, new ItemStack(ModItems.emperorChalice), new ItemStack(ModItems.emperorChalice));

		if(!Loader.isModLoaded(Compatibility.MOD_ID.OPEN_BLOCKS) && !Loader.isModLoaded(Compatibility.MOD_ID.ENDERIO)) {
			LogHelper.info("XP Juice registered by Reliquary.");
			fluidXpJuice = new Fluid(XP_JUICE_FLUID_NAME, new ResourceLocation(Reference.MOD_ID, "fluids/xpjuice_still"), new ResourceLocation(Reference.MOD_ID, "fluids/xpjuice_flowing")).setLuminosity(10).setDensity(800).setViscosity(1500).setUnlocalizedName("xreliquary.xpjuice");
			FluidRegistry.registerFluid(fluidXpJuice);

			//TODO figure out if there needs to a bucket added here as well
		}
		else {
			LogHelper.info("XP Juice registration left to Open Blocks / Ender IO.");
		}

		if (!Loader.isModLoaded(Compatibility.MOD_ID.TINKERS_CONSTRUCT) && !FluidRegistry.isFluidRegistered(MILK_FLUID_NAME)) {
			milk = new Fluid(MILK_FLUID_NAME, new ResourceLocation(Reference.MOD_ID, "fluids/milk_still"), new ResourceLocation(Reference.MOD_ID, "fluids/milk_flowing")).setTemperature(320);
			FluidRegistry.registerFluid(milk);

			FluidContainerRegistry.registerFluidContainer(new FluidStack(milk, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Items.milk_bucket), FluidContainerRegistry.EMPTY_BUCKET);
		}

	}

	public static void postInit() {
		if(fluidXpJuice == null) { //should have been registered by open blocks / Ender IO
			fluidXpJuice = FluidRegistry.getFluid(XP_JUICE_FLUID_NAME);
			if(fluidXpJuice == null) {
				LogHelper.error("Liquid XP Juice registration left to open blocks / Ender IO but could not be found.");
			}
		}

		if (milk == null) {
			milk = FluidRegistry.getFluid(MILK_FLUID_NAME);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onIconLoad(TextureStitchEvent.Pre event) {
		if(fluidXpJuice != null) {
			event.map.registerSprite(fluidXpJuice.getStill());
			event.map.registerSprite(fluidXpJuice.getFlowing());
		}
	}
}
