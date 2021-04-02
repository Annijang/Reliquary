package xreliquary.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class WraithNodeBlock extends Block {
	private static final VoxelShape SHAPE = makeCuboidShape(2, 0, 2, 14, 12, 14);

	public WraithNodeBlock() {
		super(Properties.create(Material.ROCK).hardnessAndResistance(1.5F, 5.0F).notSolid());
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}
}
