package xreliquary.entities.potion;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import xreliquary.network.PacketFXThrownPotionImpact;
import xreliquary.network.PacketHandler;
import xreliquary.util.potions.PotionEssence;

import java.util.List;
import java.util.Random;

/**
 * Created by Xeno on 11/9/2014.
 */
public class EntityThrownXRPotion extends EntityThrowable implements IEntityAdditionalSpawnData {
	public EntityThrownXRPotion(World world) {
		super(world);
	}

	private int renderColor;
	private boolean lingering = false;
	public PotionEssence essence = null;

	public EntityThrownXRPotion(World world, double x, double y, double z, ItemStack potion) {
		this(world, x, y, z, potion, false);
	}

	public EntityThrownXRPotion(World world, double x, double y, double z, ItemStack potion, boolean lingering) {
		super(world, x, y, z);
		setEssence(potion);
		this.lingering = lingering;
	}

	private void setEssence(ItemStack ist) {
		this.essence = new PotionEssence(ist.getTagCompound());
		setRenderColor(getColor());
	}

	public EntityThrownXRPotion(World world, EntityLivingBase elb, ItemStack potion) {
		this(world, elb, potion, false);
	}
	public EntityThrownXRPotion(World world, EntityLivingBase elb, ItemStack potion, boolean lingering) {
		super(world, elb);
		setEssence(potion);
		this.lingering = lingering;
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	@Override
	protected float getGravityVelocity() {
		return 0.04F;
	}

	//no clue what these do

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	protected void onImpact(RayTraceResult result) {
		if(!this.worldObj.isRemote) {
			if (this.lingering) {
				EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.worldObj, this.posX, this.posY, this.posZ);
				entityareaeffectcloud.setOwner(this.getThrower());
				entityareaeffectcloud.setRadius(3.0F);
				entityareaeffectcloud.setRadiusOnUse(-0.5F);
				entityareaeffectcloud.setWaitTime(10);
				entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / (float)entityareaeffectcloud.getDuration());
				entityareaeffectcloud.setColor(this.renderColor);

				for (PotionEffect potioneffect : this.essence.getEffects())
				{
					entityareaeffectcloud.addEffect(new PotionEffect(potioneffect.getPotion(), potioneffect.getDuration(), potioneffect.getAmplifier()));
				}

				this.worldObj.spawnEntityInWorld(entityareaeffectcloud);
			} else {
				List<PotionEffect> list = essence.getEffects();

				if(list != null && !list.isEmpty()) {
					AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
					List<EntityLivingBase> livingEntities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

					if(!livingEntities.isEmpty()) {

						for(EntityLivingBase entitylivingbase : livingEntities) {
							double d0 = this.getDistanceSqToEntity(entitylivingbase);

							if(d0 < 16.0D) {
								double d1 = 1.0D - Math.sqrt(d0) / 4.0D;

								if(entitylivingbase == result.entityHit) {
									d1 = 1.0D;
								}

								for(PotionEffect potioneffect : list) {
									if(potioneffect.getPotion().isInstant()) {
										potioneffect.getPotion().affectEntity(this, this.getThrower(), entitylivingbase, potioneffect.getAmplifier(), d1);
									} else {
										int j = (int) (d1 * (double) potioneffect.getDuration() + 0.5D);

										if(j > 20) {
											entitylivingbase.addPotionEffect(new PotionEffect(potioneffect.getPotion(), j, potioneffect.getAmplifier(), false, false));
										}
									}
								}
							}
						}
					}
				}
			}

			spawnParticles();
			this.setDead();
		}
	}

	public int getColor() {
		//basically we're just using vanillas right now. This is hilarious in comparison to the old method, which is a mile long.
		return essence == null ? getRenderColor() : PotionUtils.getPotionColorFromEffectList(essence.getEffects());
	}

	// most of these are the same in every potion, the only thing that isn't is
	// the coloration of the particles.
	protected void spawnParticles() {
		if(worldObj.isRemote)
			return;

		Random var7 = rand;
		for(int var15 = 0; var15 < 8; ++var15) {
			worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, var7.nextGaussian() * 0.15D, var7.nextDouble() * 0.2D, var7.nextGaussian() * 0.15D, Item.getIdFromItem(Items.POTIONITEM));
		}

		worldObj.playSound(null, this.getPosition(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, worldObj.rand.nextFloat() * 0.1F + 0.9F);
		PacketHandler.networkWrapper.sendToAllAround(new PacketFXThrownPotionImpact(getColor(), this.posX, this.posY, this.posZ), new NetworkRegistry.TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 32.0D));
	}

	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.essence = new PotionEssence(tag);
		setRenderColor(tag.getInteger("color"));
		this.lingering = tag.getBoolean("lingering");
		if(this.essence.getEffects().size() == 0)
			this.setDead();
	}

	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setTag("potion", essence == null ? new NBTTagCompound() : essence.writeToNBT());
		tag.setInteger("color", getRenderColor());
		tag.setBoolean("lingering", this.lingering);
	}

	public int getRenderColor() {
		return renderColor;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(renderColor);
		buffer.writeBoolean(lingering);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		setRenderColor(additionalData.readInt());
		this.lingering = additionalData.readBoolean();
	}

	public void setRenderColor(int renderColor) {
		this.renderColor = renderColor;
	}

	public boolean getLingering() {
		return lingering;
	}
}

