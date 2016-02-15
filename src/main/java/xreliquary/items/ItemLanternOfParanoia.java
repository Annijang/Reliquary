package xreliquary.items;


import lib.enderwizards.sandstone.util.ContentHelper;
import lib.enderwizards.sandstone.util.InventoryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xreliquary.Reliquary;
import xreliquary.init.ModItems;
import xreliquary.reference.Names;
import xreliquary.reference.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xeno on 5/15/14.
 */
public class ItemLanternOfParanoia extends ItemToggleable {

    public ItemLanternOfParanoia() {
        super(Names.lantern_of_paranoia);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(1);
        canRepair = false;
    }

    // so it can be extended by phoenix down
    protected ItemLanternOfParanoia(String name) {
        super(name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    public int getRange() { return Settings.LanternOfParanoia.placementScanRadius; }
    // event driven item, does nothing here.

    // minor jump buff
    @Override
    public void onUpdate(ItemStack stack, World world, Entity e, int i, boolean f) {
        if (!this.isEnabled(stack))
            return;
        if (world.isRemote)
            return;
        if (e instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) e;
            if (e instanceof EntityPlayer) {
                player = (EntityPlayer) e;
            }
            if (player == null)
                return;


            //TODO this is where we'll be placing our algorithm for darkness detection and placing torches!

            int playerX = MathHelper.floor_double(player.posX);
            int playerY = MathHelper.floor_double(player.getEntityBoundingBox().minY);
            int playerZ = MathHelper.floor_double(player.posZ);

            placement: for (int xDiff = -getRange(); xDiff <= getRange(); xDiff++) {
                for (int zDiff = -getRange(); zDiff <= getRange(); zDiff++) {
                    for (int yDiff = getRange() / 2; yDiff >= -getRange() / 2; yDiff--) {
                        int x = playerX + xDiff;
                        int y = playerY + yDiff;
                        int z = playerZ + zDiff;
                        if (!player.worldObj.isAirBlock(new BlockPos(x, y, z)))
                            continue;
                        int lightLevel = player.worldObj.getLightFromNeighbors(new BlockPos(x, y, z));
                        if (lightLevel > Settings.LanternOfParanoia.minLightLevel)
                            continue;
                        if (tryToPlaceTorchAround(stack, x, y, z, player, world))
                            break placement;
                    }
                }
            }

            //attemptPlacementByLookVector(player);

        }
    }
//
//    public void attemptPlacementByLookVector(EntityPlayer player) {
//        MovingObjectPosition mop = getMovingObjectPositionFromPlayer(player.worldObj, player, false);
//        if (!player.canPlayerEdit(x, y, z, side, ist))
//            return;
//
//    }
//
//    //experimenting with a look vector based version of the lantern to avoid some really annoying stuff I can't figure out because I'm dumb.
//    @Override
//    protected MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityPlayer player, boolean weirdBucketBoolean) {
//        float movementCoefficient = 1.0F;
//        float pitchOff = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * movementCoefficient;
//        float yawOff = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * movementCoefficient;
//        double xOff = player.prevPosX + (player.posX - player.prevPosX) * movementCoefficient;
//        double yOff = player.prevPosY + (player.posY - player.prevPosY) * movementCoefficient + 1.62D - player.yOffset;
//        double zOff = player.prevPosZ + (player.posZ - player.prevPosZ) * movementCoefficient;
//        Vec3 playerVector = Vec3.createVectorHelper(xOff, yOff, zOff);
//        float cosTraceYaw = MathHelper.cos(-yawOff * 0.017453292F - (float) Math.PI);
//        float sinTraceYaw = MathHelper.sin(-yawOff * 0.017453292F - (float) Math.PI);
//        float cosTracePitch = -MathHelper.cos(-pitchOff * 0.017453292F);
//        float sinTracePitch = MathHelper.sin(-pitchOff * 0.017453292F);
//        float pythagoraStuff = sinTraceYaw * cosTracePitch;
//        float pythagoraStuff2 = cosTraceYaw * cosTracePitch;
//        double distCoeff = getRange();
//        Vec3 rayTraceVector = playerVector.addVector(pythagoraStuff * distCoeff, sinTracePitch * distCoeff, pythagoraStuff2 * distCoeff);
//        return world.rayTraceBlocks(playerVector, rayTraceVector, weirdBucketBoolean);
//    }
//
    private boolean findAndDrainSojournersStaff(EntityPlayer player) {
        Item staffItem = ModItems.sojournerStaff;
        if (player.capabilities.isCreativeMode)
            return true;
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            if (player.inventory.getStackInSlot(slot) == null)
                continue;
            if (!(staffItem == player.inventory.getStackInSlot(slot).getItem()))
                continue;
            Item torch = ItemBlock.getItemFromBlock(Blocks.torch);
            if (((ItemSojournerStaff)staffItem).removeItemFromInternalStorage(player.inventory.getStackInSlot(slot), torch, 1))
                return true;
        }
        return false;
    }

