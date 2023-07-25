package reliquary.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import reliquary.reference.Colors;

public class ItemStackCountPane extends Component {
	private final ItemStackPane itemStackPane;
	private final TextPane countPane;
	private final Box box;

	public ItemStackCountPane(ItemStack itemStack, int count) {
		this(itemStack, count, Colors.get(Colors.PURE));
	}

	public ItemStackCountPane(ItemStack itemStack, int count, int textColor) {
		this(Box.Layout.HORIZONTAL, itemStack, count, textColor);
	}

	public ItemStackCountPane(Box.Layout layout, ItemStack itemStack, int count, int textColor) {
		countPane = new TextPane(String.valueOf(count), textColor);
		itemStackPane = new ItemStackPane(itemStack);
		box = new Box(layout, Box.Alignment.MIDDLE, itemStackPane, countPane);
	}

	public void setCount(int count) {
		countPane.setText(String.valueOf(count));
	}

	public void setItemStack(ItemStack itemStack) {
		itemStackPane.setItemStack(itemStack);
	}

	@Override
	public int getPadding() {
		return box.getPadding();
	}

	@Override
	public int getHeightInternal() {
		return box.getHeightInternal();
	}

	@Override
	public int getWidthInternal() {
		return box.getWidthInternal();
	}

	@Override
	public void renderInternal(GuiGraphics guiGraphics, int x, int y) {
		box.renderInternal(guiGraphics, x, y);
	}
}
