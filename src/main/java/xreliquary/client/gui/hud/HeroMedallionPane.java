package xreliquary.client.gui.hud;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import xreliquary.client.gui.components.Box;
import xreliquary.client.gui.components.Component;
import xreliquary.client.gui.components.ItemStackPane;
import xreliquary.client.gui.components.TextPane;
import xreliquary.client.gui.components.XPBarPane;
import xreliquary.init.ModItems;
import xreliquary.reference.Colors;
import xreliquary.util.InventoryHelper;
import xreliquary.util.XpHelper;

public class HeroMedallionPane extends Component {
	private final XPBarPane xpBar;
	private final TextPane levelPane;
	private final Box mainPane;

	public HeroMedallionPane() {
		xpBar = new XPBarPane();
		levelPane = new TextPane("0", Colors.get(Colors.GREEN));
		mainPane = Box.createVertical(Box.Alignment.RIGHT, xpBar, Box.createHorizontal(Box.Alignment.MIDDLE, new ItemStackPane(ModItems.HERO_MEDALLION.get()), levelPane));
	}

	@Override
	public int getHeightInternal() {
		return mainPane.getHeight();
	}

	@Override
	public int getWidthInternal() {
		return mainPane.getWidth();
	}

	@Override
	public boolean shouldRender() {
		return !InventoryHelper.getCorrectItemFromEitherHand(Minecraft.getInstance().player, ModItems.HERO_MEDALLION.get()).isEmpty();
	}

	@Override
	public int getPadding() {
		return 1;
	}

	@Override
	public void renderInternal(MatrixStack matrixStack, int x, int y) {
		ItemStack item = InventoryHelper.getCorrectItemFromEitherHand(Minecraft.getInstance().player, ModItems.HERO_MEDALLION.get());

		if (item.isEmpty()) {
			return;
		}

		int experience = ModItems.HERO_MEDALLION.get().getExperience(item);
		int level = XpHelper.getLevelForExperience(experience);
		levelPane.setText(String.valueOf(level));

		int remainingExperience = experience - XpHelper.getExperienceForLevel(level);
		int maxBarExperience = XpHelper.getExperienceLimitOnLevel(level);

		float xpRatio = ((float) remainingExperience) / ((float) maxBarExperience);
		xpBar.setXpRatio(xpRatio);

		mainPane.render(matrixStack, x, y);
	}
}
