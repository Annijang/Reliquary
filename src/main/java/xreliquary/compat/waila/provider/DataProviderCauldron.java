/* TODO readd waila integration
package xreliquary.compat.waila.provider;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.ChatFormatting;
import xreliquary.blocks.ApothecaryCauldronBlock;
import xreliquary.blocks.tile.ApothecaryCauldronTileEntity;
import xreliquary.init.ModBlocks;
import xreliquary.util.LanguageHelper;
import xreliquary.util.potions.XRPotionHelper;

import java.util.ArrayList;
import java.util.List;

public class DataProviderCauldron extends CachedBodyDataProvider {

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return new ItemStack(ModBlocks.APOTHECARY_CAULDRON);
	}

	@Override
	public List<String> getWailaBodyToCache(ItemStack itemStack, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		List<String> currenttip = new ArrayList<>();

		if(!(accessor.getBlock() instanceof ApothecaryCauldronBlock && accessor.getTileEntity() instanceof ApothecaryCauldronTileEntity))
			return currenttip;

		ApothecaryCauldronTileEntity cauldron = (ApothecaryCauldronTileEntity) accessor.getTileEntity();

		if(cauldron == null || cauldron.effects.isEmpty())
			return currenttip;

		if(!cauldron.hasNetherwart)
			currenttip.add(ChatFormatting.RED + LanguageHelper.getLocalization("waila.xreliquary.cauldron.missing_netherwart") + ChatFormatting.RESET);

		if(!cauldron.hasGunpowder && cauldron.hasDragonBreath)
			currenttip.add(ChatFormatting.RED + LanguageHelper.getLocalization("waila.xreliquary.cauldron.missing_gunpowder") + ChatFormatting.RESET);

		StringBuilder ingredients = new StringBuilder();
		if(cauldron.redstoneCount > 0) {
			ingredients.append(cauldron.redstoneCount);
			ingredients.append("x");
			ingredients.append(new ItemStack(Items.REDSTONE).getDisplayName());
			ingredients.append(" ");
		}

		if(cauldron.glowstoneCount > 0) {
			ingredients.append(cauldron.glowstoneCount);
			ingredients.append("x");
			ingredients.append(new ItemStack(Items.GLOWSTONE_DUST).getDisplayName());
		}

		currenttip.add(ingredients.toString());

		if (cauldron.hasDragonBreath) {
			currenttip.add(ChatFormatting.WHITE.toString() + cauldron.getLiquidLevel() + "x" + LanguageHelper.getLocalization("waila.xreliquary.cauldron.lingering") + ChatFormatting.RESET);
		}
		else if(cauldron.hasGunpowder) {
			currenttip.add(ChatFormatting.WHITE.toString() + cauldron.getLiquidLevel() + "x" + LanguageHelper.getLocalization("waila.xreliquary.cauldron.splash") + ChatFormatting.RESET);
		} else {
			currenttip.add(ChatFormatting.WHITE.toString() + cauldron.getLiquidLevel() + "x" + LanguageHelper.getLocalization("waila.xreliquary.cauldron.potion") + ChatFormatting.RESET);
		}

		XRPotionHelper.addPotionTooltip(cauldron.effects, currenttip, false, false);

		return currenttip;
	}
}
*/
