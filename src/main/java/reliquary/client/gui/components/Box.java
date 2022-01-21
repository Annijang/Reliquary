package reliquary.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;

public class Box extends Component {
	private final Layout layout;
	private final Alignment alignment;
	private final Component[] components;
	private int height;
	private int width;

	public Box(Layout layout, Alignment alignment, Component... components) {
		this.layout = layout;
		this.alignment = alignment;
		this.components = components;
		updateDimensions();
	}

	private void updateDimensions() {
		int updatedHeight = 0;
		int updatedWidth = 0;

		for (Component component : components) {
			if (layout == Layout.HORIZONTAL) {
				updatedHeight = Math.max(updatedHeight, component.getHeight());
				updatedWidth += component.getWidth();
			} else {
				updatedHeight += component.getHeight();
				updatedWidth = Math.max(updatedWidth, component.getWidth());
			}
		}
		height = updatedHeight;
		width = updatedWidth;
	}

	public static Box createVertical(Component... components) {
		return createVertical(Alignment.TOP, components);
	}

	public static Box createVertical(Alignment alignment, Component... components) {
		return new Box(Layout.VERTICAL, alignment, components);
	}

	public static Box createHorizontal(Component... components) {
		return createHorizontal(Alignment.LEFT, components);
	}

	public static Box createHorizontal(Alignment alignment, Component... components) {
		return new Box(Layout.HORIZONTAL, alignment, components);
	}

	@Override
	public int getHeightInternal() {
		return height;
	}

	@Override
	public int getWidthInternal() {
		return width;
	}

	@Override
	public int getPadding() {
		return 0;
	}

	@Override
	public void renderInternal(PoseStack matrixStack, int x, int y) {
		updateDimensions();

		for (Component component : components) {
			if (!component.shouldRender()) {
				continue;
			}

			int componentX = x;
			int componentY = y;

			switch (alignment) {
				case MIDDLE -> {
					componentX += layout == Layout.VERTICAL && component.getWidth() < width ? (width - component.getWidth()) / 2 : 0;
					componentY += layout == Layout.HORIZONTAL && component.getHeight() < height ? (height - component.getHeight()) / 2 : 0;
				}
				case RIGHT -> componentX += layout == Layout.VERTICAL && component.getWidth() < width ? width - component.getWidth() : 0;
				case BOTTOM -> componentY += layout == Layout.HORIZONTAL && component.getHeight() < height ? height - component.getHeight() : 0;
			}

			component.render(matrixStack, componentX, componentY);

			if (layout == Layout.HORIZONTAL) {
				x += component.getWidth();
			} else {
				y += component.getHeight();
			}
		}
	}

	public enum Layout {
		HORIZONTAL, VERTICAL
	}

	public enum Alignment {
		MIDDLE, LEFT, RIGHT, TOP, BOTTOM
	}
}
