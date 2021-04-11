package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import xreliquary.entities.EnderStaffProjectileEntity;
import xreliquary.init.ModBlocks;
import xreliquary.items.util.FilteredBigItemStack;
import xreliquary.items.util.FilteredItemHandlerProvider;
import xreliquary.items.util.FilteredItemStackHandler;
import xreliquary.items.util.ILeftClickableItem;
import xreliquary.reference.Settings;
import xreliquary.util.LanguageHelper;
import xreliquary.util.NBTHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class EnderStaffItem extends ToggleableItem implements ILeftClickableItem {

	private static final String DIMENSION_TAG = "dimensionID";
	private static final String NODE_X_TAG = "nodeX";
	private static final String NODE_Y_TAG = "nodeY";
	private static final String NODE_Z_TAG = "nodeZ";
	private static final String LONG_CAST_TAG = "long_cast";

	public EnderStaffItem() {
		super(new Properties().maxStackSize(1).setNoRepair().rarity(Rarity.EPIC));
	}

	private int getEnderStaffPearlCost() {
		return Settings.COMMON.items.enderStaff.enderPearlCastCost.get();
	}

	private int getEnderStaffNodeWarpCost() {
		return Settings.COMMON.items.enderStaff.enderPearlNodeWarpCost.get();
	}

	private int getEnderPearlWorth() {
		return Settings.COMMON.items.enderStaff.enderPearlWorth.get();
	}

	private int getEnderPearlLimit() {
		return Settings.COMMON.items.enderStaff.enderPearlLimit.get();
	}

	private int getNodeWarpCastTime() {
		return Settings.COMMON.items.enderStaff.nodeWarpCastTime.get();
	}

	public String getMode(ItemStack stack) {
		if (NBTHelper.getString("mode", stack).equals("")) {
			setMode(stack, "cast");
		}
		return NBTHelper.getString("mode", stack);
	}

	private void setMode(ItemStack stack, String s) {
		NBTHelper.putString("mode", stack, s);
	}

	private void cycleMode(ItemStack stack) {
		if (getMode(stack).equals("cast")) {
			setMode(stack, LONG_CAST_TAG);
		} else if (getMode(stack).equals(LONG_CAST_TAG)) {
			setMode(stack, "node_warp");
		} else {
			setMode(stack, "cast");
		}
	}

	@Override
	public ActionResultType onLeftClickItem(ItemStack stack, LivingEntity entity) {
		if (!entity.isSneaking()) {
			return ActionResultType.CONSUME;
		}
		if (entity.world.isRemote) {
			return ActionResultType.PASS;
		}
		cycleMode(stack);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return new FilteredItemHandlerProvider(Collections.singletonList(new FilteredItemStackHandler.RemovableStack(
				new FilteredBigItemStack(Items.ENDER_PEARL, Settings.COMMON.items.enderStaff.enderPearlLimit.get(),
						Settings.COMMON.items.enderStaff.enderPearlWorth.get())
				, false)));
	}

	@Override
	public void inventoryTick(ItemStack staff, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (world.isRemote || world.getGameTime() % 10 != 0) {
			return;
		}

		PlayerEntity player = null;
		if (entity instanceof PlayerEntity) {
			player = (PlayerEntity) entity;
		}
		if (player == null) {
			return;
		}

		if (!isEnabled(staff)) {
			return;
		}

		int pearlCharge = getPearlCount(staff);
		consumeAndCharge(player, getEnderPearlLimit() - pearlCharge, getEnderPearlWorth(), Items.ENDER_PEARL, 16,
				chargeToAdd -> setPearlCount(staff, pearlCharge + chargeToAdd));
	}

	private void setPearlCount(ItemStack stack, int count) {
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(itemHandler -> {
			if (!(itemHandler instanceof FilteredItemStackHandler)) {
				return;
			}
			FilteredItemStackHandler filteredHandler = (FilteredItemStackHandler) itemHandler;
			filteredHandler.setTotalAmount(0, count);
		});
	}

	private int getPearlCount(ItemStack staff) {
		return getPearlCount(staff, false);
	}

	public int getPearlCount(ItemStack staff, boolean isClient) {
		if (isClient) {
			return NBTHelper.getInt("count", staff);
		}

		return staff.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).map(itemHandler -> {
			if (!(itemHandler instanceof FilteredItemStackHandler)) {
				return 0;
			}
			FilteredItemStackHandler filteredHandler = (FilteredItemStackHandler) itemHandler;
			return filteredHandler.getTotalAmount(0);
		}).orElse(0);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity entityLivingBase, int unadjustedCount) {
		if (!(entityLivingBase instanceof PlayerEntity)) {
			return;
		}

		PlayerEntity player = (PlayerEntity) entityLivingBase;

		for (int particles = 0; particles < 2; particles++) {
			player.world.addParticle(ParticleTypes.PORTAL, player.getPosX(), player.getPosY(), player.getPosZ(), player.world.rand.nextGaussian(), player.world.rand.nextGaussian(), player.world.rand.nextGaussian());
		}
		if (unadjustedCount == 1) {
			player.stopActiveHand();
		}
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return getNodeWarpCastTime();
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		if (!(entityLiving instanceof PlayerEntity)) {
			return;
		}

		PlayerEntity player = (PlayerEntity) entityLiving;

		if (timeLeft == 1) {
			doWraithNodeWarpCheck(stack, player.world, player);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!player.isSneaking()) {
			if (getMode(stack).equals("cast") || getMode(stack).equals(LONG_CAST_TAG)) {
				if (player.isSwingInProgress || (getPearlCount(stack) < getEnderStaffPearlCost() && !player.isCreative())) {
					return new ActionResult<>(ActionResultType.FAIL, stack);
				}
				player.swingArm(hand);
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
				if (!player.world.isRemote) {
					EnderStaffProjectileEntity enderStaffProjectile = new EnderStaffProjectileEntity(player.world, player, !getMode(stack).equals(LONG_CAST_TAG));
					enderStaffProjectile.func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
					player.world.addEntity(enderStaffProjectile);
					if (!player.isCreative()) {
						setPearlCount(stack, getPearlCount(stack) - getEnderStaffPearlCost());
					}
				}
			} else {
				player.setActiveHand(hand);
			}
		}
		return super.onItemRightClick(world, player, hand);
	}

	private void doWraithNodeWarpCheck(ItemStack stack, World world, PlayerEntity player) {
		CompoundNBT tag = stack.getTag();
		if (tag == null || (getPearlCount(stack) < getEnderStaffNodeWarpCost() && !player.isCreative())) {
			return;
		}

		if (!tag.getString(DIMENSION_TAG).equals(getDimension(world))) {
			if (!world.isRemote) {
				player.sendMessage(new StringTextComponent(TextFormatting.DARK_RED + "Out of range!"), Util.DUMMY_UUID);
			}
			return;
		}

		BlockPos wraithNodePos = new BlockPos(tag.getInt(NODE_X_TAG + getDimension(world)), tag.getInt(NODE_Y_TAG + getDimension(world)), tag.getInt(NODE_Z_TAG + getDimension(world)));
		if (world.getBlockState(wraithNodePos).getBlock() == ModBlocks.WRAITH_NODE.get() && canTeleport(world, wraithNodePos)) {
			teleportPlayer(world, wraithNodePos, player);
			if (!player.isCreative() && !player.world.isRemote) {
				setPearlCount(stack, getPearlCount(stack) - getEnderStaffNodeWarpCost());
			}
			return;
		}

		if (tag.contains(DIMENSION_TAG)) {
			tag.remove(DIMENSION_TAG);
			tag.remove(NODE_X_TAG);
			tag.remove(NODE_Y_TAG);
			tag.remove(NODE_Z_TAG);
			if (!world.isRemote) {
				player.sendMessage(new StringTextComponent(TextFormatting.DARK_RED + "Node doesn't exist!"), Util.DUMMY_UUID);
			} else {
				player.playSound(SoundEvents.ENTITY_ENDERMAN_DEATH, 1.0f, 1.0f);
			}
		}
	}

	private boolean canTeleport(World world, BlockPos pos) {
		BlockPos up = pos.up();
		return world.isAirBlock(up) && world.isAirBlock(up.up());
	}

	private void teleportPlayer(World world, BlockPos pos, PlayerEntity player) {
		player.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() + 0.875, pos.getZ() + 0.5);
		player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
		for (int particles = 0; particles < 2; particles++) {
			world.addParticle(ParticleTypes.PORTAL, player.getPosX(), player.getPosY(), player.getPosZ(), world.rand.nextGaussian(), world.rand.nextGaussian(), world.rand.nextGaussian());
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void addMoreInformation(ItemStack staff, @Nullable World world, List<ITextComponent> tooltip) {
		//added spacing here to make sure the tooltips didn't come out with weird punctuation derps.
		String charge = Integer.toString(getPearlCount(staff, true));
		String phrase = "Currently bound to ";
		String position = "";
		if (staff.getTag() != null && !staff.getTag().getString(DIMENSION_TAG).equals(getDimension(world))) {
			phrase = "Out of range!";
		} else if (staff.getTag() != null && staff.getTag().contains(NODE_X_TAG + getDimension(world)) && staff.getTag().contains(NODE_Y_TAG + getDimension(world)) && staff.getTag().contains(NODE_Z_TAG + getDimension(world))) {
			position = "X: " + staff.getTag().getInt(NODE_X_TAG + getDimension(world)) + " Y: " + staff.getTag().getInt(NODE_Y_TAG + getDimension(world)) + " Z: " + staff.getTag().getInt(NODE_Z_TAG + getDimension(world));
		} else {
			position = "nowhere.";
		}
		LanguageHelper.formatTooltip(getTranslationKey() + ".tooltip2", ImmutableMap.of("phrase", phrase, "position", position, "charge", charge), tooltip);
		if (isEnabled(staff)) {
			LanguageHelper.formatTooltip("tooltip.absorb_active", ImmutableMap.of("item", TextFormatting.GREEN + Items.ENDER_PEARL.getDisplayName(new ItemStack(Items.ENDER_PEARL)).toString()), tooltip);
		}
		LanguageHelper.formatTooltip("tooltip.absorb", null, tooltip);
	}

	@Override
	protected boolean hasMoreInformation(ItemStack stack) {
		return true;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext itemUseContext) {
		ItemStack stack = itemUseContext.getItem();
		World world = itemUseContext.getWorld();
		BlockPos pos = itemUseContext.getPos();

		// if right clicking on a wraith node, bind the eye to that wraith node.
		if ((stack.getTag() == null || !(stack.getTag().contains(DIMENSION_TAG))) && world.getBlockState(pos).getBlock() == ModBlocks.WRAITH_NODE.get()) {
			setWraithNode(stack, pos, getDimension(world));

			PlayerEntity player = itemUseContext.getPlayer();
			if (player != null) {
				player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
			}
			for (int particles = 0; particles < 12; particles++) {
				world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.rand.nextDouble(), pos.getY() + world.rand.nextDouble(), pos.getZ() + world.rand.nextDouble(), world.rand.nextGaussian(), world.rand.nextGaussian(), world.rand.nextGaussian());
			}
			return ActionResultType.SUCCESS;
		} else {
			return ActionResultType.PASS;
		}
	}

	private String getDimension(@Nullable World world) {
		return world != null ? world.getDimensionKey().getRegistryName().toString() : Dimension.OVERWORLD.getRegistryName().toString();
	}

	private void setWraithNode(ItemStack eye, BlockPos pos, String dimension) {
		NBTHelper.putInt(NODE_X_TAG + dimension, eye, pos.getX());
		NBTHelper.putInt(NODE_Y_TAG + dimension, eye, pos.getY());
		NBTHelper.putInt(NODE_Z_TAG + dimension, eye, pos.getZ());
		NBTHelper.putString(DIMENSION_TAG, eye, dimension);
	}

	@Nullable
	@Override
	public CompoundNBT getShareTag(ItemStack staff) {
		CompoundNBT nbt = super.getShareTag(staff);
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		nbt.putInt("count", getPearlCount(staff));

		return nbt;
	}
}