    public boolean tryToPlaceTorchAround(ItemStack stack, int xO, int yO, int zO, EntityPlayer player, World world) {
        Block var12 = Blocks.torch;

        int x = xO;
        int y = yO;
        int z = zO;

        double playerEyeHeight = player.posY + player.getEyeHeight();

        for (float xOff = -0.2F; xOff <= 0.2F; xOff += 0.4F) {
            for (float yOff = -0.2F; yOff <= 0.2F; yOff += 0.4F) {
                for (float zOff = -0.2F; zOff <= 0.2F; zOff += 0.4F) {

                    Vec3 playerVec = new Vec3(player.posX + xOff, playerEyeHeight + yOff, player.posZ + zOff);
                    Vec3 rayTraceVector = new Vec3((float)x + 0.5D + xOff, (float)y + 0.5D + yOff, (float)z + 0.5D + zOff);

                    MovingObjectPosition mop = world.rayTraceBlocks(playerVec, rayTraceVector, false, false, true);

                    if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        IBlockState blockState = world.getBlockState(mop.getBlockPos());
                        if (blockState.getBlock().getCollisionBoundingBox(world, mop.getBlockPos(), blockState) != null) {
                            if (blockState.getBlock().canCollideCheck(blockState, false))
                                return false;
                        }
                    }
                }
            }
        }



        float xOff = (float)player.posX;
        float zOff = (float)player.posZ;
        float yOff = (float)player.posY;

        if (Blocks.torch.canPlaceBlockAt(world, new BlockPos(x, y, z))) {
            int rotation = ((MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3) + 2) % 4;
            EnumFacing trySide = EnumFacing.DOWN;
            switch (rotation) {
                case (0):
                    trySide = EnumFacing.EAST;
                    break;
                case (1):
                    trySide = EnumFacing.SOUTH;
                    break;
                case (2):
                    trySide = EnumFacing.WEST;
                    break;
                case (3):
                    trySide = EnumFacing.NORTH;
                    break;
            }

            List<EnumFacing> trySides = new ArrayList<EnumFacing>();
            trySides.add(trySide);
            trySides.add(EnumFacing.DOWN);

            //TODO: alright this seems like there's way too much code and logic here for something that always adds all 4 sides of a block
            // once the mod is working this should be reviewed

            EnumFacing[] tryOtherSides = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
            for (EnumFacing tryOtherSide : tryOtherSides) {
                if (trySides.contains(tryOtherSide)) continue;
                trySides.add(tryOtherSide);
            }
            for (EnumFacing side : trySides) {
                if (!world.canBlockBePlaced(Blocks.torch, new BlockPos(x, y, z), false, side, player, stack))
                    continue;
                if (!(InventoryHelper.consumeItem(Blocks.torch, player, 0, 1) || findAndDrainSojournersStaff(player)))
                    continue;
                IBlockState torchBlockState = getTorchSideAttempt(world, new BlockPos(x, y, z), side, player);

                if (placeBlockAt(stack, player, world, new BlockPos(x, y, z),torchBlockState)) {
                    Blocks.torch.onBlockAdded(world, new BlockPos(x, y, z), torchBlockState);
                    double gauss = 0.5D + world.rand.nextFloat() / 2;
                    world.spawnParticle(EnumParticleTypes.SPELL_MOB, x + 0.5D, y + 0.5D, z + 0.5D, gauss, gauss, 0.0F);
                    world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, var12.stepSound.getStepSound(), (var12.stepSound.getVolume() + 1.0F) / 2.0F, var12.stepSound.getFrequency() * 0.8F);
                    return true;
                }
            }
        }
        return false;
    }

    private IBlockState getTorchSideAttempt(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        return Blocks.torch.onBlockPlaced(world, pos, side, pos.getX(), pos.getY(), pos.getZ(), 0, player);
    }

    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,IBlockState torchBlockState) {
        if (!world.setBlockState(pos, torchBlockState, 3))
            return false;

        if (ContentHelper.areBlocksEqual(torchBlockState.getBlock(), Blocks.torch)) {
            Blocks.torch.onNeighborBlockChange(world, pos, torchBlockState, torchBlockState.getBlock());
            Blocks.torch.onBlockPlacedBy(world, pos, torchBlockState, player, stack);
        }

        return true;
    }
}
