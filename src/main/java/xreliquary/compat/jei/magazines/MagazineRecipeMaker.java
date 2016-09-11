package xreliquary.compat.jei.magazines;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import xreliquary.init.ModItems;
import xreliquary.reference.Settings;
import xreliquary.util.potions.PotionEssence;
import xreliquary.util.potions.XRPotionHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MagazineRecipeMaker {
	@Nonnull
	public static List<MagazineRecipeJEI> getRecipes() {
		ArrayList<MagazineRecipeJEI> recipes = new ArrayList<>();

		//add basic set of magazines
		for(int meta = 1; meta <= 9; meta++) {
			List<ItemStack> inputs = new ArrayList<>();
			inputs.add(new ItemStack(ModItems.magazine));
			inputs.addAll(addShots(inputs, meta, null));

			recipes.add(new MagazineRecipeJEI(inputs, new ItemStack(ModItems.magazine, 1, meta)));
		}

		//now add potion variants for the neutral one
		for(PotionEssence essence : Settings.Potions.uniquePotions) {
			List<PotionEffect> effects = XRPotionHelper.changeDuration(essence.getEffects(), 0.2F);

			List<ItemStack> inputs = new ArrayList<>();
			inputs.add(new ItemStack(ModItems.magazine, 1, 1));
			inputs.addAll(addShots(inputs, 1, effects));

			ItemStack output = new ItemStack(ModItems.magazine, 1, 1);
			PotionUtils.appendEffects(output, effects);

			recipes.add(new MagazineRecipeJEI(inputs, output));
		}

		return recipes;
	}

	private static List<ItemStack> addShots(List<ItemStack> inputs, int meta, List<PotionEffect> effects) {
		ItemStack shot = new ItemStack(ModItems.bullet, 1, meta);

		if(effects != null && !effects.isEmpty()) {
			PotionUtils.appendEffects(shot, effects);
		}

		for(int i = 0; i < 8; i++) {
			inputs.add(shot);
		}

		return inputs;
	}
}
