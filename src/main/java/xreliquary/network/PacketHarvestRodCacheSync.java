package xreliquary.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xreliquary.items.util.HarvestRodPlayerProps;

public class PacketHarvestRodCacheSync implements IMessage, IMessageHandler<PacketHarvestRodCacheSync, IMessage> {

	private int timesUsed;

	public PacketHarvestRodCacheSync() {
	}

	public PacketHarvestRodCacheSync(int timesUsed) {
		this.timesUsed = timesUsed;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		timesUsed = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(timesUsed);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage onMessage(PacketHarvestRodCacheSync message, MessageContext ctx) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		HarvestRodPlayerProps props = HarvestRodPlayerProps.get(player);
		if(props == null) {
			HarvestRodPlayerProps.register(player);
			props = HarvestRodPlayerProps.get(player);
		}

		props.setTimesUsed(message.timesUsed);

		return null;
	}
}
