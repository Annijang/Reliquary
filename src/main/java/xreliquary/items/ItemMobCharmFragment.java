package xreliquary.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import xreliquary.Reliquary;
import xreliquary.reference.Names;

import javax.annotation.Nonnull;

public class ItemMobCharmFragment extends ItemBase {
	public ItemMobCharmFragment() {
		super(Names.Items.MOB_CHARM_FRAGMENT);
		this.setCreativeTab(Reliquary.CREATIVE_TAB);
		this.setMaxDamage(0);
		this.setMaxStackSize(64);
		this.setHasSubtypes(true);
		canRepair = false;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item.mob_charm_fragment_" + stack.getItemDamage();
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
		if (!isInCreativeTab(tab))
			return;

		for(int i = 0; i < ItemMobCharm.CHARM_DEFINITIONS.size(); i++)
			list.add(new ItemStack(this, 1, i));
	}
}
