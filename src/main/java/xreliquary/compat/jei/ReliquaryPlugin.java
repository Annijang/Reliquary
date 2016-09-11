package xreliquary.compat.jei;

import mezz.jei.api.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import xreliquary.compat.jei.alkahestry.*;
import xreliquary.compat.jei.cauldron.CauldronRecipeCategory;
import xreliquary.compat.jei.cauldron.CauldronRecipeHandler;
import xreliquary.compat.jei.cauldron.CauldronRecipeMaker;
import xreliquary.compat.jei.descriptions.DescriptionEntry;
import xreliquary.compat.jei.descriptions.JEIDescriptionRegistry;
import xreliquary.compat.jei.magazines.MagazineRecipeHandler;
import xreliquary.compat.jei.magazines.MagazineRecipeMaker;
import xreliquary.compat.jei.mortar.MortarRecipeCategory;
import xreliquary.compat.jei.mortar.MortarRecipeHandler;
import xreliquary.compat.jei.mortar.MortarRecipeMaker;
import xreliquary.compat.jei.lingering.ArrowShotRecipeHandler;
import xreliquary.compat.jei.lingering.ArrowShotRecipeMaker;
import xreliquary.init.ModBlocks;
import xreliquary.init.ModItems;
import xreliquary.reference.Reference;
import xreliquary.reference.Settings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JEIPlugin
public class ReliquaryPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

		boolean tomeEnabled = ModItems.alkahestryTome.getRegistryName() != null && !Settings.disabledItemsBlocks.contains(ModItems.alkahestryTome.getRegistryName().getResourcePath());

		if (tomeEnabled) {
			registry.addRecipeCategories(new AlkahestryCraftingRecipeCategory(guiHelper));
			registry.addRecipeCategories(new AlkahestryChargingRecipeCategory(guiHelper));

			registry.addRecipeHandlers(new AlkahestryCraftingRecipeHandler());
			registry.addRecipeHandlers(new AlkahestryChargingRecipeHandler());

			registry.addRecipes(AlkahestryCraftingRecipeMaker.getRecipes());
			registry.addRecipes(AlkahestryChargingRecipeMaker.getRecipes());

			registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.CRAFTING_TABLE), Reference.JEI_CATEGORY_ALKAHESTRY_CHARGING);
			registry.addRecipeCategoryCraftingItem(new ItemStack(Blocks.CRAFTING_TABLE), Reference.JEI_CATEGORY_ALKAHESTRY_CRAFTING);
		}

		registry.addRecipeCategories(new MortarRecipeCategory(guiHelper));
		registry.addRecipeCategories(new CauldronRecipeCategory(guiHelper));

		registry.addRecipeHandlers(new MortarRecipeHandler());
		registry.addRecipeHandlers(new CauldronRecipeHandler());
		registry.addRecipeHandlers(new ArrowShotRecipeHandler());
		registry.addRecipeHandlers(new MagazineRecipeHandler());

		registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.apothecaryMortar), Reference.JEI_CATEGORY_MORTAR);
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.apothecaryCauldron), Reference.JEI_CATEGORY_CAULDRON);

		registry.addRecipes(MortarRecipeMaker.getRecipes());
		registry.addRecipes(CauldronRecipeMaker.getRecipes());
		registry.addRecipes(ArrowShotRecipeMaker.getRecipes(new ItemStack(ModItems.bullet, 1, 1)));
		registry.addRecipes(ArrowShotRecipeMaker.getRecipes(new ItemStack(ModItems.tippedArrow)));
		registry.addRecipes(MagazineRecipeMaker.getRecipes());

		for(DescriptionEntry entry : JEIDescriptionRegistry.entrySet())
			registry.addDescription(entry.itemStacks(), entry.langKey());

		//blacklisted items
		registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(ModItems.filledVoidTear));
		for (byte i=0; i<13; i++) {
			registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(ModItems.heartZhu, 1, i));
		}

		ISubtypeRegistry nbtRegistry = registry.getJeiHelpers().getSubtypeRegistry();

		nbtRegistry.useNbtForSubtypes(ModItems.mobCharm);
		nbtRegistry.useNbtForSubtypes(ModItems.potionEssence);
		nbtRegistry.useNbtForSubtypes(ModItems.bullet);
		nbtRegistry.useNbtForSubtypes(ModItems.tippedArrow);
		nbtRegistry.useNbtForSubtypes(ModItems.magazine);

		nbtRegistry.registerNbtInterpreter(ModItems.potion, new ISubtypeRegistry.ISubtypeInterpreter() {
			@Nullable
			@Override
			public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
				NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
				if(nbtTagCompound == null || nbtTagCompound.hasNoTags()) {
					return null;
				}
				return nbtTagCompound.toString();
			}
		});
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}
}