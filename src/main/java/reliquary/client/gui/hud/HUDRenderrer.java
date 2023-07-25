package reliquary.client.gui.hud;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Tuple;
import reliquary.client.gui.components.Component;

public class HUDRenderrer {
	private HUDRenderrer() {}

	public static void render(GuiGraphics guiGraphics, Component component, HUDPosition position) {
		if (component.shouldRender()) {
			Minecraft mc = Minecraft.getInstance();
			Window mainWindow = mc.getWindow();
			Tuple<Integer, Integer> xy = getXYPosition(mainWindow, component, position);

			component.render(guiGraphics, xy.getA(), xy.getB());
		}
	}

	private static Tuple<Integer, Integer> getXYPosition(Window sr, Component component, HUDPosition position) {
		return switch (position) {
			case BOTTOM_LEFT -> new Tuple<>(0, sr.getGuiScaledHeight() - component.getHeight());
			case LEFT -> new Tuple<>(0, (sr.getGuiScaledHeight() - component.getHeight()) / 2);
			case TOP_LEFT -> new Tuple<>(0, 0);
			case TOP -> new Tuple<>((sr.getGuiScaledWidth() - component.getWidth()) / 2, 0);
			case TOP_RIGHT -> new Tuple<>(sr.getGuiScaledWidth() - component.getWidth(), 0);
			case RIGHT -> new Tuple<>(sr.getGuiScaledWidth() - component.getWidth(), (sr.getGuiScaledHeight() - component.getHeight()) / 2);
			default -> new Tuple<>(sr.getGuiScaledWidth() - component.getWidth(), sr.getGuiScaledHeight() - component.getHeight());
		};
	}

}
