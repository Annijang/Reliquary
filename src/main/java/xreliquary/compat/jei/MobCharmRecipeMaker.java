package xreliquary.compat.jei;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import xreliquary.init.ModItems;
import xreliquary.items.MobCharmRegistry;
import xreliquary.reference.Reference;

import java.util.ArrayList;
import java.util.List;

public class MobCharmRecipeMaker {
	private MobCharmRecipeMaker() {}

	public static List<ShapedRecipe> getRecipes() {
		List<ShapedRecipe> recipes = new ArrayList<>();

		for (String regName : MobCharmRegistry.getRegisteredNames()) {
			Ingredient fragmentIngredient = Ingredient.fromStacks(ModItems.MOB_CHARM_FRAGMENT.get().getStackFor(regName));
			Ingredient leatherIngredient = Ingredient.fromItems(Items.LEATHER);
			Ingredient stringIngredient = Ingredient.fromItems(Items.STRING);

			NonNullList<Ingredient> inputs = NonNullList.create();
			inputs.add(fragmentIngredient);
			inputs.add(leatherIngredient);
			inputs.add(fragmentIngredient);
			inputs.add(fragmentIngredient);
			inputs.add(stringIngredient);
			inputs.add(fragmentIngredient);
			inputs.add(fragmentIngredient);
			inputs.add(Ingredient.EMPTY);
			inputs.add(fragmentIngredient);

			ItemStack output = ModItems.MOB_CHARM.get().getStackFor(regName);

			ResourceLocation id = new ResourceLocation(Reference.MOD_ID, "mob_charm_" + regName.replace(':', '_'));
			recipes.add(new ShapedRecipe(id, "xreliquary.mob_charm", 3, 3, inputs, output));
		}

		return recipes;
	}
}
