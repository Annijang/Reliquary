package xreliquary.handler.config;


import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import xreliquary.handler.ConfigurationHandler;
import xreliquary.init.XRRecipes;
import xreliquary.reference.Names;
import xreliquary.reference.Reference;
import xreliquary.reference.Settings;
import xreliquary.util.LogHelper;
import xreliquary.util.StackHelper;
import xreliquary.util.potions.EffectComparator;
import xreliquary.util.potions.PotionEssence;
import xreliquary.util.potions.PotionIngredient;
import xreliquary.util.potions.XRPotionHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class PotionConfiguration
{
	public static void loadPotionMap() {
		ConfigCategory category = ConfigurationHandler.configuration.getCategory(Names.potion_map);

		if (category.isEmpty()) {
			addDefaultPotionMap(category);
		}

		loadPotionMapIntoSettings(category);

		LogHelper.debug("Starting calculation of potion combinations");
		loadPotionCombinations();
		loadUniquePotions();
		LogHelper.debug("Done with potion combinations");

		ConfigurationHandler.setCategoryTranslations(Names.potion_map, true);
	}

	private static void loadUniquePotions() {
		Settings.Potions.uniquePotions.clear();

		for (PotionEssence essence : Settings.Potions.potionCombinations) {
			boolean found = false;
			for (PotionEssence uniqueEssence : Settings.Potions.uniquePotions) {
				if (effectsEqual(essence.effects, uniqueEssence.effects)) {
					found = true;
					break;
				}
			}
			if (!found)
				Settings.Potions.uniquePotions.add(essence);
		}

		Settings.Potions.uniquePotions.sort(new Comparator<PotionEssence>() {
			@Override
			public int compare(PotionEssence o1, PotionEssence o2) {

				int ret=0;

				for (int i=0;i<Math.min(o1.effects.size(), o2.effects.size());i++) {
					ret = new EffectComparator().compare(o1.effects.get(i), o2.effects.get(i));
					if (ret != 0)
						break;
				}

				if (ret == 0)
					ret = Integer.compare(o1.effects.size(), o2.effects.size());

				return ret;
			}
		});
	}

	private static void loadPotionCombinations() {
		Settings.Potions.potionCombinations.clear();

		//multiple effect potions and potions made of 3 ingredients are turned on by config option
		for(PotionIngredient ingredient1 : Settings.Potions.potionMap) {
			for(PotionIngredient ingredient2 : Settings.Potions.potionMap) {
				if (ingredient1.item.getItem() != ingredient2.item.getItem() || ingredient1.item.getMetadata() != ingredient2.item.getMetadata()) {
					PotionEssence twoEssence = new PotionEssence(new PotionIngredient[] {ingredient1, ingredient2});
					if (twoEssence.effects.size() > 0 && twoEssence.effects.size() <= Settings.Potions.maxEffectCount) {
						addPotionCombination(twoEssence);

						if (Settings.Potions.threeIngredients) {
							for(PotionIngredient ingredient3 : Settings.Potions.potionMap) {
								if ((ingredient3.item.getItem() != ingredient1.item.getItem() || ingredient3.item.getMetadata() != ingredient1.item.getMetadata())
										&& (ingredient3.item.getItem() != ingredient2.item.getItem() || ingredient3.item.getMetadata() != ingredient2.item.getMetadata())) {
									PotionEssence threeEssence = new PotionEssence(new PotionIngredient [] {ingredient1, ingredient2, ingredient3});

									if (!effectsEqual(twoEssence.effects, threeEssence.effects)) {
										addPotionCombination(threeEssence);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private static void addPotionCombination(PotionEssence newEssence) {
		for (PotionEssence essence: Settings.Potions.potionCombinations) {
			//exactly same ingredients in a different order are not to be added here
			if(ingredientsEqual(essence.ingredients, newEssence.ingredients)) {
				return;
			}
			//the same effect potion id with different duration is turned on by config option
			if(effectsEqual(essence.effects, newEssence.effects, Settings.Potions.differentDurations, true)
					&& !effectsEqual(essence.effects, newEssence.effects)) {
				return;
			}
		}

		Settings.Potions.potionCombinations.add(newEssence);
	}

	private static boolean ingredientsEqual(List<PotionIngredient> a, List<PotionIngredient> b) {
		if (a.size() != b.size())
			return false;
		for (PotionIngredient ingredientA:a) {
			boolean found = false;
			for(PotionIngredient ingredientB:b) {
				if(ingredientA.item.getItem() == ingredientB.item.getItem()
						&& ingredientA.item.getMetadata() == ingredientB.item.getMetadata()) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	private static boolean effectsEqual(List<PotionEffect> a, List<PotionEffect> b) {
		return effectsEqual(a, b, true, true);
	}

	private static boolean effectsEqual(List<PotionEffect> a, List<PotionEffect> b, boolean compareDuration, boolean comparePotency) {
		if(a.size() != b.size())
			return false;

		for (PotionEffect effectA:a) {
			boolean found = false;
			for(PotionEffect effectB:b) {
				if(effectA.getPotionID() == effectB.getPotionID()
						&& (!compareDuration || effectA.getDuration() == effectB.getDuration())
						&& (!comparePotency || effectA.getAmplifier() == effectB.getAmplifier())) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	private static void loadPotionMapIntoSettings(ConfigCategory category) {
		Settings.Potions.potionMap.clear();

		for(Map.Entry<String, Property> entry: category.getValues().entrySet()) {
			String[] nameParts = entry.getKey().split("\\|");
			String[] effects = entry.getValue().getStringList();

			String modId = nameParts[0].split(":")[0];
			String name = nameParts[0].split(":")[1];
			int meta = Integer.parseInt(nameParts[1]);

			ItemStack stack = StackHelper.getItemStackFromNameMeta(modId, name, meta);

			if (stack != null) {
				PotionIngredient ingredient = new PotionIngredient(stack);
				for (int i=0; i<effects.length; i++) {
					String[] effectValues = effects[i].split("\\|");
					int potionId = XRPotionHelper.getPotionIdByName(effectValues[0]);
					if (potionId > 0) {
						short durationWeight = Short.parseShort(effectValues[1]);
						short ampWeight = Short.parseShort(effectValues[2]);
						ingredient.addEffect(potionId, durationWeight, ampWeight);
					}
				}
				if (ingredient.effects.size() > 0) {
					Settings.Potions.potionMap.add(ingredient);
				}
			}
		}
	}

	private static void addDefaultPotionMap(ConfigCategory category) {
		//TIER ONE INGREDIENTS, these are always 0 potency and have minimal durations (3 for positive, 1 for negative or super-positive)
		addPotionConfig(category, Items.sugar, speed(3, 0), haste(3, 0));
		addPotionConfig(category, Items.apple,heal(0), hboost(3, 0));
		addPotionConfig(category, Items.coal, blind(1, 0), absorb(3, 0));
		addPotionConfig(category, Items.coal, 1, invis(1, 0), wither(0, 0));
		addPotionConfig(category, Items.feather, jump(3, 0), weak(1, 0));
		addPotionConfig(category, Items.wheat_seeds, harm(0), hboost(3, 0));
		addPotionConfig(category, Items.wheat,heal(0), hboost(3, 0));
		addPotionConfig(category, Items.flint, harm(0), dboost(3, 0));
		addPotionConfig(category, Items.porkchop,slow(1, 0), fatigue(1, 0));
		addPotionConfig(category, Items.leather,resist(3, 0), absorb(3, 0));
		addPotionConfig(category, Items.clay_ball,slow(1, 0), hboost(3, 0));
		addPotionConfig(category, Items.egg,absorb(3, 0), regen(0, 0));
		addPotionConfig(category, Items.dye, Reference.RED_DYE_META, heal(0), hboost(3, 0)); //rose red
		addPotionConfig(category, Items.dye, Reference.YELLOW_DYE_META,jump(3, 0), weak(1, 0)); //dandellion yellow
		addPotionConfig(category, Items.dye, Reference.GREEN_DYE_META,resist(3, 0), absorb(3, 0)); //cactus green
		addPotionConfig(category, Items.dye, Reference.WHITE_DYE_META, weak(1, 0), fatigue(1, 0)); //bone meal
		addPotionConfig(category, Items.pumpkin_seeds,invis(1, 0), fireres(1,0));
		addPotionConfig(category, Items.beef,slow(1,0), satur(0));
		addPotionConfig(category, Items.chicken,nausea(1, 0), poison(1, 0));
		addPotionConfig(category, Items.rotten_flesh,nausea(1, 0), hunger(1, 0), wither(0, 0));
		addPotionConfig(category, Items.gold_nugget, dboost(0, 0), haste(0, 0));
		addPotionConfig(category, Items.carrot,vision(3, 0), hboost(3, 0));
		addPotionConfig(category, Items.potato,hboost(3, 0), satur(0));
		addPotionConfig(category, Items.fish, satur(0), breath(1, 0));

		//TIER TWO INGREDIENTS, one of the effects of each will always be a one, slightly increased duration vs. TIER ONE
		addPotionConfig(category, Items.spider_eye, vision(4, 0), poison(2, 0));
		addPotionConfig(category, Items.blaze_powder, dboost(4, 0), harm(0));
		addPotionConfig(category, Items.iron_ingot, resist(4, 0), slow(2, 0));
		addPotionConfig(category, Items.string, slow(2, 0), fatigue(2, 0));
		addPotionConfig(category, Items.bread, hboost(4, 0), satur(0));
		addPotionConfig(category, Items.cooked_porkchop, fatigue(2, 0), satur(0));
		addPotionConfig(category, Items.slime_ball, resist(4, 0), fireres(2, 0));
		addPotionConfig(category, Items.cooked_fish, satur(0), breath(2, 0));
		addPotionConfig(category, Items.dye, Reference.BLUE_DYE_META, haste(4, 0), dboost(4, 0));  //lapis lazuli
		addPotionConfig(category, Items.dye, Reference.BLACK_DYE_META, blind(2, 0), invis(2, 0)); //ink
		addPotionConfig(category, Items.bone, weak(2, 0), fatigue(2, 0));
		addPotionConfig(category, Items.cookie, heal(0), satur(0));
		addPotionConfig(category, Items.melon, heal(0), speed(4, 0));
		addPotionConfig(category, Items.cooked_beef, resist(4, 0), satur(0));
		addPotionConfig(category, Items.cooked_chicken, jump(4, 0), satur(0));
		addPotionConfig(category, Items.baked_potato, satur(0), regen(1, 0));
		addPotionConfig(category, Items.poisonous_potato, poison(2, 0), wither(1, 0));
		addPotionConfig(category, Items.quartz, harm(0), dboost(4, 0));
		addPotionConfig(category, XRRecipes.zombieHeart(), nausea(2, 0), hunger(2, 0), wither(1, 0));
		addPotionConfig(category, XRRecipes.squidBeak(), hunger(2, 0), breath(2, 0));

		//TIER THREE INGREDIENTS, these are closer to vanilla durations, carry many effects or a slightly increased duration. Some/most are combos.
		addPotionConfig(category, Items.pumpkin_pie, invis(1, 0), fireres(1, 0), speed(3, 0), haste(3, 0), absorb(3, 0), regen(0, 0)); //combination of ingredients, strong.
		addPotionConfig(category, Items.magma_cream, dboost(4, 0), harm(0), resist(4, 0), fireres(2, 0)); //also a combo, strong.
		addPotionConfig(category, Items.speckled_melon, dboost(3, 0), haste(3, 0), heal(0), speed(4, 0)); //combo
		addPotionConfig(category, Items.ghast_tear, regen(3, 0), absorb(5, 0));
		addPotionConfig(category, Items.fermented_spider_eye, vision(4, 0), poison(2, 0), speed(3, 0), haste(3, 0)); //combo
		addPotionConfig(category, Items.golden_carrot, dboost(3, 0), haste(3, 0), hboost(3, 0), vision(3, 0)); //combo
		addPotionConfig(category, Items.gold_ingot, dboost(4, 0), haste(4, 0)); //combo
		addPotionConfig(category, XRRecipes.ribBone(), weak(3, 0), fatigue(3, 0));
		addPotionConfig(category, Items.ender_pearl, invis(5, 0), speed(5, 0));
		addPotionConfig(category, Items.blaze_rod, dboost(8, 0), harm(0));
		addPotionConfig(category, Items.fire_charge, dboost(4, 0), harm(0), blind(1, 0), absorb(3, 0)); //combo
		addPotionConfig(category, XRRecipes.creeperGland(), regen(3, 0), hboost(5, 0));
		addPotionConfig(category, XRRecipes.spiderFangs(), poison(3, 0), weak(3, 0));
		addPotionConfig(category, XRRecipes.slimePearl(), resist(5, 0), absorb(5, 0));
		addPotionConfig(category, XRRecipes.shellFragment(), absorb(5, 0), breath(5, 0));
		addPotionConfig(category, XRRecipes.batWing(), jump(5, 0), weak(3, 0));

		//TIER FOUR INGREDIENTS, these carry multiple one-potency effects and have the most duration for any given effect.
		addPotionConfig(category, Items.diamond, resist(6, 1), absorb(6, 1), fireres(6, 0));
		addPotionConfig(category, XRRecipes.witherRib(), wither(2, 1), weak(3, 1), slow(3, 1), fatigue(3, 1));
		addPotionConfig(category, Items.ender_eye, dboost(6, 1), invis(6, 0), speed(6, 1), harm(1));
		addPotionConfig(category, Items.emerald, haste(6, 1), speed(6, 1), hboost(6, 1));
		addPotionConfig(category, Items.nether_star, hboost(24, 1), regen(24, 1), absorb(24, 1)); //nether star is holy stonk
		addPotionConfig(category, XRRecipes.moltenCore(), dboost(6, 1), fireres(6, 0), harm(1));
		addPotionConfig(category, XRRecipes.stormEye(), haste(24, 1), speed(24, 1), jump(24, 1), harm(1));
		addPotionConfig(category, XRRecipes.fertileEssence(), hboost(8, 1), regen(3, 1), heal(1), satur(1), weak(9, 1), fatigue(9, 1));
		addPotionConfig(category, XRRecipes.frozenCore(), absorb(6, 1), slow(3, 1), fatigue(3, 1), harm(1), fireres(6, 0));
		addPotionConfig(category, XRRecipes.enderHeart(), vision(6, 0), invis(6, 0), harm(1), hboost(6, 1), dboost(6, 1), speed(6, 1), haste(6, 1));
		addPotionConfig(category, XRRecipes.infernalClaw(), harm(1), resist(6, 1), fireres(6, 0), dboost(6, 1), satur(1), heal(1));
	}

	public static String harm(int potency) { return effectString(Reference.HARM, Integer.toString(0),Integer.toString(potency)); }

	public static String heal(int potency) { return effectString(Reference.HEAL, Integer.toString(0),Integer.toString(potency)); }

	public static String satur(int potency) { return effectString(Reference.SATURATION, Integer.toString(0),Integer.toString(potency)); }

	public static String invis(int duration, int potency) { return effectString(Reference.INVIS, Integer.toString(duration), Integer.toString(potency)); }

	public static String absorb(int duration, int potency) { return effectString(Reference.ABSORB, Integer.toString(duration),Integer.toString(potency)); }

	public static String hboost(int duration, int potency) { return effectString(Reference.HBOOST, Integer.toString(duration),Integer.toString(potency)); }

	public static String dboost(int duration, int potency) { return effectString(Reference.DBOOST, Integer.toString(duration),Integer.toString(potency)); }

	public static String speed(int duration, int potency) { return effectString(Reference.SPEED, Integer.toString(duration),Integer.toString(potency)); }

	public static String haste(int duration, int potency) { return effectString(Reference.HASTE, Integer.toString(duration),Integer.toString(potency)); }

	public static String slow(int duration, int potency) { return effectString(Reference.SLOW, Integer.toString(duration),Integer.toString(potency)); }

	public static String fatigue(int duration, int potency) { return effectString(Reference.FATIGUE, Integer.toString(duration),Integer.toString(potency)); }

	public static String breath(int duration, int potency) { return effectString(Reference.BREATH, Integer.toString(duration),Integer.toString(potency)); }

	public static String vision(int duration, int potency) { return effectString(Reference.VISION, Integer.toString(duration),Integer.toString(potency)); }

	public static String resist(int duration, int potency) { return effectString(Reference.RESIST, Integer.toString(duration),Integer.toString(potency)); }

	public static String fireres(int duration, int potency) { return effectString(Reference.FRESIST, Integer.toString(duration),Integer.toString(potency)); }

	public static String weak(int duration, int potency) { return effectString(Reference.WEAK, Integer.toString(duration),Integer.toString(potency)); }

	public static String jump(int duration, int potency) { return effectString(Reference.JUMP, Integer.toString(duration),Integer.toString(potency)); }

	public static String nausea(int duration, int potency) { return effectString(Reference.NAUSEA, Integer.toString(duration),Integer.toString(potency)); }

	public static String hunger(int duration, int potency) { return effectString(Reference.HUNGER, Integer.toString(duration),Integer.toString(potency)); }

	public static String regen(int duration, int potency) { return effectString(Reference.REGEN, Integer.toString(duration),Integer.toString(potency)); }

	public static String poison(int duration, int potency) { return effectString(Reference.POISON, Integer.toString(duration),Integer.toString(potency)); }

	public static String wither(int duration, int potency) { return effectString(Reference.WITHER, Integer.toString(duration),Integer.toString(potency)); }

	public static String blind(int duration, int potency) { return effectString(Reference.BLIND, Integer.toString(duration), Integer.toString(potency)); }

	public static String effectString(String name, String duration, String potency) {
		return name + "|" + duration + "|" + potency;
	}

	private static void addPotionConfig(ConfigCategory category, ItemStack ingredient, String... effects ) {
		addPotionConfig(category, ingredient.getItem(), ingredient.getMetadata(), effects);
	}

	private static void addPotionConfig(ConfigCategory category, Item ingredient, String... effects ) {
		addPotionConfig(category, ingredient, 0, effects);
	}

	private static void addPotionConfig(ConfigCategory category, Item ingredient, int meta, String... effects ) {
		Property prop = new Property(String.format("%s|%d", ingredient.getRegistryName(), meta),effects, Property.Type.STRING);

		category.put(prop.getName(), prop);
	}
}
