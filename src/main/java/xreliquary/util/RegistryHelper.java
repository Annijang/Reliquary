package xreliquary.util;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class RegistryHelper {
	private RegistryHelper() {}
	public static String getItemRegistryName(Item item) {
		ResourceLocation rl = item.getRegistryName();
		//null check because some mods don't properly register items they use in recipes
		if(rl != null) {
			return rl.toString();
		}

		return "";
	}
}
