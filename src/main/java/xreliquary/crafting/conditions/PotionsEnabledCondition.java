package xreliquary.crafting.conditions;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import xreliquary.reference.Reference;
import xreliquary.reference.Settings;

public class PotionsEnabledCondition implements ICondition {
	private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "potions_enabled");
	public static final SimpleConditionSerializer<PotionsEnabledCondition> SERIALIZER = new SimpleConditionSerializer<>(ID, PotionsEnabledCondition::new);

	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public boolean test() {
		return !Settings.COMMON.disable.disablePotions.get();
	}
}
