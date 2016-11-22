package xreliquary.items;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xreliquary.Reliquary;
import xreliquary.entities.EntityLyssaHook;
import xreliquary.reference.Names;

import javax.annotation.Nonnull;

public class ItemRodOfLyssa extends ItemBase {
	public ItemRodOfLyssa() {
		super(Names.Items.ROD_OF_LYSSA);
		this.setCreativeTab(Reliquary.CREATIVE_TAB);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		canRepair = false;
		this.addPropertyOverride(new ResourceLocation("cast"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(@Nonnull ItemStack stack, World worldIn, EntityLivingBase entityIn) {
				return entityIn == null ? 0.0F : (entityIn.getHeldItemMainhand() == stack && entityIn instanceof EntityPlayer && ((EntityPlayer) entityIn).fishEntity != null ? 1.0F : 0.0F);
			}
		});
	}

	/**
	 * Returns true if this item should be rotated by 180 degrees around the Y axis when being held in an entities
	 * hands.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRotateAroundWhenRendering() {
		return true;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	public ActionResult<ItemStack> onItemRightClick(ItemStack ist, World world, EntityPlayer player, EnumHand hand) {
		if(player.fishEntity != null) {
			player.swingArm(hand);
			player.fishEntity.handleHookRetraction();
		} else {
			world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

			if(!world.isRemote) {
				world.spawnEntity(new EntityLyssaHook(world, player));
			}

			player.swingArm(hand);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, ist);
	}

}
