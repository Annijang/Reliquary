/* TODO readd waila integration
package xreliquary.compat.waila.provider;

import com.mojang.realmsclient.gui.ChatFormatting;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import xreliquary.blocks.PedestalBlock;
import xreliquary.blocks.tile.PedestalTileEntity;
import xreliquary.init.ModBlocks;

import java.text.MessageFormat;
import java.util.List;

public class DataProviderPedestal implements IWailaDataProvider {

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return new ItemStack(ModBlocks.pedestal);
	}


	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		if(!(accessor.getBlock() instanceof PedestalBlock && accessor.getTileEntity() instanceof PedestalTileEntity))
			return currenttip;

		PedestalTileEntity pedestal = (PedestalTileEntity) accessor.getTileEntity();
		BlockState pedestalState = accessor.getBlockState();

		if(pedestalState.get(PedestalBlock.ENABLED)) {
			//TODO translate in lang file if there will be a need for that in the future
			currenttip.add(ChatFormatting.GREEN + "ON");
			if(pedestal.isSwitchedOn()) {
				currenttip.add("Switch");
			}
			if(pedestal.isPowered()) {
				currenttip.add("Redstone");
			}
			if(pedestal.getOnSwitches().size() > 0) {
				for(long loc : pedestal.getOnSwitches()) {
					BlockPos pos = BlockPos.fromLong(loc);
					currenttip.add(MessageFormat.format("Remote at: {0}, {1}, {2}", pos.getX(), pos.getY(), pos.getZ()));
				}
			}
		} else {
			currenttip.add(ChatFormatting.RED + "OFF");
		}

		return currenttip;
	}
}
*/
