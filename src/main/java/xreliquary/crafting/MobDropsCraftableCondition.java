package xreliquary.crafting;

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import xreliquary.reference.Settings;

import java.util.function.BooleanSupplier;

public class MobDropsCraftableCondition implements IConditionFactory{
	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		return () -> Settings.dropCraftingRecipesEnabled;
	}
}
