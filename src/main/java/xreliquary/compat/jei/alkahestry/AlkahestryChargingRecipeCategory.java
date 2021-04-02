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
import xreliquary.crafting.AlkahestryChargingRecipe;
import xreliquary.init.ModItems;
import xreliquary.items.AlkahestryTomeItem;
import xreliquary.reference.Reference;
import xreliquary.util.LanguageHelper;

import java.util.List;

public class AlkahestryChargingRecipeCategory extends AlkahestryRecipeCategory<AlkahestryChargingRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(Reference.MOD_ID, "alkahestry_charging");
	private static final int INPUT_SLOT = 0;
	private static final int TOME_SLOT = 1;
	private static final int OUTPUT_SLOT = 2;

	private final IDrawable background;
	private final String localizedName;

	public AlkahestryChargingRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, UID);
		background = guiHelper.createDrawable(new ResourceLocation(Reference.DOMAIN + "textures/gui/jei/alkahest_charging.png"), 0, 0, 95, 36);
		localizedName = LanguageHelper.getLocalization("jei." + Reference.MOD_ID + ".recipe.alkahest_charging");
	}

	@Override
	public Class<? extends AlkahestryChargingRecipe> getRecipeClass() {
		return AlkahestryChargingRecipe.class;
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
	public void setIngredients(AlkahestryChargingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputIngredients(recipe.getIngredients());
		NonNullList<ItemStack> outputs = NonNullList.create();
		outputs.add(recipe.getRecipeOutput());
		outputs.add(AlkahestryTomeItem.setCharge(new ItemStack(ModItems.ALKAHESTRY_TOME.get()), recipe.getChargeToAdd()));
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AlkahestryChargingRecipe recipe, IIngredients ingredients) {
		recipeLayout.getItemStacks().init(INPUT_SLOT, true, 0, 0);
		recipeLayout.getItemStacks().init(TOME_SLOT, true, 18, 0);
		recipeLayout.getItemStacks().init(OUTPUT_SLOT, false, 73, 9);

		List<List<ItemStack>> ingredientsInputs = ingredients.getInputs(VanillaTypes.ITEM);
		ItemStack input = ingredientsInputs.get(0).get(0);
		ItemStack tome = ingredientsInputs.get(1).get(0);
		ItemStack output = ingredients.getOutputs(VanillaTypes.ITEM).get(0).get(0);

		recipeLayout.getItemStacks().set(INPUT_SLOT, input);
		recipeLayout.getItemStacks().set(TOME_SLOT, tome);
		recipeLayout.getItemStacks().set(OUTPUT_SLOT, output);
	}

	@Override
	public void draw(AlkahestryChargingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		String chargeString = "+" + recipe.getChargeToAdd();
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		int stringWidth = fontRenderer.getStringWidth(chargeString);
		fontRenderer.drawString(matrixStack, chargeString, (float) (((double) background.getWidth() - stringWidth) / 2), 3.0F, -8355712);
	}
}
