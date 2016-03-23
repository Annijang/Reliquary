package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xreliquary.init.ModItems;
import xreliquary.reference.Names;
import xreliquary.reference.Settings;
import xreliquary.util.InventoryHelper;
import xreliquary.util.LanguageHelper;
import xreliquary.util.NBTHelper;
import xreliquary.util.StackHelper;

import java.util.List;

public class ItemVoidTear extends ItemToggleable {

	public ItemVoidTear() {
		super(Names.void_tear);
		setMaxStackSize(1);
		canRepair = false;
		this.setCreativeTab(null);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {

		this.formatTooltip(null, stack, list);

		ItemStack contents = this.getContainedItem(stack);

		if(contents == null)
			return;

		if(this.isEnabled(stack)) {
			LanguageHelper.formatTooltip("tooltip.absorb_active", ImmutableMap.of("item", TextFormatting.YELLOW + contents.getDisplayName()), stack, list);
			list.add(LanguageHelper.getLocalization("tooltip.absorb_tear"));
		}
		LanguageHelper.formatTooltip("tooltip.tear_quantity", ImmutableMap.of("item", contents.getDisplayName(), "amount", Integer.toString(NBTHelper.getInteger("itemQuantity", stack))), stack, list);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack ist, World world, EntityPlayer player, EnumHand hand) {
		if(!world.isRemote) {

			if(NBTHelper.getInteger("itemQuantity", ist) == 0)
				return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(ModItems.emptyVoidTear, 1, 0));

			RayTraceResult movingObjectPosition = this.getMovingObjectPositionFromPlayer(world, player, false);

			//not enabling void tear if player tried to deposit everything into inventory but there wasn't enough space
			if(movingObjectPosition != null && movingObjectPosition.typeOfHit == RayTraceResult.Type.BLOCK && world.getTileEntity(movingObjectPosition.getBlockPos()) instanceof IInventory && player.isSneaking())
				return new ActionResult<>(EnumActionResult.PASS, ist);

			if(player.isSneaking())
				return super.onItemRightClick(ist, world, player, hand);
			if(this.attemptToEmptyIntoInventory(ist, player, player.inventory, player.inventory.mainInventory.length)) {
				player.worldObj.playSound(null, player.getPosition(), SoundEvents.entity_experience_orb_touch, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));
				NBTHelper.resetTag(ist);
				return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(ModItems.emptyVoidTear, 1, 0));
			}
		}

		player.inventoryContainer.detectAndSendChanges();
		return new ActionResult<>(EnumActionResult.PASS, ist);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean f) {
		if(!world.isRemote) {
			if(!this.isEnabled(stack) || !(entity instanceof EntityPlayer))
				return;

			EntityPlayer player = (EntityPlayer) entity;

			ItemStack contents = this.getContainedItem(stack);

			int itemQuantity = InventoryHelper.getItemQuantity(contents, player.inventory);

			if(NBTHelper.getInteger("itemQuantity", stack) < Settings.VoidTear.itemLimit && itemQuantity > contents.getMaxStackSize() && InventoryHelper.consumeItem(contents, player, contents.getMaxStackSize(), itemQuantity - contents.getMaxStackSize())) {
				//doesn't absorb in creative mode.. this is mostly for testing, it prevents the item from having unlimited *whatever* for eternity.
				if(!player.capabilities.isCreativeMode)
					NBTHelper.setInteger("itemQuantity", stack, NBTHelper.getInteger("itemQuantity", stack) + itemQuantity - contents.getMaxStackSize());
			}

			attemptToReplenishSingleStack(player, stack);
		}
	}

	public void attemptToReplenishSingleStack(EntityPlayer player, ItemStack ist) {
		int preferredSlot = -1;
		int stackCount = 0;
		IInventory inventory = player.inventory;
		for(int slot = 0; slot < inventory.getSizeInventory(); slot++) {
			ItemStack stackFound = inventory.getStackInSlot(slot);
			if(stackFound == null) {
				continue;
			}
			if(StackHelper.isItemAndNbtEqual(stackFound, getContainedItem(ist))) {
				if(preferredSlot == -1)
					preferredSlot = slot;
				stackCount += 1;
			}
		}

		//use first empty slot for new stack if there's no stack to restock
		if(preferredSlot == -1) {
			preferredSlot = player.inventory.getFirstEmptyStack();
			if(preferredSlot > -1)
				stackCount = 1;
		}

		if(stackCount == 1 && preferredSlot != -1 && NBTHelper.getInteger("itemQuantity", ist) > 1) {
			ItemStack stackToIncrease = player.inventory.getStackInSlot(preferredSlot);
			if(stackToIncrease == null) {
				ItemStack newStack = getContainedItem(ist).copy();
				int quantityToDecrease = Math.min(newStack.getMaxStackSize(), NBTHelper.getInteger("itemQuantity", ist) - 1);
				newStack.stackSize = quantityToDecrease;
				player.inventory.setInventorySlotContents(preferredSlot, newStack);
				NBTHelper.setInteger("itemQuantity", ist, NBTHelper.getInteger("itemQuantity", ist) - quantityToDecrease);
				return;
			}

			if(stackToIncrease.stackSize < stackToIncrease.getMaxStackSize()) {
				int quantityToDecrease = Math.min(stackToIncrease.getMaxStackSize() - stackToIncrease.stackSize, NBTHelper.getInteger("itemQuantity", ist) - 1);
				stackToIncrease.stackSize += quantityToDecrease;
				NBTHelper.setInteger("itemQuantity", ist, NBTHelper.getInteger("itemQuantity", ist) - quantityToDecrease);
			}
		}
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack ist, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
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
					this.attemptToEmptyIntoInventory(ist, player, inventory, 0);
					if(!player.isSneaking() && !(NBTHelper.getInteger("itemQuantity", ist) > 0)) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(ModItems.emptyVoidTear, 1, 0));
					}
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	public ItemStack getContainedItem(ItemStack ist) {
		//something awful happened. We either lost data or this is an invalid tear by some other means. Either way, not great.
		if(NBTHelper.getString("itemID", ist).equals("") && (NBTHelper.getTagCompound("item", ist) == null || NBTHelper.getTagCompound("item", ist).hasNoTags()))
			return null;

		//backwards compatibility
		//TODO remove later
		if(!NBTHelper.getString("itemID", ist).equals("")) {
			return new ItemStack(Item.itemRegistry.getObject(new ResourceLocation(NBTHelper.getString("itemID", ist))), NBTHelper.getInteger("itemQuantity", ist));
		}

		ItemStack stackToReturn = ItemStack.loadItemStackFromNBT(NBTHelper.getTagCompound("item", ist));
		stackToReturn.stackSize = NBTHelper.getInteger("itemQuantity", ist);

		return stackToReturn;
	}

	protected boolean attemptToEmptyIntoInventory(ItemStack ist, EntityPlayer player, IInventory inventory, int limit) {
		ItemStack contents = this.getContainedItem(ist);
		contents.stackSize = 1;

		int quantity = NBTHelper.getInteger("itemQuantity", ist);
		int maxNumberToEmpty = player.isSneaking() ? quantity : Math.min(contents.getMaxStackSize(), quantity);

		quantity -= InventoryHelper.tryToAddToInventory(contents, inventory, limit, maxNumberToEmpty);

		NBTHelper.setInteger("itemQuantity", ist, quantity);
		if(quantity == 0) {
			player.worldObj.playSound(null, player.getPosition(), SoundEvents.entity_experience_orb_touch, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.8F));
			return true;
		} else {
			player.worldObj.playSound(null, player.getPosition(), SoundEvents.entity_experience_orb_touch, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));
			return false;
		}
	}

	protected void drainInventory(ItemStack ist, EntityPlayer player, IInventory inventory) {
		ItemStack contents = this.getContainedItem(ist);
		int quantity = NBTHelper.getInteger("itemQuantity", ist);

		int quantityDrained = InventoryHelper.tryToRemoveFromInventory(contents, inventory, Settings.VoidTear.itemLimit - quantity);

		if(!(quantityDrained > 0))
			return;

		player.worldObj.playSound(null, player.getPosition(), SoundEvents.entity_experience_orb_touch, SoundCategory.PLAYERS, 0.1F, 0.5F * ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.2F));

		NBTHelper.setInteger("itemQuantity", ist, quantity + quantityDrained);
	}

}
