package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;
import xreliquary.init.ModBlocks;
import xreliquary.init.ModItems;
import xreliquary.items.util.FilteredItemStackHandler;
import xreliquary.items.util.VoidTearItemStackHandler;
import xreliquary.network.PacketHandler;
import xreliquary.network.PacketItemHandlerSync;
import xreliquary.reference.Names;
import xreliquary.reference.Settings;
import xreliquary.util.InventoryHelper;
import xreliquary.util.LanguageHelper;
import xreliquary.util.NBTHelper;
import xreliquary.util.StackHelper;

import java.util.List;
import java.util.Random;

public class ItemVoidTear extends ItemToggleable {

	public ItemVoidTear() {
		super(Names.Items.VOID_TEAR);
		setMaxStackSize(1);
		setNoRepair();
		//noinspection ConstantConditions
		this.setCreativeTab(null);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new ICapabilitySerializable<NBTTagCompound>() {
			VoidTearItemStackHandler itemHandler = new VoidTearItemStackHandler();

			@Override
			public NBTTagCompound serializeNBT() {
				return itemHandler.serializeNBT();
			}

			@Override
			public void deserializeNBT(NBTTagCompound nbt) {
				itemHandler.deserializeNBT(nbt);
			}

			@Override
			public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
				return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
			}

			@Override
			public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
				if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					//noinspection unchecked
					return (T) itemHandler;

				return null;
			}
		};
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) && super.hasEffect(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
		this.formatTooltip(null, stack, list);

		ItemStack contents = this.getContainedItem(stack);

		if(contents == null)
			return;

		if(this.isEnabled(stack)) {
			LanguageHelper.formatTooltip("tooltip.absorb_active", ImmutableMap.of("item", TextFormatting.YELLOW + contents.getDisplayName()), list);
			list.add(LanguageHelper.getLocalization("tooltip.absorb_tear"));
		}
		LanguageHelper.formatTooltip("tooltip.tear_quantity", ImmutableMap.of("item", contents.getDisplayName(), "amount", Integer.toString(getItemQuantity(stack))), list);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack ist, World world, EntityPlayer player, EnumHand hand) {
		if(!world.isRemote) {

			if(getItemQuantity(ist) == 0)
				return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(ModItems.emptyVoidTear, 1, 0));

			RayTraceResult movingObjectPosition = this.rayTrace(world, player, false);

			//not enabling void tear if player tried to deposit everything into inventory but there wasn't enough space
			if(movingObjectPosition != null && movingObjectPosition.typeOfHit == RayTraceResult.Type.BLOCK && world.getTileEntity(movingObjectPosition.getBlockPos()) instanceof IInventory && player.isSneaking())
				return new ActionResult<>(EnumActionResult.PASS, ist);

			if(player.isSneaking())
				return super.onItemRightClick(ist, world, player, hand);
			if(this.attemptToEmptyIntoInventory(ist, player, player.inventory)) {
				player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));
				NBTHelper.resetTag(ist);
				return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(ModItems.emptyVoidTear, 1, 0));
			}
		}

		player.inventoryContainer.detectAndSendChanges();
		return new ActionResult<>(EnumActionResult.PASS, ist);
	}

	@Override
	public void onUpdate(ItemStack voidTear, World world, Entity entity, int slotNumber, boolean isSelected) {
		if(voidTear.getTagCompound() != null && voidTear.getTagCompound().hasKey("item")) {

			setItemStack(voidTear, ItemStack.loadItemStackFromNBT(NBTHelper.getTagCompound("item", voidTear)));
			setItemQuantity(voidTear, NBTHelper.getInteger("itemQuantity", voidTear));

			voidTear.getTagCompound().removeTag("item");
			voidTear.getTagCompound().removeTag("itemQuantity");
		}

		if(!world.isRemote) {
			if(!(entity instanceof EntityPlayer))
				return;

			EntityPlayer player = (EntityPlayer) entity;

			boolean quantityUpdated = false;
			if(this.isEnabled(voidTear)) {
				ItemStack contents = this.getContainedItem(voidTear);

				if(contents != null) {
					int itemQuantity = InventoryHelper.getItemQuantity(contents, player.inventory);

					if(getItemQuantity(voidTear) <= Settings.VoidTear.itemLimit && itemQuantity > contents.getMaxStackSize() && InventoryHelper.consumeItem(contents, player, contents.getMaxStackSize(), itemQuantity - contents.getMaxStackSize())) {
						//doesn't absorb in creative mode.. this is mostly for testing, it prevents the item from having unlimited *whatever* for eternity.
						if(!player.capabilities.isCreativeMode) {
							setItemQuantity(voidTear, getItemQuantity(voidTear) + itemQuantity - contents.getMaxStackSize());
							quantityUpdated = true;
						}
					}

					if(attemptToReplenishSingleStack(player, voidTear))
						quantityUpdated = true;
				}
			}

			//noinspection ConstantConditions
			if(player.inventory.getStackInSlot(slotNumber) != null && player.inventory.getStackInSlot(slotNumber).getItem() == ModItems.filledVoidTear && (isSelected || quantityUpdated)) {
				PacketHandler.networkWrapper.sendTo(new PacketItemHandlerSync(slotNumber, getItemHandlerNBT(voidTear)), (EntityPlayerMP) player);
			} else if(player.inventory.offHandInventory[0] != null && player.inventory.offHandInventory[0].getItem() == ModItems.filledVoidTear) {
				PacketHandler.networkWrapper.sendTo(new PacketItemHandlerSync(EnumHand.OFF_HAND, getItemHandlerNBT(voidTear)), (EntityPlayerMP) player);
			}

		}
	}

	private NBTTagCompound getItemHandlerNBT(ItemStack ist) {
		IItemHandler itemHandler = ist.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if(!(itemHandler instanceof FilteredItemStackHandler))
			return null;

		FilteredItemStackHandler filteredHandler = (FilteredItemStackHandler) itemHandler;

		return filteredHandler.serializeNBT();
	}

	private boolean attemptToReplenish(EntityPlayer player, ItemStack voidTear) {
		IInventory inventory = player.inventory;
		for(int slot = 0; slot < inventory.getSizeInventory(); slot++) {
			ItemStack stackFound = inventory.getStackInSlot(slot);
			if(stackFound == null) {
				continue;
			}
			if(StackHelper.isItemAndNbtEqual(stackFound, getContainedItem(voidTear))) {
				int quantityToDecrease = Math.min(stackFound.getMaxStackSize() - stackFound.stackSize, getItemQuantity(voidTear) - 1);
				stackFound.stackSize += quantityToDecrease;
				setItemQuantity(voidTear, getItemQuantity(voidTear) - quantityToDecrease);
				if (getMode(voidTear) != Mode.FULL_INVENTORY)
					return true;
			}
		}

		int slot;
		while (getItemQuantity(voidTear) > 1 && (slot = player.inventory.getFirstEmptyStack()) != -1  ) {
			ItemStack newStack = getContainedItem(voidTear).copy();
			int quantityToDecrease = Math.min(newStack.getMaxStackSize(), getItemQuantity(voidTear) - 1);
			newStack.stackSize = quantityToDecrease;
			player.inventory.setInventorySlotContents(slot, newStack);
			setItemQuantity(voidTear, getItemQuantity(voidTear) - quantityToDecrease);
			if (getMode(voidTear) != Mode.FULL_INVENTORY)
				return true;
		}

		return false;
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack ist, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if(world.getBlockState(pos).getBlock() == ModBlocks.pedestal)
			return EnumActionResult.PASS;

		if(!world.isRemote) {
			if(world.getTileEntity(pos) instanceof IInventory) {
				IInventory inventory = (IInventory) world.getTileEntity(pos);

				if(inventory instanceof TileEntityChest && world.getBlockState(pos).getBlock() instanceof BlockChest) {
					inventory = ((BlockChest) world.getBlockState(pos).getBlock()).getLockableContainer(world, pos);
				}

				//enabled == drinking mode, we're going to drain the inventory of items.
				if(this.isEnabled(ist)) {
					this.drainInventory(ist, player, inventory);
				} else {
					//disabled == placement mode, try and stuff the tear's contents into the inventory
					this.attemptToEmptyIntoInventory(ist, player, inventory);
					if(!player.isSneaking() && !(getItemQuantity(ist) > 0)) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(ModItems.emptyVoidTear, 1, 0));
					}
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	private boolean attemptToEmptyIntoInventory(ItemStack ist, EntityPlayer player, IInventory inventory) {
		ItemStack contents = this.getContainedItem(ist);
		contents.stackSize = 1;

		int quantity = getItemQuantity(ist);
		int maxNumberToEmpty = player.isSneaking() ? quantity : Math.min(contents.getMaxStackSize(), quantity);

		quantity -= InventoryHelper.tryToAddToInventory(contents, inventory, maxNumberToEmpty);

		setItemQuantity(ist, quantity);
		if(quantity == 0) {
			player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.8F));
			return true;
		} else {
			player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));
			return false;
		}
	}

	private void drainInventory(ItemStack ist, EntityPlayer player, IInventory inventory) {
		ItemStack contents = this.getContainedItem(ist);
		int quantity = getItemQuantity(ist);

		int quantityDrained = InventoryHelper.tryToRemoveFromInventory(contents, inventory, Settings.VoidTear.itemLimit - quantity);

		if(!(quantityDrained > 0))
			return;

		player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));

		setItemQuantity(ist, quantity + quantityDrained);
	}

	public ItemStack getContainedItem(ItemStack voidTear) {
		IItemHandler itemHandler = voidTear.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if(!(itemHandler instanceof FilteredItemStackHandler))
			return null;

		FilteredItemStackHandler filteredHandler = (FilteredItemStackHandler) itemHandler;
		ItemStack stackToReturn = null;
		if(filteredHandler.getStackInParentSlot(0) != null) {
			stackToReturn = filteredHandler.getStackInParentSlot(0).copy();
			stackToReturn.stackSize = filteredHandler.getTotalAmount(0);
		}

		return stackToReturn;
	}

	void setItemStack(ItemStack voidTear, ItemStack stack) {
		IItemHandler itemHandler = voidTear.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if(!(itemHandler instanceof FilteredItemStackHandler))
			return;

		FilteredItemStackHandler filteredHandler = (FilteredItemStackHandler) itemHandler;
		filteredHandler.setParentSlotStack(0, stack);
	}

	void setItemQuantity(ItemStack voidTear, int quantity) {
		IItemHandler itemHandler = voidTear.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if(!(itemHandler instanceof FilteredItemStackHandler))
			return;

		FilteredItemStackHandler filteredHandler = (FilteredItemStackHandler) itemHandler;
		filteredHandler.setTotalAmount(0, quantity);
	}

	private int getItemQuantity(ItemStack voidTear) {
		IItemHandler itemHandler = voidTear.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		if(!(itemHandler instanceof FilteredItemStackHandler))
			return 0;

		FilteredItemStackHandler filteredHandler = (FilteredItemStackHandler) itemHandler;
		return filteredHandler.getTotalAmount(0);
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack voidTear) {
		if(entityLiving.worldObj.isRemote || !(entityLiving instanceof EntityPlayer))
			return false;

		EntityPlayer player = (EntityPlayer) entityLiving;
		if(player.isSneaking()) {
			cycleMode(voidTear);
			return true;
		}
		return false;
	}

	public enum Mode {
		ONE_STACK,
		FULL_INVENTORY,
		NO_REFILL
	}

	public Mode getMode(ItemStack voidTear) {
		if(NBTHelper.getString("mode", voidTear).isEmpty()) {
			setMode(voidTear, Mode.ONE_STACK);
		}
		return Mode.valueOf(NBTHelper.getString("mode", voidTear));
	}

	private void setMode(ItemStack voidTear, Mode mode) {
		NBTHelper.setString("mode", voidTear, mode.toString());
	}

	private void cycleMode(ItemStack voidTear) {
		Mode mode = getMode(voidTear);
		switch(mode) {
			case ONE_STACK:
				setMode(voidTear, Mode.FULL_INVENTORY);
				break;
			case FULL_INVENTORY:
				setMode(voidTear, Mode.NO_REFILL);
				break;
			case NO_REFILL:
				setMode(voidTear, Mode.ONE_STACK);
				break;
		}
	}

	public int getKeepQuantity(ItemStack voidTear) {
		Mode mode = getMode(voidTear);

		if (mode == Mode.NO_REFILL)
			return 0;
		if (mode == Mode.ONE_STACK)
			return getContainedItem(voidTear).getMaxStackSize();

		return Integer.MAX_VALUE;
	}

	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent event) {
		ItemStack pickedUpStack = event.getItem().getEntityItem();
		EntityPlayer player = event.getEntityPlayer();
		EntityItem itemEntity = event.getItem();

		for(int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
			ItemStack tearStack = player.inventory.getStackInSlot(slot);
			if(tearStack != null && tearStack.getItem() == this && this.isEnabled(tearStack)) {
				int tearItemQuantity = this.getItemQuantity(tearStack);
				if(canAbsorbStack(pickedUpStack, tearStack)) {
					int playerItemQuantity = InventoryHelper.getItemQuantity(pickedUpStack, player.inventory);

					if (playerItemQuantity + pickedUpStack.stackSize >= getKeepQuantity(tearStack) || player.inventory.getFirstEmptyStack() == -1) {
						this.setItemQuantity(tearStack, tearItemQuantity + pickedUpStack.stackSize);
						if (!itemEntity.isSilent()) {
							Random rand = new Random();
							itemEntity.worldObj.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
						}
						itemEntity.setDead();
						event.setCanceled(true);
						break;
					}
				}
			}
		}
	}

	boolean canAbsorbStack(ItemStack pickedUpStack, ItemStack tearStack) {
		return StackHelper.isItemAndNbtEqual(this.getContainedItem(tearStack), pickedUpStack) && this.getItemQuantity(tearStack) + pickedUpStack.stackSize <= Settings.VoidTear.itemLimit;
	}
}
