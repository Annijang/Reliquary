package xreliquary.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class StackHelper {
	private StackHelper() {}

	public static Optional<ItemStack> getItemStackFromName(String name) {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
		if (item == null) {
			return Optional.empty();
		}
		return Optional.of(new ItemStack(item));
	}
}
