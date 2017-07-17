package xreliquary.init;

import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xreliquary.potions.PotionFlight;
import xreliquary.potions.PotionPacification;

public class ModPotions {
	public static Potion potionFlight = new PotionFlight();
	public static Potion potionPacification = new PotionPacification();

	public static void init() {
		GameRegistry.register(potionFlight);
		GameRegistry.register(potionPacification);
	}
}
