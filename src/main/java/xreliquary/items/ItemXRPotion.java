package xreliquary.items;


import com.google.common.collect.HashMultimap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xreliquary.Reliquary;
import xreliquary.blocks.BlockApothecaryCauldron;
import xreliquary.blocks.tile.TileEntityCauldron;
import xreliquary.entities.potion.EntityThrownXRPotion;
import xreliquary.init.ModItems;
import xreliquary.reference.Colors;
import xreliquary.reference.Names;
import xreliquary.reference.Settings;
import xreliquary.util.NBTHelper;
import xreliquary.util.potions.PotionEssence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Xeno on 11/9/2014.
 */
public class ItemXRPotion extends ItemBase {

    public ItemXRPotion() {
        super(Names.potion);
        this.setCreativeTab(Reliquary.CREATIVE_TAB);
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
    }


    // returns an empty vial when used in crafting recipes, unless it's one of
    // the base potion types.
    @Override
    public boolean hasContainerItem(ItemStack ist) {
        PotionEssence essence = new PotionEssence(ist.getTagCompound());
        return essence.getEffects().size() > 0;
    }
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack ist, EntityPlayer player, List list, boolean flag) {
        PotionEssence essence = new PotionEssence(ist.getTagCompound());
        if (essence.getEffects().size() > 0) {
            HashMultimap hashmultimap = HashMultimap.create();
            Iterator iterator1;

            if (essence.getEffects() != null && !essence.getEffects().isEmpty())
            {
                iterator1 = essence.getEffects().iterator();

                while (iterator1.hasNext())
                {
                    PotionEffect potioneffect = (PotionEffect)iterator1.next();
                    String s1 = StatCollector.translateToLocal(potioneffect.getEffectName()).trim();
                    Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
                    Map<IAttribute, AttributeModifier> map = potion.getAttributeModifierMap();

                    if (map != null && map.size() > 0)
                    {
                        Iterator iterator = map.entrySet().iterator();

                        while (iterator.hasNext())
                        {
                            Map.Entry entry = (Map.Entry)iterator.next();
                            AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
                            AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), potion.getAttributeModifierAmount(potioneffect.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                            hashmultimap.put(((IAttribute)entry.getKey()).getAttributeUnlocalizedName(), attributemodifier1);
                        }
                    }

                    if (potioneffect.getAmplifier() > 0)
                    {
                        s1 = s1 + " " + (potioneffect.getAmplifier() + 1);
                    }

                    if (potioneffect.getDuration() > 20)
                    {
                        s1 = s1 + " (" + Potion.getDurationString(potioneffect) + ")";
                    }

                    if (potion.isBadEffect())
                    {
                        list.add(EnumChatFormatting.RED + s1);
                    }
                    else
                    {
                        list.add(EnumChatFormatting.GRAY + s1);
                    }
                }
            }
            else
            {
                String s = StatCollector.translateToLocal("potion.empty").trim();
                list.add(EnumChatFormatting.GRAY + s);
            }

            if (!hashmultimap.isEmpty())
            {
                list.add("");
                list.add(EnumChatFormatting.DARK_PURPLE + StatCollector.translateToLocal("potion.effects.whenDrank"));
                iterator1 = hashmultimap.entries().iterator();

                while (iterator1.hasNext())
                {
                    Map.Entry entry1 = (Map.Entry)iterator1.next();
                    AttributeModifier attributemodifier2 = (AttributeModifier)entry1.getValue();
                    double d0 = attributemodifier2.getAmount();
                    double d1;

                    if (attributemodifier2.getOperation() != 1 && attributemodifier2.getOperation() != 2)
                    {
                        d1 = attributemodifier2.getAmount();
                    }
                    else
                    {
                        d1 = attributemodifier2.getAmount() * 100.0D;
                    }

                    if (d0 > 0.0D)
                    {
                        list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier2.getOperation(), new Object[]{ItemStack.DECIMALFORMAT.format(d1), StatCollector.translateToLocal("attribute.name." + (String) entry1.getKey())}));
                    }
                    else if (d0 < 0.0D)
                    {
                        d1 *= -1.0D;
                        list.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted("attribute.modifier.take." + attributemodifier2.getOperation(), new Object[]{ItemStack.DECIMALFORMAT.format(d1), StatCollector.translateToLocal("attribute.name." + (String) entry1.getKey())}));
                    }
                }
            }
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack ist, World world, EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            --ist.stackSize;
        }
        if (!world.isRemote) {
            for (PotionEffect effect : new PotionEssence(ist.getTagCompound()).getEffects()) {
                if (effect == null)
                    continue;
                player.addPotionEffect(new PotionEffect(effect.getPotionID(), effect.getDuration(), effect.getAmplifier(), false, false));
            }
        }
        if (!player.capabilities.isCreativeMode) {
            if (ist.stackSize <= 0)
                return new ItemStack(this, 1, 0);
            player.inventory.addItemStackToInventory(new ItemStack(this, 1, 0));
        }
        return ist;
    }

    public boolean getSplash(ItemStack ist) {
        return NBTHelper.getBoolean("splash", ist);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack ist, int renderPass) {
        if (renderPass == 1)
            return getColor(ist);
        else
            return Integer.parseInt(Colors.PURE, 16);
    }

    public int getColor(ItemStack itemStack) {
        //used when rendering as thrown entity
        if (NBTHelper.getInteger("renderColor", itemStack) > 0)
            return NBTHelper.getInteger("renderColor", itemStack);

        PotionEssence essence = new PotionEssence(itemStack.getTagCompound());
        boolean hasEffect = essence.getEffects().size() > 0;
        if (!hasEffect)
            return Integer.parseInt(Colors.PURE, 16);

        return PotionHelper.calcPotionLiquidColor(new PotionEssence(itemStack.getTagCompound()).getEffects());
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 16;
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        subItems.add(new ItemStack(ModItems.potion)); //just an empty one

        List<ItemStack> splashPotions = new ArrayList<>();
        for(PotionEssence essence : Settings.Potions.uniquePotions) {
            ItemStack potion = new ItemStack(ModItems.potion, 1);
            potion.setTagCompound(essence.writeToNBT());
            NBTHelper.setBoolean("hasPotion", potion, true);

            ItemStack splashPotion = potion.copy();
            NBTHelper.setBoolean("splash", splashPotion, true);

            subItems.add(potion);
            splashPotions.add(splashPotion);
        }
        subItems.addAll(splashPotions);
    }

    /**
     * returns the action that specifies what animation to play when the items
     * is being used
     */
    @Override
    public EnumAction getItemUseAction(ItemStack ist) {
        if (!getSplash(ist) && new PotionEssence(ist.getTagCompound()).getEffects().size() > 0)
            return EnumAction.DRINK;
        return EnumAction.NONE;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is
     * pressed. Args: itemStack, world, entityPlayer
     */

    @Override
    public ItemStack onItemRightClick(ItemStack ist, World world, EntityPlayer player) {
        PotionEssence essence = new PotionEssence(ist.getTagCompound());
        if (!getSplash(ist)) {
            if (essence.getEffects().size() > 0) {
                player.setItemInUse(ist, this.getMaxItemUseDuration(ist));
                return ist;
            } else {
                MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);

                if (mop == null)
                    return ist;
                else {
                    if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        if (world.getBlockState(mop.getBlockPos()).getBlock() instanceof BlockApothecaryCauldron) {
                            TileEntityCauldron cauldronTile = (TileEntityCauldron)world.getTileEntity(mop.getBlockPos());
                            NBTTagCompound potionTag = cauldronTile.removeContainedPotion(world);
                            ItemStack newPotion = new ItemStack(this, 1, 0);
                            newPotion.setTagCompound(potionTag);

                            if (--ist.stackSize <= 0) {
                                return newPotion;
                            }

                            if (!player.inventory.addItemStackToInventory(newPotion)) {
                                player.entityDropItem(newPotion, 0.1F);
                            }
                        }
                    }
                }
            }
        } else {
            if (world.isRemote)
                return ist;
            Entity e = new EntityThrownXRPotion(world, player, ist);
            if (e == null)
                return ist;
            if (!player.capabilities.isCreativeMode) {
                --ist.stackSize;
            }
            world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            world.spawnEntityInWorld(e);
        }
        return ist;
    }
}
