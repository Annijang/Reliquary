package xreliquary.client.gui.components;

public abstract class Component {
	public int getPadding() {
		return 1;
	}

	public int getHeight() {
		return getHeightInternal() + getPadding() * 2;
	}

	public int getWidth() {
		return getWidthInternal() + getPadding() * 2;
	}

	public void render(int x, int y) {
		if (hasChanged()) {
			refresh();
			setChanged(false);
		}
		renderInternal(x + getPadding(), y + getPadding());
	}

	protected void refresh() {
	}

	public boolean hasChanged() {
		return false;
	}

	public void setChanged(boolean changed) {
	}

	public boolean shouldRender() {
		return true;
	}

	public abstract int getHeightInternal();
	public abstract int getWidthInternal();
	public abstract void renderInternal(int x, int y);
}
