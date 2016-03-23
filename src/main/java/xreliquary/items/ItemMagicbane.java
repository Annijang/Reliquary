package xreliquary.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import xreliquary.Reliquary;
import xreliquary.reference.Names;
import xreliquary.util.LanguageHelper;

import java.util.List;

public class ItemMagicbane extends ItemSword {

	public ItemMagicbane() {
		super(ToolMaterial.GOLD);
		this.setMaxDamage(16);
		this.setMaxStackSize(1);
		canRepair = true;
		this.setCreativeTab(Reliquary.CREATIVE_TAB);
		this.setUnlocalizedName(Names.magicbane);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.EPIC;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List list, boolean par4) {
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			return;
		String value = LanguageHelper.getLocalization("item." + Names.magicbane + ".tooltip");
		for(String descriptionLine : value.split(";")) {
			if(descriptionLine != null && descriptionLine.length() > 0)
				list.add(descriptionLine);
		}
	}

	/**
	 * Returns the strength of the stack against a given block. 1.0F base,
	 * (Quality+1)*2 if correct blocktype, 1.5F if sword
	 */
	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState blockState) {
		return blockState.getBlock() == Blocks.web ? 15.0F : 1.5F;
	}

	/**
	 * Current implementations of this method in child classes do not use the
	 * entry argument beside ev. They just raise the damage on the stack.
	 */
	@Override
	public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase e, EntityLivingBase par3EntityLivingBase) {
		if(e != null) {
			int random = e.worldObj.rand.nextInt(16);
			switch(random) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
					e.addPotionEffect(new PotionEffect(MobEffects.weakness, 100, 2));
				case 5:
				case 6:
				case 7:
				case 8:
					e.addPotionEffect(new PotionEffect(MobEffects.moveSlowdown, 100, 2));
					break;
				case 9:
				case 10:
				case 11:
					e.addPotionEffect(new PotionEffect(MobEffects.moveSlowdown, 100, 2));
					break;
				case 12:
				case 13:
					e.addPotionEffect(new PotionEffect(MobEffects.poison, 100, 2));
					e.addPotionEffect(new PotionEffect(MobEffects.confusion, 100, 2));
					break;
				case 14:
					e.addPotionEffect(new PotionEffect(MobEffects.wither, 100, 2));
					e.addPotionEffect(new PotionEffect(MobEffects.blindness, 100, 2));
					break;
				default:
					break;
			}
		}
		if(par3EntityLivingBase instanceof EntityPlayer) {
			NBTTagList enchants = par1ItemStack.getEnchantmentTagList();
			int bonus = 0;
			if(enchants != null) {
				for(int enchant = 0; enchant < enchants.tagCount(); enchant++) {
					bonus += enchants.getCompoundTagAt(enchant).getShort("lvl");
				}
			}
			e.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) par3EntityLivingBase), bonus + 4);
		}
		par1ItemStack.damageItem(1, par3EntityLivingBase);
		return true;
	}
}
