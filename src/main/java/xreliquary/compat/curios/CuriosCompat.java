package xreliquary.compat.curios;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import xreliquary.client.render.MobCharmBeltLayerRenderer;
import xreliquary.compat.ICompat;
import xreliquary.init.ModItems;
import xreliquary.items.util.IBaubleItem;
import xreliquary.reference.Compatibility;
import xreliquary.reference.Reference;
import xreliquary.util.InventoryHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CuriosCompat implements ICompat {

	private static final EmptyCuriosHandler EMPTY_HANDLER = new EmptyCuriosHandler();

	public CuriosCompat() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::sendImc);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void sendImc(InterModEnqueueEvent evt) {
		InterModComms.sendTo(Compatibility.MOD_ID.CURIOS, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.NECKLACE.getMessageBuilder().build());
		InterModComms.sendTo(Compatibility.MOD_ID.CURIOS, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BODY.getMessageBuilder().build());
		InterModComms.sendTo(Compatibility.MOD_ID.CURIOS, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.BELT.getMessageBuilder().build());
	}

	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> evt) {
		ItemStack stack = evt.getObject();
		Item item = stack.getItem();
		if (item == ModItems.MOB_CHARM_BELT.get()) {
			evt.addCapability(new ResourceLocation(Reference.MOD_ID, item.getRegistryName().getPath() + "_curios"), new ICapabilityProvider() {
				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
					return CuriosCapability.ITEM.orEmpty(cap, LazyOptional.of(() -> new CuriosBaubleItemWrapper((IBaubleItem) item) {
						@Override
						public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
							return true;
						}

						@Override
						public void render(String identifier, int index, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
							MobCharmBeltLayerRenderer.renderBelt(matrixStack, renderTypeBuffer, light, livingEntity);
						}
					}));
				}
			});
		} else if (item.getRegistryName() != null && item.getRegistryName().getNamespace().equals(Reference.MOD_ID) && item instanceof IBaubleItem) {
			evt.addCapability(new ResourceLocation(Reference.MOD_ID, item.getRegistryName().getPath() + "_curios"), new ICapabilityProvider() {
				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
					return CuriosCapability.ITEM.orEmpty(cap, LazyOptional.of(() -> new CuriosBaubleItemWrapper((IBaubleItem) item)));
				}
			});
		}
	}

	@Override
	public void setup() {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> new CuriosFortuneCoinToggler().registerSelf());
		ModItems.MOB_CHARM.get().setCharmInventoryHandler(new CuriosCharmInventoryHandler());
		InventoryHelper.addBaublesItemHandlerFactory((player, type) -> (CuriosApi.getCuriosHelper().getCuriosHandler(player)
				.map(handler -> handler.getStacksHandler(type.getIdentifier()).map(ICurioStacksHandler::getStacks).orElse(EMPTY_HANDLER)).orElse(EMPTY_HANDLER)));
	}

	public static Optional<ItemStack> getStackInSlot(LivingEntity entity, String slotName, int slot) {
		return CuriosApi.getCuriosHelper().getCuriosHandler(entity).map(handler -> handler.getStacksHandler(slotName)
				.map(sh -> sh.getStacks().getStackInSlot(slot))).orElse(Optional.empty());
	}

	public static void setStackInSlot(LivingEntity entity, String slotName, int slot, ItemStack stack) {
		CuriosApi.getCuriosHelper().getCuriosHandler(entity).ifPresent(handler -> handler.getStacksHandler(slotName).ifPresent(sh -> sh.getStacks().setStackInSlot(slot, stack)));
	}

	private static class EmptyCuriosHandler extends ItemStackHandler implements IDynamicStackHandler {
		@Override
		public void setPreviousStackInSlot(int i, @Nonnull ItemStack itemStack) {
			//noop
		}

		@Override
		public ItemStack getPreviousStackInSlot(int i) {
			return ItemStack.EMPTY;
		}

		@Override
		public void grow(int i) {
			//noop
		}

		@Override
		public void shrink(int i) {
			//noop
		}
	}
}
