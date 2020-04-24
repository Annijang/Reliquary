package xreliquary.pedestal.wrappers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import xreliquary.api.IPedestal;
import xreliquary.api.IPedestalActionItemWrapper;
import xreliquary.blocks.FertileLilyPadBlock;
import xreliquary.init.ModItems;
import xreliquary.items.HarvestRodItem;
import xreliquary.items.util.HarvestRodItemStackHandler;
import xreliquary.reference.Settings;
import xreliquary.util.ItemHelper;
import xreliquary.util.NBTHelper;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class PedestalHarvestRodWrapper implements IPedestalActionItemWrapper {

	private static final int NO_JOB_COOL_DOWN_CYCLES = 10;
	private static final HarvestRodItem harvestRod = ModItems.HARVEST_ROD;

	private int hoeCoolDown = 0;
	private int plantCoolDown = 0;
	private int boneMealCoolDown = 0;
	private int breakCoolDown = 0;

	private final Queue<BlockPos> queueToHoe;
	private final Queue<BlockPos> queueToPlant;
	private final Queue<BlockPos> queueToBoneMeal;
	private final Queue<BlockPos> queueToBreak;

	public PedestalHarvestRodWrapper() {
		queueToHoe = new ArrayDeque<>();
		queueToPlant = new ArrayDeque<>();
		queueToBoneMeal = new ArrayDeque<>();
		queueToBreak = new ArrayDeque<>();
	}

	@Override
	public void update(ItemStack stack, IPedestal pedestal) {
		World world = pedestal.getTheWorld();
		BlockPos pos = pedestal.getBlockPos();
		PlayerEntity player = pedestal.getFakePlayer();
		int range = Settings.COMMON.items.harvestRod.pedestalRange.get();
		int cooldown = Settings.COMMON.items.harvestRod.pedestalCooldown.get();

		hoeLand(world, player, pos, range);

		plantSeeds(world, player, pos, stack, range);

		boneMealCrops(world, pos, stack, range);

		breakCrops(world, player, pos, stack, range);

		pedestal.setActionCoolDown(cooldown);
	}

	@Override
	public void onRemoved(ItemStack stack, IPedestal pedestal) {
		NBTHelper.updateContainedStack(stack, (short) HarvestRodItemStackHandler.BONEMEAL_SLOT, ItemStack.EMPTY, harvestRod.getBoneMealCount(stack));
		for(short slot=1; slot < harvestRod.getCountPlantable(stack) + 1; slot++) {
			NBTHelper.updateContainedStack(stack, slot, harvestRod.getPlantableInSlot(stack, slot), harvestRod.getPlantableQuantity(stack, slot));
		}
	}

	@Override
	public void stop(ItemStack stack, IPedestal pedestal) {
		//noop
	}

	private void breakCrops(World world, PlayerEntity player, BlockPos pos, ItemStack stack, int range) {
		if (breakCoolDown > 0) {
			breakCoolDown--;
		} else {
			if (!breakNext(world, player, pos, stack, range)) {
				breakCoolDown = NO_JOB_COOL_DOWN_CYCLES;
			}
		}
	}

	private boolean breakNext(World world, PlayerEntity player, BlockPos pos, ItemStack stack, int range) {
		return getNextBlockToBreak(world, pos, range).map(nextBlockToBreak -> {
			doHarvestBlockBreak(world, player, stack, nextBlockToBreak);
			return true;
		}).orElse(false);
	}

	private void doHarvestBlockBreak(World world, PlayerEntity player, ItemStack stack, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld) world, pos, null, player, stack);
		for (ItemStack drop : drops) {
			float f = 0.7F;
			double d = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
			double d1 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
			double d2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
			ItemEntity entityitem = new ItemEntity(world, (double) pos.getX() + d, (double) pos.getY() + d1, (double) pos.getZ() + d2, drop);
			entityitem.setPickupDelay(10);
			world.addEntity(entityitem);
		}

		world.setBlockState(pos, Blocks.AIR.getDefaultState());
	}

	private void boneMealCrops(World world, BlockPos pos, ItemStack stack, int range) {

		if (boneMealCoolDown > 0) {
			boneMealCoolDown--;
		} else {
			if (harvestRod.getBoneMealCount(stack) >= (harvestRod.getBonemealCost()) && boneMealNext(world, pos, stack, range)) {
				return;
			}
			boneMealCoolDown = NO_JOB_COOL_DOWN_CYCLES;
		}
	}

	private boolean boneMealNext(World world, BlockPos pos, ItemStack stack, int range) {
		return getNextBlockToBoneMeal(world, pos, range).map(blockToBoneMeal -> {
			boneMealBlock(stack, world, blockToBoneMeal);
			return true;
		}).orElse(false);
	}

	private void boneMealBlock(ItemStack stack, World world, BlockPos pos) {
		ItemStack fakeItemStack = new ItemStack(Items.BONE_MEAL);

		boolean boneMealUsed = false;
		for (int repeatedUses = 0; repeatedUses <= harvestRod.getLuckRolls(); repeatedUses++) {
			if ((repeatedUses == 0 || world.rand.nextInt(100) <= harvestRod.getLuckPercent()) && BoneMealItem.applyBonemeal(fakeItemStack, world, pos)) {
				boneMealUsed = true;
			}
		}
		if (boneMealUsed) {
			world.playEvent(2005, pos, 0);
			harvestRod.setBoneMealCount(stack, harvestRod.getBoneMealCount(stack) - harvestRod.getBonemealCost());
		}
	}

	private void plantSeeds(World world, PlayerEntity player, BlockPos pos, ItemStack stack, int range) {

		if (plantCoolDown > 0) {
			plantCoolDown--;
		} else {
			byte plantableSlot = 1;

			if (harvestRod.getCountPlantable(stack) > 0) {
				int quantity = harvestRod.getPlantableQuantity(stack, plantableSlot);

				if (quantity > 0 && plantNext(world, player, pos, stack, range, plantableSlot)) {
					return;
				}
			}
			plantCoolDown = NO_JOB_COOL_DOWN_CYCLES;
		}
	}

	private boolean plantNext(World world, PlayerEntity player, BlockPos pos, ItemStack stack, int range, byte plantableSlot) {
		return getNextBlockToPlantOn(world, pos, range, (IPlantable) ((BlockItem) harvestRod.getPlantableInSlot(stack, plantableSlot).getItem()).getBlock()).map(blockToPlantOn -> {
			plantItem(player, blockToPlantOn, stack, plantableSlot);
			return true;
		}).orElse(false);
	}

	private void plantItem(PlayerEntity player, BlockPos pos, ItemStack stack, byte idx) {
		ItemStack fakePlantableStack = harvestRod.getPlantableInSlot(stack, idx).copy();
		fakePlantableStack.setCount(1);
		player.setHeldItem(Hand.MAIN_HAND, fakePlantableStack);

		if (fakePlantableStack.onItemUse(ItemHelper.getItemUseContext(pos, player)) == ActionResultType.SUCCESS) {
			harvestRod.setPlantableQuantity(stack, idx, harvestRod.getPlantableQuantity(stack, idx) - 1);
		}
	}

	private void hoeLand(World world, PlayerEntity player, BlockPos pos, int range) {
		if (hoeCoolDown > 0) {
			hoeCoolDown--;
		} else {
			if (!hoeNext(world, player, pos, range)) {
				hoeCoolDown = NO_JOB_COOL_DOWN_CYCLES;
			}
		}
	}

	private boolean hoeNext(World world, PlayerEntity player, BlockPos pos, int range) {
		return getNextBlockToHoe(world, pos, range).map(blockToHoe -> {
			ItemStack fakeHoe = new ItemStack(Items.WOODEN_HOE);
			player.setHeldItem(Hand.MAIN_HAND, fakeHoe);
			Items.WOODEN_HOE.onItemUse(ItemHelper.getItemUseContext(blockToHoe, player));
			return true;
		}).orElse(false);
	}

	private Optional<BlockPos> getNextBlockToBreak(World world, BlockPos pos, int range) {
		if (queueToBreak.isEmpty()) {
			fillQueueToBreak(world, pos, range);
		}
		return Optional.ofNullable(queueToBreak.poll());

	}

	private void fillQueueToBreak(World world, BlockPos pos, int range) {
		BlockPos.getAllInBox(pos.add(-range, -range, -range), pos.add(range, range, range)).forEach(
				p -> {
					BlockPos currentPos = p.toImmutable();
					BlockState state = world.getBlockState(currentPos);
					Block block = state.getBlock();
					if (block instanceof IPlantable || block == Blocks.MELON || block == Blocks.PUMPKIN) {
						if (block instanceof FertileLilyPadBlock || block == Blocks.PUMPKIN_STEM || block == Blocks.MELON_STEM
								|| block instanceof CropsBlock && ((CropsBlock) block).canGrow(world, currentPos, state, false)
								|| block instanceof NetherWartBlock && state.get(NetherWartBlock.AGE) < 3) {
							return;
						}

						queueToBreak.add(currentPos);
					}
				});
	}

	private Optional<BlockPos> getNextBlockToHoe(World world, BlockPos pos, int range) {
		if (queueToHoe.isEmpty()) {
			fillQueueToHoe(world, pos, range);
		}

		return Optional.ofNullable(queueToHoe.poll());
	}

	private void fillQueueToHoe(World world, BlockPos pos, int range) {
		queueToHoe.clear();
		BlockPos.getAllInBox(pos.add(-range, -range, -range), pos.add(range, range, range)).forEach(
				p -> {
					BlockPos currentPos = p.toImmutable();
					BlockState blockState = world.getBlockState(currentPos);
					Block block = blockState.getBlock();

					if (world.isAirBlock(currentPos.up()) && (block == Blocks.GRASS_BLOCK || block == Blocks.GRASS_PATH || block == Blocks.DIRT || block == Blocks.COARSE_DIRT)) {
						queueToHoe.add(currentPos);
					}
				}
		);
	}

	private Optional<BlockPos> getNextBlockToPlantOn(World world, BlockPos pos, int range, IPlantable plantable) {
		if (queueToPlant.isEmpty()) {
			fillQueueToPlant(world, pos, range, plantable);
		}

		return Optional.ofNullable(queueToPlant.poll());
	}

	private void fillQueueToPlant(World world, BlockPos pos, int range, IPlantable plantable) {
		queueToPlant.clear();

		boolean checkerboard = false;
		boolean bothOddOrEven = false;

		if (plantable == Items.PUMPKIN_SEEDS || plantable == Items.MELON_SEEDS) {
			checkerboard = true;
			boolean xEven = pos.getX() % 2 == 0;
			boolean zEven = pos.getZ() % 2 == 0;
			bothOddOrEven = xEven == zEven;
		}

		boolean finalCheckerboard = checkerboard;
		boolean finalBothOddOrEven = bothOddOrEven;
		BlockPos.getAllInBox(pos.add(-range, -range, -range), pos.add(range, range, range)).forEach(
				p -> {
					BlockPos currentPos = p.toImmutable();
					BlockState blockState = world.getBlockState(currentPos);
					if ((!finalCheckerboard || (finalBothOddOrEven == ((currentPos.getX() % 2 == 0) == (currentPos.getZ() % 2 == 0)))) && blockState.getBlock().canSustainPlant(blockState, world, pos, Direction.UP, plantable) && world.isAirBlock(currentPos.up())) {
						queueToPlant.add(currentPos);
					}
				});

	}

	private Optional<BlockPos> getNextBlockToBoneMeal(World world, BlockPos pos, int range) {
		if (queueToBoneMeal.isEmpty()) {
			fillQueueToBoneMeal(world, pos, range);
		}

		return Optional.ofNullable(queueToBoneMeal.poll());
	}

	private void fillQueueToBoneMeal(World world, BlockPos pos, int range) {
		queueToBoneMeal.clear();
		BlockPos.getAllInBox(pos.add(-range, -range, -range), pos.add(range, range, range)).forEach(
				p -> {
					BlockPos currentPos = p.toImmutable();
					BlockState blockState = world.getBlockState(currentPos);
					if (blockState.getBlock() != Blocks.GRASS_BLOCK && blockState.getBlock() instanceof IGrowable && ((IGrowable) blockState.getBlock()).canGrow(world, currentPos, blockState, world.isRemote)) {
						queueToBoneMeal.add(currentPos);
					}
				});
	}
}
