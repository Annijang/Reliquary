package reliquary.crafting.conditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import reliquary.reference.Reference;
import reliquary.reference.Settings;

public class SpawnEggEnabledCondition implements ICondition {
	private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "spawn_egg_enabled");
	public static final SimpleConditionSerializer<SpawnEggEnabledCondition> SERIALIZER = new SimpleConditionSerializer<>(ID, SpawnEggEnabledCondition::new);

	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public boolean test(IContext context) {
		return !Settings.COMMON.disable.disableSpawnEggRecipes.get();
	}
}
