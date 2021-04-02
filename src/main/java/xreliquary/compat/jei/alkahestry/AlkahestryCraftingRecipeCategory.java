package xreliquary.compat.jei.alkahestry;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import xreliquary.crafting.AlkahestryCraftingRecipe;
import xreliquary.init.ModItems;
import xreliquary.items.AlkahestryTomeItem;
import xreliquary.reference.Reference;
import xreliquary.reference.Settings;
import xreliquary.util.LanguageHelper;

import java.util.List;

public class AlkahestryCraftingRecipeCategory extends AlkahestryRecipeCategory<AlkahestryCraftingRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(Reference.MOD_ID, "alkahestry_crafting");
	private static final int INPUT_SLOT = 0;
	private static final int TOME_SLOT = 1;
	private static final int OUTPUT_SLOT = 2;
	private static final int TOME_OUTPUT_SLOT = 3;

	private final IDrawable background;
	private final String localizedName;

	public AlkahestryCraftingRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, UID);
		background = guiHelper.createDrawable(new ResourceLocation(Reference.DOMAIN + "textures/gui/jei/alkahest_crafting.png"), 0, 0, 95, 76);
		localizedName = LanguageHelper.getLocalization("jei." + Reference.MOD_ID + ".recipe.alkahest_crafting");
	}

	@Override
	public Class<? extends AlkahestryCraftingRecipe> getRecipeClass() {
		return AlkahestryCraftingRecipe.class;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setIngredients(AlkahestryCraftingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		NonNullList<ItemStack> outputs = NonNullList.create();
		outputs.add(recipe.getRecipeOutput());
		outputs.add(AlkahestryTomeItem.setCharge(new ItemStack(ModItems.ALKAHESTRY_TOME.get()),
				Settings.COMMON.items.alkahestryTome.chargeLimit.get() - recipe.getChargeNeeded()));
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AlkahestryCraftingRecipe recipeWrapper, IIngredients ingredients) {
		recipeLayout.getItemStacks().init(INPUT_SLOT, true, 0, 0);
		recipeLayout.getItemStacks().init(TOME_SLOT, true, 18, 0);
		recipeLayout.getItemStacks().init(OUTPUT_SLOT, false, 73, 9);
		recipeLayout.getItemStacks().init(TOME_OUTPUT_SLOT, false, 18, 59);

		List<List<ItemStack>> ingredientsInputs = ingredients.getInputs(VanillaTypes.ITEM);
		ItemStack input = ingredientsInputs.get(0).get(0);
		ItemStack tome = ingredientsInputs.get(1).get(0);
		ItemStack output = ingredients.getOutputs(VanillaTypes.ITEM).get(0).get(0);
		ItemStack tomeOutput = ingredients.getOutputs(VanillaTypes.ITEM).get(1).get(0);

		recipeLayout.getItemStacks().set(INPUT_SLOT, input);
		recipeLayout.getItemStacks().set(TOME_SLOT, tome);
		recipeLayout.getItemStacks().set(OUTPUT_SLOT, output);
		recipeLayout.getItemStacks().set(TOME_OUTPUT_SLOT, tomeOutput);
	}

	@Override
	public void draw(AlkahestryCraftingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		String chargeString = "-" + recipe.getChargeNeeded();
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		int stringWidth = fontRenderer.getStringWidth(chargeString);
		fontRenderer.drawString(matrixStack, chargeString, (float) (((double) background.getWidth() - stringWidth) / 2), 40.0F, -8355712);
	}
}
