package xreliquary.blocks.tile;

import lib.enderwizards.sandstone.blocks.tile.TileEntityBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import xreliquary.Reliquary;
import xreliquary.blocks.BlockAlkahestryAltar;
import xreliquary.reference.Names;

public class TileEntityAltar extends TileEntityBase
{
    private int cycleTime;
    private boolean isActive;
    private int redstoneCount;

    public TileEntityAltar(boolean isActive) {
        cycleTime = 0;
        redstoneCount = 0;
        this.isActive = isActive;

        if (this.isActive) {
            startCycle();
        }
    }

    @Override
    public void update() {
        if (this.worldObj.isRemote)
            return;
        if (!isActive)
            return;
        int worldTime = (int) (worldObj.getWorldTime() % 24000);
        if (worldTime >= 12000)
            return;
        if (!worldObj.canSeeSky(getPos().add(0,1,0)))
            return;
        if (cycleTime > 0) {
            cycleTime--;
        } else {
            isActive = false;
            worldObj.setBlockState(getPos().add(0,1,0),Blocks.glowstone.getDefaultState());
            BlockAlkahestryAltar.updateAltarBlockState(isActive(), worldObj, getPos());
        }
    }

    public void startCycle() {
        //grabs the cycle time from the configs
        int defaultCycleTime = Reliquary.CONFIG.getInt(Names.altar, "time_in_minutes") * 60 * 20;
        int maximumVariance = Reliquary.CONFIG.getInt(Names.altar, "maximum_time_variance_in_minutes") * 60 * 20;
        cycleTime = (int) (defaultCycleTime + (double)maximumVariance * worldObj.rand.nextGaussian());
        redstoneCount = 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readFromNBT(par1NBTTagCompound);
        cycleTime = par1NBTTagCompound.getShort("cycleTime");
        redstoneCount = par1NBTTagCompound.getShort("redstoneCount");
        isActive = par1NBTTagCompound.getBoolean("isActive");
    }

    @Override
    public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setShort("cycleTime", (short) cycleTime);
        par1NBTTagCompound.setShort("redstoneCount", (short) redstoneCount);
        par1NBTTagCompound.setBoolean("isActive", isActive);
    }

    public void addRedstone() {
        //TODO:move this logic into the block itself and use blockstate for redstone
        redstoneCount++;
        if (redstoneCount >= getRedstoneCost()) {
            BlockAlkahestryAltar.updateAltarBlockState( isActive(), worldObj, getPos() );
        }
    }

    public static int getRedstoneCost() { return Reliquary.CONFIG.getInt(Names.altar, "redstone_cost"); }

    public int getRedstoneCount() {
        return redstoneCount;
    }

    public boolean isActive() {
        return isActive;
    }
}
