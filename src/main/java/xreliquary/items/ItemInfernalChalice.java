package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import xreliquary.Reliquary;
import xreliquary.handler.CommonEventHandler;
import xreliquary.handler.IPlayerHurtHandler;
import xreliquary.init.ModItems;
import xreliquary.items.util.fluid.FluidHandlerInfernalChalice;
import xreliquary.reference.Names;
import xreliquary.reference.Settings;
import xreliquary.util.InventoryHelper;
import xreliquary.util.LanguageHelper;
import xreliquary.util.NBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemInfernalChalice extends ItemToggleable {
	public ItemInfernalChalice() {
		super(Names.Items.INFERNAL_CHALICE);
		this.setCreativeTab(Reliquary.CREATIVE_TAB);
		this.setMaxStackSize(1);
		canRepair = false;

		CommonEventHandler.registerPlayerHurtHandler(new IPlayerHurtHandler() {
			@Override
			public boolean canApply(EntityPlayer player, LivingAttackEvent event) {
				return (event.getSource() == DamageSource.LAVA || event.getSource() == DamageSource.ON_FIRE || event.getSource() == DamageSource.IN_FIRE)
						&& player.getFoodStats().getFoodLevel() > 0
						&& InventoryHelper.playerHasItem(player, ModItems.infernalChalice);
			}

			@Override
			public boolean apply(EntityPlayer player, LivingAttackEvent event) {
				player.addExhaustion(event.getAmount() * ((float) Settings.Items.InfernalChalice.hungerCostPercent / 100F));
				return true;
			}

			@Override
			public Priority getPriority() {
				return Priority.HIGH;
			}
		});
	}

	@Override
	protected void addMoreInformation(ItemStack chalice, @Nullable World world, List<String> tooltip) {
		LanguageHelper.formatTooltip(getUnlocalizedNameInefficiently(chalice) + ".tooltip2",
				ImmutableMap.of("amount", String.valueOf(NBTHelper.getInteger("fluidStacks", chalice))), tooltip);

		if(this.isEnabled(chalice)) {
			LanguageHelper.formatTooltip("tooltip.place", tooltip);
		} else {
			LanguageHelper.formatTooltip("tooltip.drain", tooltip);
		}
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking()) {
			return super.onItemRightClick(world, player, hand);
		}

		boolean isInDrainMode = this.isEnabled(stack);
		RayTraceResult result = this.rayTrace(world, player, isInDrainMode);

		//noinspection ConstantConditions
		if(result == null) {
			return new ActionResult<>(EnumActionResult.PASS, stack);
		} else {
			if(result.typeOfHit == RayTraceResult.Type.BLOCK) {

				if(!world.isBlockModifiable(player, result.getBlockPos()))
					return new ActionResult<>(EnumActionResult.PASS, stack);

				if(!player.canPlayerEdit(result.getBlockPos(), result.sideHit, stack))
					return new ActionResult<>(EnumActionResult.PASS, stack);

				IFluidHandler fluidHandler = getFluidHandler(stack);
				if(fluidHandler == null)
					return new ActionResult<>(EnumActionResult.FAIL, stack);

				IBlockState blockState = world.getBlockState(result.getBlockPos());
				if(this.isEnabled(stack) && (blockState.getBlock() == Blocks.FLOWING_LAVA || blockState.getBlock() == Blocks.LAVA) && blockState.getValue(BlockLiquid.LEVEL) == 0 && fluidHandler.fill(new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), false) == Fluid.BUCKET_VOLUME) {
					world.setBlockState(result.getBlockPos(), Blocks.AIR.getDefaultState());
					fluidHandler.fill(new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), true);
					return new ActionResult<>(EnumActionResult.SUCCESS, stack);
				}

				if(!this.isEnabled(stack)) {
					FluidStack fluidDrained = fluidHandler.drain(new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), false);
					if(player.capabilities.isCreativeMode || (fluidDrained != null && fluidDrained.amount == Fluid.BUCKET_VOLUME)) {
						BlockPos adjustedPos = result.getBlockPos().offset(result.sideHit);

						if(!player.canPlayerEdit(adjustedPos, result.sideHit, stack))
							return new ActionResult<>(EnumActionResult.PASS, stack);

						if(this.tryPlaceContainedLiquid(world, adjustedPos) && !player.capabilities.isCreativeMode) {
							fluidHandler.drain(new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME), true);
							return new ActionResult<>(EnumActionResult.SUCCESS, stack);
						}
					}
				}
			}

			return new ActionResult<>(EnumActionResult.PASS, stack);
		}
	}

	private IFluidHandler getFluidHandler(ItemStack stack) {
		if(!stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
			return null;

		return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
	}

	private boolean tryPlaceContainedLiquid(World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		Material material = blockState.getMaterial();
		if(!world.isAirBlock(pos) && material.isSolid())
			return false;
		else {
			world.setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState(), 3);
			return true;
		}
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidHandlerInfernalChalice(stack);
	}
}
