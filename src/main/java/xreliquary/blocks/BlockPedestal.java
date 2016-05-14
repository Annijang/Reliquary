package xreliquary.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import xreliquary.Reliquary;
import xreliquary.blocks.tile.TileEntityPedestal;
import xreliquary.reference.Names;
import xreliquary.util.InventoryHelper;
import xreliquary.util.pedestal.PedestalRegistry;

import java.util.List;
import java.util.Random;

public class BlockPedestal extends BlockBase {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool ENABLED = PropertyBool.create("enabled");
	private static final AxisAlignedBB PEDESTAL_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.6875D, 0.875D);

	public BlockPedestal() {
		super(Material.rock, Names.pedestal);
		this.setUnlocalizedName(Names.pedestal);
		this.setCreativeTab(Reliquary.CREATIVE_TAB);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		List<BlockPos> pedestalPositions = PedestalRegistry.getPositionsInRange(worldIn.provider.getDimension(), pos, 160);

		for(BlockPos pedestalPosition : pedestalPositions) {
			TileEntity te = worldIn.getTileEntity(pedestalPosition);
			if(te != null && te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).updateRedstone();
			}
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getHorizontal(meta);

		return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(ENABLED, (meta & 4) != 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING, ENABLED});
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = 0;

		i |= state.getValue(FACING).getHorizontalIndex();

		if(state.getValue(ENABLED)) {
			i |= 4;
		}

		return i;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);

		((TileEntityPedestal) worldIn.getTileEntity(pos)).neighborUpdate();
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing()).withProperty(ENABLED, false);
	}

	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {

		if(state.getValue(ENABLED) && rand.nextInt(3) == 1) {
			EnumFacing enumfacing = state.getValue(FACING);
			double xMiddle = (double) pos.getX() + 0.5D;
			double y = (double) pos.getY() + 4.0D / 16.0D + rand.nextDouble() * 4.0D / 16.0D;
			double zMiddle = (double) pos.getZ() + 0.5D;
			double sideOffset = 0.27D;
			double randomOffset = rand.nextDouble() * 0.3D - 0.15D;

			switch(enumfacing) {
				case WEST:
					world.spawnParticle(EnumParticleTypes.REDSTONE, xMiddle + sideOffset, y, zMiddle + randomOffset, 0.0D, 0.0D, 0.0D, new int[0]);
					break;
				case EAST:
					world.spawnParticle(EnumParticleTypes.REDSTONE, xMiddle - sideOffset, y, zMiddle + randomOffset, 0.0D, 0.0D, 0.0D, new int[0]);
					break;
				case NORTH:
					world.spawnParticle(EnumParticleTypes.REDSTONE, xMiddle + randomOffset, y, zMiddle + sideOffset, 0.0D, 0.0D, 0.0D, new int[0]);
					break;
				case SOUTH:
					world.spawnParticle(EnumParticleTypes.REDSTONE, xMiddle + randomOffset, y, zMiddle - sideOffset, 0.0D, 0.0D, 0.0D, new int[0]);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float xOff, float yOff, float zOff) {
		TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(pos);
		if(pedestal == null)
			return false;

		if(world.isRemote)
			return player.getHeldItem(hand) != null || player.isSneaking();

		if(heldItem == null) {
			if(!player.isSneaking() && hand == EnumHand.MAIN_HAND && switchClicked(side, xOff, yOff, zOff)) {
				pedestal.toggleSwitch();
				return true;
			}

			if(player.isSneaking()) {
				pedestal.removeLastPedestalStack();
				return true;
			} else {
				return false;
			}
		} else {
			return InventoryHelper.tryAddingPlayerCurrentItem(player, pedestal, EnumHand.MAIN_HAND);
		}
	}

	private boolean switchClicked(EnumFacing side, float xOff, float yOff, float zOff) {
		if(yOff < 0.3 || yOff > 0.65)
			return false;
		if(side == EnumFacing.NORTH && (xOff < 0.35 || xOff > 0.65 || zOff != 0.125))
			return false;
		if(side == EnumFacing.SOUTH && (xOff < 0.35 || xOff > 0.65 || zOff != 0.875))
			return false;
		if(side == EnumFacing.WEST && (zOff < 0.35 || zOff > 0.65 || xOff != 0.125))
			return false;
		if(side == EnumFacing.EAST && (zOff < 0.35 || zOff > 0.65 || xOff != 0.875))
			return false;

		return true;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityPedestal();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return PEDESTAL_AABB;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityPedestal pedestal = (TileEntityPedestal) world.getTileEntity(pos);

		if(pedestal != null) {
			pedestal.dropPedestalInventory();
		}

		PedestalRegistry.unregisterPosition(world.provider.getDimension(), pos);

		pedestal.removeRedstoneItems();

		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public void setEnabled(World world, BlockPos pos, boolean enabled) {
		IBlockState state = world.getBlockState(pos);
		if(state.getValue(BlockPedestal.ENABLED) != enabled) {
			state = state.withProperty(BlockPedestal.ENABLED, enabled);

			world.setBlockState(pos, state);
		}
	}
}
