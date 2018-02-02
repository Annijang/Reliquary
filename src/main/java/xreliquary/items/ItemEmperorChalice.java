package xreliquary.items;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xreliquary.Reliquary;
import xreliquary.items.util.fluid.FluidHandlerEmperorChalice;
import xreliquary.reference.Names;
import xreliquary.reference.Settings;
import xreliquary.util.LanguageHelper;
import xreliquary.util.RegistryHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemEmperorChalice extends ItemToggleable {

	public ItemEmperorChalice() {
		super(Names.Items.EMPEROR_CHALICE);
		this.setCreativeTab(Reliquary.CREATIVE_TAB);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		canRepair = false;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void addMoreInformation(ItemStack stack, @Nullable World world, List<String> tooltip) {
		LanguageHelper.formatTooltip(getUnlocalizedNameInefficiently(stack) + ".tooltip2", tooltip);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
		return 16;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidHandlerEmperorChalice(stack);
	}

	@Nonnull
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.EPIC;
	}

	@Nonnull
	@Override
	public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World world, EntityLivingBase entityLiving) {
		if(world.isRemote)
			return stack;

		if(!(entityLiving instanceof EntityPlayer))
			return stack;

		EntityPlayer player = (EntityPlayer) entityLiving;

		int multiplier = Settings.Items.EmperorChalice.hungerSatiationMultiplier;
		player.getFoodStats().addStats(1, (float) (multiplier / 2));
		player.attackEntityFrom(DamageSource.DROWN, multiplier);
		return stack;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
		ItemStack ist = player.getHeldItem(hand);
		if(player.isSneaking())
			return super.onItemRightClick(world, player, hand);
		boolean isInDrainMode = this.isEnabled(ist);
		RayTraceResult result = this.rayTrace(world, player, isInDrainMode);

		//noinspection ConstantConditions
		if(result == null) {
			if(!this.isEnabled(ist)) {
				player.setActiveHand(hand);
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, ist);
		} else if(result.typeOfHit == RayTraceResult.Type.BLOCK) {

			if(!world.isBlockModifiable(player, result.getBlockPos()))
				return new ActionResult<>(EnumActionResult.FAIL, ist);

			if(!player.canPlayerEdit(result.getBlockPos(), result.sideHit, ist))
				return new ActionResult<>(EnumActionResult.FAIL, ist);

			if(!this.isEnabled(ist)) {
				BlockPos waterPlacementPos = result.getBlockPos().offset(result.sideHit);

				if(!player.canPlayerEdit(waterPlacementPos, result.sideHit, ist))
					return new ActionResult<>(EnumActionResult.FAIL, ist);

				if(this.tryPlaceContainedLiquid(world, ist, waterPlacementPos))
					return new ActionResult<>(EnumActionResult.SUCCESS, ist);

			} else {
				String ident = RegistryHelper.getBlockRegistryName(world.getBlockState(result.getBlockPos()).getBlock());
				//noinspection ConstantConditions
				if((ident.equals(RegistryHelper.getBlockRegistryName(Blocks.FLOWING_WATER)) || ident.equals(RegistryHelper.getBlockRegistryName(Blocks.WATER))) && world.getBlockState(result.getBlockPos()).getValue(BlockLiquid.LEVEL) == 0) {
					world.setBlockState(result.getBlockPos(), Blocks.AIR.getDefaultState());

					return new ActionResult<>(EnumActionResult.SUCCESS, ist);
				}
			}
		}

		return new ActionResult<>(EnumActionResult.PASS, ist);
	}

	@SubscribeEvent
	public void onBlockRightClick(PlayerInteractEvent.RightClickBlock evt) {
		if (evt.getItemStack().getItem() == this) {
			World world = evt.getWorld();
			IBlockState state = world.getBlockState(evt.getPos());
			if (state.getBlock() == Blocks.CAULDRON) {
				if (!isEnabled(evt.getItemStack()) && state.getValue(BlockCauldron.LEVEL) == 0) {
					Blocks.CAULDRON.setWaterLevel(world, evt.getPos(), state, 3);
					evt.setUseItem(Event.Result.DENY);
					evt.setCanceled(true);
					evt.setCancellationResult(EnumActionResult.SUCCESS);
				} else if (isEnabled(evt.getItemStack()) && state.getValue(BlockCauldron.LEVEL) == 3) {
					Blocks.CAULDRON.setWaterLevel(world, evt.getPos(), state, 0);
					evt.setUseItem(Event.Result.DENY);
					evt.setCanceled(true);
					evt.setCancellationResult(EnumActionResult.SUCCESS);
				}
			}
		}
	}

	private boolean tryPlaceContainedLiquid(World world, @Nonnull ItemStack stack, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		Material material = blockState.getMaterial();

		if(this.isEnabled(stack))
			return false;
		if(!world.isAirBlock(pos) && material.isSolid())
			return false;
		else {
			if(world.provider.doesWaterVaporize()) {
				world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

				for(int var11 = 0; var11 < 8; ++var11) {
					world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
				}
			} else {
				world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState(), 3);
			}

			return true;
		}
	}
}
