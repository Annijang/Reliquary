package xreliquary.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A language file 'preprocessor', I guess you could call it. It just injects globals right now.
 *
 * @author TheMike
 * @author x3n0ph0b3
 */
@OnlyIn(Dist.CLIENT)
public class LanguageHelper {
	private LanguageHelper() {}

	private static Map<String, String> preprocessed = new HashMap<>();
	private static Map<String, String> globals = new HashMap<>();

	static {
		globals.put("colors.black", "\u00A70");
		globals.put("colors.navy", "\u00A71");
		globals.put("colors.green", "\u00A72");
		globals.put("colors.blue", "\u00A73");
		globals.put("colors.red", "\u00A74");
		globals.put("colors.purple", "\u00A75");
		globals.put("colors.gold", "\u00A76");
		globals.put("colors.light_gray", "\u00A77");
		globals.put("colors.gray", "\u00A78");
		globals.put("colors.dark_purple", "\u00A79");
		globals.put("colors.light_green", "\u00A7a");
		globals.put("colors.light_blue", "\u00A7b");
		globals.put("colors.rose", "\u00A7c");
		globals.put("colors.light_purple", "\u00A7d");
		globals.put("colors.yellow", "\u00A7e");
		globals.put("colors.white", "\u00A7f");
		globals.put("colors.reset", TextFormatting.RESET.toString());
	}

	/**
	 * Gets the preprocessed version of the localized string. Preprocessing will only be ran once, not on every call.
	 *
	 * @param key The localization key.
	 * @return A preprocessed localized string. If your current language doesn't have a localized string, it defaults to en_US.
	 */
	public static String getLocalization(String key, Object... parameters) {
		String localization = I18n.format(key, parameters);

		if (preprocessed.containsKey(key)) {
			return preprocessed.get(key);
		} else if (localization.contains("{{!")) {
			while (localization.contains("{{!")) {
				int startingIndex = localization.indexOf("{{!");
				int endingIndex = localization.indexOf("}}", startingIndex);
				String fragment = localization.substring(startingIndex + 3, endingIndex);

				try {
					String replacement = globals.get(fragment.toLowerCase());
					localization = localization.substring(0, startingIndex) + replacement + localization.substring(endingIndex + 2);
				}
				catch (Exception e) {
					localization = localization.substring(0, startingIndex) + localization.substring(endingIndex + 2);
				}
			}

			preprocessed.put(key, localization);
		}
		return localization;
	}

	public static void formatTooltip(String langName, List<ITextComponent> list) {
		formatTooltip(langName, ImmutableMap.of(), list);
	}

	public static void formatTooltip(String langName, @Nullable ImmutableMap<String, String> toFormat, List<ITextComponent> list) {
		String langTooltip = getLocalization(langName);
		if (langTooltip.equals(langName)) {
			return;
		}
		if (toFormat != null) {
			for (Map.Entry<String, String> toReplace : toFormat.entrySet()) {
				langTooltip = langTooltip.replace("{{" + toReplace.getKey() + "}}", toReplace.getValue());
			}
		}

		for (String descriptionLine : langTooltip.split(";")) {
			if (descriptionLine != null && descriptionLine.length() > 0) {
				list.add(new StringTextComponent(descriptionLine));
			}
		}
	}

	public static boolean localizationExists(String langName) {
		return I18n.hasKey(langName);
	}

}
