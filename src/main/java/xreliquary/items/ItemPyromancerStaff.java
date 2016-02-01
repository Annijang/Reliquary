package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import lib.enderwizards.sandstone.init.ContentInit;
import lib.enderwizards.sandstone.items.ItemToggleable;
import lib.enderwizards.sandstone.util.ContentHelper;
import lib.enderwizards.sandstone.util.InventoryHelper;
import lib.enderwizards.sandstone.util.LanguageHelper;
import lib.enderwizards.sandstone.util.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import xreliquary.Reliquary;
import xreliquary.reference.Names;
import xreliquary.reference.Settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Xeno on 10/11/2014.
 */
@ContentInit
public class ItemPyromancerStaff extends ItemToggleable {
    public ItemPyromancerStaff() {
        super(Names.pyromancer_staff);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(1);
        canRepair = false;
    }

    @Override
    public void onUpdate(ItemStack ist, World world, Entity e, int i, boolean f) {
        if (!(e instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) e;

        doFireballAbsorbEffect(ist, player);

        if (!this.isEnabled(ist))
            doExtinguishEffect(player);
        else
            scanForFireChargeAndBlazePowder(ist, player);
    }

    @Override
    public void addInformation(ItemStack ist, EntityPlayer player, List list, boolean par4) {
        //maps the contents of the Pyromancer's staff to a tooltip, so the player can review the torches stored within.
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
            return;
        String charges = "0";
        String blaze = "0";
        NBTTagCompound tagCompound = NBTHelper.getTag(ist);
        if (tagCompound != null) {
            NBTTagList tagList = tagCompound.getTagList("Items", 10);
            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
                String itemName = tagItemData.getString("Name");
                Item containedItem = Reliquary.CONTENT.getItem(itemName);
                int quantity = tagItemData.getInteger("Quantity");

                if (containedItem == Items.blaze_powder) {
                    blaze = Integer.toString(quantity);
                } else if (containedItem == Items.fire_charge) {
                    charges = Integer.toString(quantity);
                }
            }
        }
        this.formatTooltip(ImmutableMap.of("charges", charges, "blaze", blaze), ist, list);
        if(this.isEnabled(ist))
            LanguageHelper.formatTooltip("tooltip.absorb_active", ImmutableMap.of("item", EnumChatFormatting.RED + Items.blaze_powder.getItemStackDisplayName(new ItemStack(Items.blaze_powder)) + EnumChatFormatting.WHITE + " & " + EnumChatFormatting.RED + Items.fire_charge.getItemStackDisplayName(new ItemStack(Items.fire_charge))), ist, list);

        LanguageHelper.formatTooltip("tooltip.absorb", null, ist, list);
    }


    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 11;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack ist) {
        return EnumAction.BLOCK;
    }

    public String getMode(ItemStack ist) {
        if (NBTHelper.getString("mode", ist).equals("")) {
            setMode(ist, "blaze");
        }
        return NBTHelper.getString("mode", ist);
    }

    public void setMode(ItemStack ist, String s) {
        NBTHelper.setString("mode", ist, s);
    }

    public void cycleMode(ItemStack ist) {
        if (getMode(ist).equals("blaze"))
            setMode(ist, "charge");
        else if (getMode(ist).equals("charge"))
            setMode(ist, "eruption");
        else if (getMode(ist).equals("eruption"))
            setMode(ist, "flint_and_steel");
        else
            setMode(ist, "blaze");
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack ist) {
        if (entityLiving.worldObj.isRemote)
            return true;
        if (!(entityLiving instanceof EntityPlayer))
            return true;
        EntityPlayer player = (EntityPlayer)entityLiving;
        if (player.isSneaking()) {
            cycleMode(ist);
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack ist, World world, EntityPlayer player) {
        if (player.isSneaking())
            super.onItemRightClick(ist, world, player);
        else {
            if (getMode(ist).equals("blaze")) {
                if (player.isSwingInProgress)
                    return ist;
                player.swingItem();
                Vec3 lookVec = player.getLookVec();
                //blaze fireball!
                if (removeItemFromInternalStorage(ist, Items.blaze_powder, getBlazePowderCost(), player.worldObj.isRemote)) {
                    player.worldObj.playAuxSFXAtEntity(player, 1009, new BlockPos((int)player.posX, (int)player.posY, (int)player.posZ), 0);
                    EntitySmallFireball fireball = new EntitySmallFireball(player.worldObj, player, lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
                    fireball.accelerationX = lookVec.xCoord;
                    fireball.accelerationY = lookVec.yCoord;
                    fireball.accelerationZ = lookVec.zCoord;
                    fireball.posX += lookVec.xCoord;
                    fireball.posY += lookVec.yCoord;
                    fireball.posZ += lookVec.zCoord;
                    fireball.posY = player.posY + player.getEyeHeight();
                    player.worldObj.spawnEntityInWorld(fireball);
                }
            } else if (getMode(ist).equals("charge")) {
                if (player.isSwingInProgress)
                    return ist;
                player.swingItem();
                Vec3 lookVec = player.getLookVec();
                //ghast fireball!
                if (removeItemFromInternalStorage(ist, Items.fire_charge, getFireChargeCost(), player.worldObj.isRemote)) {
                    player.worldObj.playAuxSFXAtEntity(player, 1008, new BlockPos((int)player.posX, (int)player.posY, (int)player.posZ), 0);
                    EntityLargeFireball fireball = new EntityLargeFireball(player.worldObj, player, lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
                    fireball.accelerationX = lookVec.xCoord;
                    fireball.accelerationY = lookVec.yCoord;
                    fireball.accelerationZ = lookVec.zCoord;
                    fireball.posX += lookVec.xCoord;
                    fireball.posY += lookVec.yCoord;
                    fireball.posZ += lookVec.zCoord;
                    fireball.posY = player.posY + player.getEyeHeight();
                    player.worldObj.spawnEntityInWorld(fireball);

                }
            } else
                player.setItemInUse(ist, this.getMaxItemUseDuration(ist));
        }
        return ist;
    }

    //a longer ranged version of "getMovingObjectPositionFromPlayer" basically
    public MovingObjectPosition getEruptionBlockTarget(World world, EntityPlayer player) {
        float f = 1.0F;
        float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
        float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
        double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)f;
        double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)f + (double)(world.isRemote ? player.getEyeHeight() - player.getDefaultEyeHeight() : player.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
        double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)f;
        Vec3 vec3 = new Vec3(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = 12.0D;
        Vec3 vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
        return world.rayTraceBlocks(vec3, vec31, true, false, false);
    }

    @Override
    public void onUsingTick(ItemStack ist, EntityPlayer player, int count) {
        //mop call and fakes onItemUse, getting read to do the eruption effect. If the item is enabled, it just sets a bunch of fires!
        MovingObjectPosition mop = this.getEruptionBlockTarget(player.worldObj, player);

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (getMode(ist).equals("eruption")) {
                count -= 1;
                count = getMaxItemUseDuration(ist) - count;

                doEruptionAuxEffects(player, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), 5D);
                if (count % 10 == 0) {
                    if (removeItemFromInternalStorage(ist, Items.blaze_powder, getBlazePowderCost(), player.worldObj.isRemote)) {
                        doEruptionEffect(player, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), 5D);
                    }
                }
            }
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing sideHit, float hitX, float hitY, float hitZ)
    {
        if (getMode(stack).equals("flint_and_steel")) {
            BlockPos placeFireAt = pos.offset(sideHit);
            if (!player.canPlayerEdit(placeFireAt, sideHit, stack)) {
                return false;
            } else {
                if (world.isAirBlock(placeFireAt)) {
                    world.playSoundEffect((double) placeFireAt.getX() + 0.5D, (double) placeFireAt.getY() + 0.5D, (double) placeFireAt.getZ() + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                    world.setBlockState(placeFireAt, Blocks.fire.getDefaultState());
                }
                return false;
            }
        }
        return false;
    }

    public void doEruptionAuxEffects(EntityPlayer player, int x, int y, int z, double areaCoefficient) {
        double soundX = x;
        double soundY = y;
        double soundZ = z;
        player.worldObj.playSound(soundX + 0.5D, soundY + 0.5D, soundZ + 0.5D, "mob.ghast.fireball", 0.2F, 0.03F + (0.07F * itemRand.nextFloat()), false);

        for (int particleCount = 0; particleCount < 2; ++particleCount) {
            double randX = (x + 0.5D) + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient;
            double randZ = (z + 0.5D) + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient;
            if (Math.abs(randX - (x + 0.5D)) >= 4.0D && Math.abs(randZ - (z + 0.5D)) >= 4.0D)
                continue;
            player.worldObj.spawnParticle(EnumParticleTypes.LAVA, randX, y + 1D, randZ, 0D,0D,0D);
        }
        for (int particleCount = 0; particleCount < 4; ++particleCount) {
            double randX = x + 0.5D + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient / 2D;
            double randZ = z + 0.5D + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient / 2D;
            if (Math.abs(randX - (x + 0.5D)) >= 4.0D && Math.abs(randZ - (z + 0.5D)) >= 4.0D)
                continue;
            player.worldObj.spawnParticle(EnumParticleTypes.LAVA, randX, y + 1D, randZ, 0D,0D,0D);
        }
        for (int particleCount = 0; particleCount < 6; ++particleCount) {
            double randX = x + 0.5D + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient;
            double randZ = z + 0.5D + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient;
            if (Math.abs(randX - (x + 0.5D)) >= 4.0D && Math.abs(randZ - (z + 0.5D)) >= 4.0D)
                continue;
            player.worldObj.spawnParticle(EnumParticleTypes.FLAME, randX, y + 1D, randZ, player.worldObj.rand.nextGaussian() * 0.2D, player.worldObj.rand.nextGaussian() * 0.2D, player.worldObj.rand.nextGaussian() * 0.2D);
        }
        for (int particleCount = 0; particleCount < 8; ++particleCount) {
            double randX = x + 0.5D + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient / 2D;
            double randZ = z + 0.5D + (player.worldObj.rand.nextFloat() - 0.5F) * areaCoefficient / 2D;
            if (Math.abs(randX - (x + 0.5D)) >= 4.0D && Math.abs(randZ - (z + 0.5D)) >= 4.0D)
                continue;
            player.worldObj.spawnParticle(EnumParticleTypes.FLAME, randX, y + 1D, randZ, player.worldObj.rand.nextGaussian() * 0.2D, player.worldObj.rand.nextGaussian() * 0.2D, player.worldObj.rand.nextGaussian() * 0.2D);
        }
    }


    public void doEruptionEffect(EntityPlayer player, int x, int y, int z, double areaCoefficient) {
        double lowerX = x - areaCoefficient + 0.5D;
        double lowerZ = z - areaCoefficient + 0.5D;
        double upperX = x + areaCoefficient + 0.5D;
        double upperY = y + areaCoefficient;
        double upperZ = z + areaCoefficient + 0.5D;
        List eList = player.worldObj.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(lowerX, y, lowerZ, upperX, upperY, upperZ));
        Iterator iterator = eList.iterator();


        while (iterator.hasNext()) {
            Entity e = (Entity)iterator.next();
            if (e instanceof EntityLivingBase && !e.isEntityEqual(player)) {
                e.setFire(40);
                if (!e.isImmuneToFire())
                    e.attackEntityFrom(DamageSource.causePlayerDamage(player), 4F);
            }
        }
    }

    private void scanForFireChargeAndBlazePowder(ItemStack ist, EntityPlayer player) {
        List<Item> absorbItems = new ArrayList<Item>();
        absorbItems.add(Items.fire_charge);
        absorbItems.add(Items.blaze_powder);
        for (Item absorbItem : absorbItems) {
            if (!isInternalStorageFullOfItem(ist, absorbItem) && InventoryHelper.consumeItem(absorbItem, player)) {
                addItemToInternalStorage(ist, absorbItem, false);
            }
        }
    }


    private void addItemToInternalStorage(ItemStack ist, Item item, boolean isAbsorb) {
        int quantityIncrease = item == Items.fire_charge ? (isAbsorb ? getGhastAbsorbWorth() : getFireChargeWorth()) : (isAbsorb ? getBlazeAbsorbWorth() : getBlazePowderWorth());
        NBTTagCompound tagCompound = NBTHelper.getTag(ist);

        if (tagCompound.getTag("Items") == null)
            tagCompound.setTag("Items", new NBTTagList());
        NBTTagList tagList = tagCompound.getTagList("Items", 10);

        boolean added = false;
        for (int i = 0; i < tagList.tagCount(); ++i)
        {
            NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
            String itemName = tagItemData.getString("Name");
            if (itemName.equals(ContentHelper.getIdent(item))) {
                int quantity = tagItemData.getInteger("Quantity");
                tagItemData.setInteger("Quantity", quantity + quantityIncrease);
                added = true;
            }
        }
        if (!added) {
            NBTTagCompound newTagData = new NBTTagCompound();
            newTagData.setString("Name", ContentHelper.getIdent(item));
            newTagData.setInteger("Quantity", quantityIncrease);
            tagList.appendTag(newTagData);
        }

        tagCompound.setTag("Items", tagList);

        NBTHelper.setTag(ist, tagCompound);
    }

    public boolean removeItemFromInternalStorage(ItemStack ist, Item item, int cost, boolean simulate) {
        if (hasItemInInternalStorage(ist, item, cost)) {
            NBTTagCompound tagCompound = NBTHelper.getTag(ist);

            NBTTagList tagList = tagCompound.getTagList("Items", 10);

            NBTTagList replacementTagList = new NBTTagList();

            for (int i = 0; i < tagList.tagCount(); ++i)
            {
                NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
                String itemName = tagItemData.getString("Name");
                if (itemName.equals(ContentHelper.getIdent(item))) {
                    int quantity = tagItemData.getInteger("Quantity");
                    if (!simulate)
                        tagItemData.setInteger("Quantity", quantity - cost);
                }
                replacementTagList.appendTag(tagItemData);
            }
            tagCompound.setTag("Items", replacementTagList);
            NBTHelper.setTag(ist, tagCompound);
            return true;
        }
        return false;

    }

    private boolean hasItemInInternalStorage(ItemStack ist, Item item, int cost) {
        NBTTagCompound tagCompound = NBTHelper.getTag(ist);
        if (tagCompound.hasNoTags()) {
            tagCompound.setTag("Items", new NBTTagList());
            return false;
        }

        NBTTagList tagList = tagCompound.getTagList("Items", 10);
        for (int i = 0; i < tagList.tagCount(); ++i)
        {
            NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
            String itemName = tagItemData.getString("Name");
            if (itemName.equals(ContentHelper.getIdent(item))) {
                int quantity = tagItemData.getInteger("Quantity");
                return quantity >= cost;
            }
        }

        return false;
    }

    private boolean isInternalStorageFullOfItem(ItemStack ist, Item item) {
        int quantityLimit = item == Items.fire_charge ? getFireChargeLimit() : getBlazePowderLimit();
        if (hasItemInInternalStorage(ist, item, 1)) {
            NBTTagCompound tagCompound = NBTHelper.getTag(ist);
            NBTTagList tagList = tagCompound.getTagList("Items", 10);

            for (int i = 0; i < tagList.tagCount(); ++i)
            {
                NBTTagCompound tagItemData = tagList.getCompoundTagAt(i);
                String itemName = tagItemData.getString("Name");
                if (itemName.equals(ContentHelper.getIdent(item))) {
                    int quantity = tagItemData.getInteger("Quantity");
                    return quantity >= quantityLimit;
                }
            }
        }
        return false;
    }

    private int getFireChargeWorth() {
        return Settings.PyromancerStaff.fireChargeWorth;
    }
    private int getFireChargeCost() {
        return Settings.PyromancerStaff.fireChargeCost;
    }
    private int getFireChargeLimit() {
        return Settings.PyromancerStaff.fireChargeLimit;
    }
    private int getBlazePowderWorth() {
        return Settings.PyromancerStaff.blazePowderWorth;
    }
    private int getBlazePowderCost() {
        return Settings.PyromancerStaff.blazePowderCost;
    }
    private int getBlazePowderLimit() {
        return Settings.PyromancerStaff.blazePowderLimit;
    }
    private int getBlazeAbsorbWorth() {
        return Settings.PyromancerStaff.blazeAbsorbWorth;
    }
    private int getGhastAbsorbWorth() {
        return Settings.PyromancerStaff.ghastAbsorbWorth;
    }


    private void doExtinguishEffect(EntityPlayer player) {
        if (player.isBurning()) {
            player.extinguish();
        }
        int x = (int) Math.floor(player.posX);
        int y = (int) Math.floor(player.posY);
        int z = (int) Math.floor(player.posZ);
        for (int xOff = -3; xOff <= 3; xOff++) {
            for (int yOff = -3; yOff <= 3; yOff++) {
                for (int zOff = -3; zOff <= 3; zOff++)
                    if (ContentHelper.getIdent(player.worldObj.getBlockState(new BlockPos(x + xOff, y + yOff, z + zOff)).getBlock()).equals(ContentHelper.getIdent(Blocks.fire))) {
                        player.worldObj.setBlockState(new BlockPos(x + xOff, y + yOff, z + zOff), Blocks.air.getDefaultState());
                        player.worldObj.playSoundEffect(x + xOff + 0.5D, y + yOff + 0.5D, z + zOff + 0.5D, "random.fizz", 0.5F, 2.6F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.8F);
                    }
            }
        }
    }

    private void doFireballAbsorbEffect(ItemStack ist, EntityPlayer player) {
        List ghastFireballs = player.worldObj.getEntitiesWithinAABB(EntityLargeFireball.class, new AxisAlignedBB(player.posX - 5, player.posY - 5, player.posZ - 5, player.posX + 5, player.posY + 5, player.posZ + 5));
        Iterator fire1 = ghastFireballs.iterator();
        while (fire1.hasNext()) {
            EntityLargeFireball fireball = (EntityLargeFireball) fire1.next();
            if (fireball.shootingEntity == player)
                continue;
            if (player.getDistanceToEntity(fireball) < 4) {
                if (!isInternalStorageFullOfItem(ist, Items.fire_charge) && InventoryHelper.consumeItem(Items.fire_charge, player)) {
                        addItemToInternalStorage(ist, Items.fire_charge, true);
                    player.worldObj.playSoundEffect(fireball.posX, fireball.posY, fireball.posZ, "random.fizz", 0.5F, 2.6F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.8F);
                }
                    fireball.setDead();
            }
        }
        List blazeFireballs = player.worldObj.getEntitiesWithinAABB(EntitySmallFireball.class, new AxisAlignedBB(player.posX - 3, player.posY - 3, player.posZ - 3, player.posX + 3, player.posY + 3, player.posZ + 3));
        Iterator fire2 = blazeFireballs.iterator();
        while (fire2.hasNext()) {
            EntitySmallFireball fireball = (EntitySmallFireball) fire2.next();
            if (fireball.shootingEntity == player)
                continue;
            for (int particles = 0; particles < 4; particles++) {
                player.worldObj.spawnParticle(EnumParticleTypes.REDSTONE, fireball.posX, fireball.posY, fireball.posZ, 0.0D, 1.0D, 1.0D);
            }
            player.worldObj.playSoundEffect(fireball.posX, fireball.posY, fireball.posZ, "random.fizz", 0.5F, 2.6F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.8F);

            if (!isInternalStorageFullOfItem(ist, Items.blaze_powder) && InventoryHelper.consumeItem(Items.blaze_powder, player)) {
                    addItemToInternalStorage(ist, Items.blaze_powder, true);
            }
            fireball.setDead();
        }
    }
}
