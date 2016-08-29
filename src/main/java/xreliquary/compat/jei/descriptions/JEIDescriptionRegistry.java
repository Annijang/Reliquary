package xreliquary.compat.jei.descriptions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import xreliquary.reference.Names;
import xreliquary.reference.Reference;

import java.util.*;
import java.util.stream.Collectors;

public class JEIDescriptionRegistry {
	private static Map<String, List<ItemStack>> registry = new HashMap<>();

	public static Set<DescriptionEntry> entrySet() {
		return registry.entrySet().stream().map(entry -> new DescriptionEntry(entry.getValue(), entry.getKey())).collect(Collectors.toCollection(HashSet::new));
	}

	public static void register(List<ItemStack> itemStacks, String name) {
		registry.put(Reference.MOD_ID + "." + Names.jei_description_prefix + name, itemStacks);
	}

	public static void register(Item item, String name) {
		if(item.getCreativeTab() != null) {
			if(item.getHasSubtypes()) {
				ArrayList<ItemStack> subItems = new ArrayList<>();
				item.getSubItems(item, item.getCreativeTab(), subItems);

				for(ItemStack stack : subItems) {
					registry.put(Reference.MOD_ID + "." + Names.jei_description_prefix + name + stack.getMetadata(), Collections.singletonList(stack));
				}
			} else {
				registry.put(Reference.MOD_ID + "." + Names.jei_description_prefix + name, Collections.singletonList(new ItemStack(item, 1)));
			}
		}
	}
}
