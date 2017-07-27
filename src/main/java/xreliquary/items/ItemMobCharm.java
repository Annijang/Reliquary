package xreliquary.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xreliquary.Reliquary;
import xreliquary.blocks.BlockPedestal;
import xreliquary.blocks.tile.TileEntityPedestal;
import xreliquary.init.ModItems;
import xreliquary.network.PacketHandler;
import xreliquary.network.PacketMobCharmDamage;
import xreliquary.pedestal.PedestalRegistry;
import xreliquary.reference.Names;
import xreliquary.reference.Reference;
import xreliquary.reference.Settings;
import xreliquary.util.MobHelper;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemMobCharm extends ItemBase {
	private static final String TYPE_TAG = "type";

	public ItemMobCharm() {
		super(Names.Items.MOB_CHARM);
		this.setCreativeTab(Reliquary.CREATIVE_TAB);
		this.setMaxDamage(Settings.MobCharm.durability);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.canRepair = false;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack ist) {
		return "item." + Names.Items.MOB_CHARM + "_" + getType(ist);
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs creativeTab, @Nonnull NonNullList<ItemStack> subItems) {
		for(byte i = 0; i < Reference.MOB_CHARM.COUNT_TYPES; i++) {
			ItemStack subItem = new ItemStack(this);
			setType(subItem, i);
			subItems.add(subItem);
		}
	}

	public byte getType(ItemStack stack) {
		if(stack.getItem() != this || stack.getTagCompound() == null || !stack.getTagCompound().hasKey(TYPE_TAG))
			return -1;

		return stack.getTagCompound().getByte(TYPE_TAG);
	}

	private void setType(ItemStack stack, byte type) {
		NBTTagCompound compound = stack.getTagCompound();

		if(compound == null)
			compound = new NBTTagCompound();

		compound.setByte(TYPE_TAG, type);

		stack.setTagCompound(compound);
	}

	public static ItemStack getCharmStack(byte meta) {
		ItemStack mobCharm = new ItemStack(ModItems.mobCharm);

		ModItems.mobCharm.setType(mobCharm, meta);

		return mobCharm;
	}

	private byte getMobCharmTypeForEntity(Entity entity) {
		if(entity instanceof EntityGhast) {
			return Reference.MOB_CHARM.GHAST_META;
		} else if(entity instanceof EntityMagmaCube) {
			return Reference.MOB_CHARM.MAGMA_CUBE_META;
		} else if(entity instanceof EntitySlime) {
			return Reference.MOB_CHARM.SLIME_META;
		} else if(entity instanceof EntityPigZombie) {
			return Reference.MOB_CHARM.ZOMBIE_PIGMAN_META;
		} else if(entity instanceof EntityZombie) {
			return Reference.MOB_CHARM.ZOMBIE_META;
		} else if(entity instanceof EntitySkeleton || entity instanceof EntityStray) {
			return Reference.MOB_CHARM.SKELETON_META;
		} else if(entity instanceof EntityWitherSkeleton) {
			return Reference.MOB_CHARM.WITHER_SKELETON_META;
		} else if(entity instanceof EntityCreeper) {
			return Reference.MOB_CHARM.CREEPER_META;
		} else if(entity instanceof EntityWitch) {
			return Reference.MOB_CHARM.WITCH_META;
		} else if(entity instanceof EntityCaveSpider) {
			return Reference.MOB_CHARM.CAVE_SPIDER_META;
		} else if(entity instanceof EntitySpider) {
			return Reference.MOB_CHARM.SPIDER_META;
		} else if(entity instanceof EntityEnderman) {
			return Reference.MOB_CHARM.ENDERMAN_META;
		} else if(entity instanceof EntityBlaze) {
			return Reference.MOB_CHARM.BLAZE_META;
		} else if(entity instanceof EntityGuardian) {
			return Reference.MOB_CHARM.GUARDIAN_META;
		}

		return -1;
	}

	@SubscribeEvent
	public void onEntityTargetedEvent(LivingSetAttackTargetEvent event) {
		if(event.getTarget() == null)
			return;
		if(!(event.getTarget() instanceof EntityPlayer) || event.getTarget() instanceof FakePlayer)
			return;
		if(!(event.getEntity() instanceof EntityLiving))
			return;

		EntityPlayer player = (EntityPlayer) event.getTarget();
		boolean resetTarget = false;
		EntityLiving entity = (EntityLiving) event.getEntity();

		if(entity instanceof EntityZombie && !(entity instanceof EntityPigZombie)) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.ZOMBIE_META);
		} else if(entity instanceof EntityWitherSkeleton) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.WITHER_SKELETON_META);
		} else if(entity instanceof EntitySkeleton || entity instanceof EntityStray) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.SKELETON_META);
		} else if(entity instanceof EntityCreeper) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.CREEPER_META);
		} else if(entity instanceof EntityWitch) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.WITCH_META);
		} else if(entity instanceof EntityCaveSpider) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.CAVE_SPIDER_META);
		} else if(entity instanceof EntitySpider) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.SPIDER_META);
		} else if(entity instanceof EntityEnderman) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.ENDERMAN_META);
		} else if(entity instanceof EntityBlaze) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.BLAZE_META);
		} else if(entity instanceof EntityGhast) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.GHAST_META);
		} else if(entity instanceof EntityMagmaCube) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.MAGMA_CUBE_META);
		} else if(entity instanceof EntitySlime) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.SLIME_META);
		} else if(entity instanceof EntityGuardian) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.GUARDIAN_META);
		}

		if(resetTarget) {
			MobHelper.resetTarget(entity);
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		if(!(event.getEntity() instanceof EntityLiving))
			return;
		EntityLiving entity = (EntityLiving) event.getEntity();

		if(entity.getAttackTarget() == null || !(entity.getAttackTarget() instanceof EntityPlayer) || entity.getAttackTarget() instanceof FakePlayer)
			return;

		EntityPlayer player = (EntityPlayer) entity.getAttackTarget();
		boolean resetTarget = false;

		if(entity instanceof EntityGhast) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.GHAST_META);
		} else if(entity instanceof EntityMagmaCube) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.MAGMA_CUBE_META);
		} else if(entity instanceof EntitySlime) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.SLIME_META);
		} else if(entity instanceof EntityEnderman) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.ENDERMAN_META);
		} else if(entity instanceof EntityPigZombie) {
			resetTarget = isMobCharmPresent(player, Reference.MOB_CHARM.ZOMBIE_PIGMAN_META);
		}

		if(resetTarget) {
			MobHelper.resetTarget(entity, true);
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if(event.getSource() == null || event.getSource().getTrueSource() == null || !(event.getSource().getTrueSource() instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();

		if (!damagePlayersMobCharm(player, event.getEntity()))
			damageMobCharmInPedestal(player, event.getEntity());
	}

	private void damageMobCharmInPedestal(EntityPlayer player, Entity entity) {
		List<BlockPos> pedestalPositions = PedestalRegistry.getPositionsInRange(player.dimension, player.getPosition(), Settings.MobCharm.pedestalRange);
		byte mobCharmType = getMobCharmTypeForEntity(entity);
		World world = player.getEntityWorld();

		for(BlockPos pos : pedestalPositions) {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof TileEntityPedestal) {
				IBlockState blockState = world.getBlockState(pos);
				TileEntityPedestal pedestal = (TileEntityPedestal) te;
				if (blockState.getValue(BlockPedestal.ENABLED)) { //TODO this needs a field / method in TEPedestal instead of having to load blockstate
					for(int slot = 0; slot < pedestal.getSizeInventory(); slot++) {
						ItemStack slotStack = pedestal.getStackInSlot(slot);
						if(slotStack.getItem() == this && getType(slotStack) == mobCharmType) {
							if(slotStack.getItemDamage() + Settings.MobCharm.damagePerKill > slotStack.getMaxDamage()) {
								((TileEntityPedestal) te).setInventorySlotContents(slot, ItemStack.EMPTY);
							} else {
								slotStack.setItemDamage(slotStack.getItemDamage() + Settings.MobCharm.damagePerKill);
							}
							return;
						} else if(slotStack.getItem() == ModItems.mobCharmBelt) {
							int damage = ModItems.mobCharmBelt.damageCharmType(slotStack, mobCharmType);

							if(damage > -1) {
								return;
							}
						}
					}
				}
			}
		}
	}

	private boolean damagePlayersMobCharm(EntityPlayer player, Entity entity) {
		if(player.capabilities.isCreativeMode)
			return true;

		byte mobCharmType = getMobCharmTypeForEntity(entity);

		for(int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
			ItemStack stack = player.inventory.mainInventory.get(slot);

			if(stack.isEmpty())
				continue;
			if(stack.getItem() == this && getType(stack) == mobCharmType) {
				if(stack.getItemDamage() + Settings.MobCharm.damagePerKill > stack.getMaxDamage()) {
					player.inventory.mainInventory.set(slot, ItemStack.EMPTY);
					PacketHandler.networkWrapper.sendTo(new PacketMobCharmDamage(mobCharmType, stack.getMaxDamage() + 1, slot), (EntityPlayerMP) player);
				} else {
					stack.setItemDamage(stack.getItemDamage() + Settings.MobCharm.damagePerKill);
					PacketHandler.networkWrapper.sendTo(new PacketMobCharmDamage(mobCharmType, stack.getItemDamage(), slot), (EntityPlayerMP) player);
				}
				return true;
			}
			if(damageMobCharmInBelt((EntityPlayerMP) player, mobCharmType, stack))
				return true;
		}

/* TODO Baubles
		if(Loader.isModLoaded(Compatibility.MOD_ID.BAUBLES)) {
			IBaublesItemHandler inventoryBaubles = BaublesApi.getBaublesHandler(player);

			for(int i = 0; i < inventoryBaubles.getSlots(); i++) {
				ItemStack baubleStack = inventoryBaubles.getStackInSlot(i);

				if(baubleStack.isEmpty())
					continue;

				if(damageMobCharmInBelt((EntityPlayerMP) player, mobCharmType, baubleStack))
					return true;
			}
		}
*/

		return false;
	}

	private boolean damageMobCharmInBelt(EntityPlayerMP player, byte mobCharmType, ItemStack slotStack) {
		if(slotStack.getItem() == ModItems.mobCharmBelt) {
			int damage = ModItems.mobCharmBelt.damageCharmType(slotStack, mobCharmType);

			if(damage > -1) {
				PacketHandler.networkWrapper.sendTo(new PacketMobCharmDamage(mobCharmType, damage, -mobCharmType), player);
				return true;
			}
		}
		return false;
	}

	private boolean isMobCharmPresent(EntityPlayer player, byte type) {
		return playerHasMobCharm(player, type) || pedestalWithCharmInRange(player, type);
	}

	private boolean playerHasMobCharm(EntityPlayer player, byte type) {

		for(ItemStack slotStack : player.inventory.mainInventory) {
			if(slotStack.isEmpty())
				continue;
			if(slotStack.getItem() == this && getType(slotStack) == type)
				return true;
			if(slotStack.getItem() == ModItems.mobCharmBelt && ModItems.mobCharmBelt.hasCharmType(slotStack, type))
				return true;
		}

/* TODO Baubles
		if(Loader.isModLoaded(Compatibility.MOD_ID.BAUBLES)) {
			IBaublesItemHandler inventoryBaubles = BaublesApi.getBaublesHandler(player);

			for(int i = 0; i < inventoryBaubles.getSlots(); i++) {
				ItemStack baubleStack = inventoryBaubles.getStackInSlot(i);
				if(!baubleStack.isEmpty() && baubleStack.getItem() == ModItems.mobCharmBelt && ModItems.mobCharmBelt.hasCharmType(baubleStack, type))
					return true;
			}
		}
*/

		return false;
	}

	private boolean pedestalWithCharmInRange(EntityPlayer player, byte type) {
		List<BlockPos> pedestalPositions = PedestalRegistry.getPositionsInRange(player.dimension, player.getPosition(), Settings.MobCharm.pedestalRange);

		World world = player.getEntityWorld();
		for(BlockPos pos : pedestalPositions) {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof TileEntityPedestal) {
				IBlockState blockState = world.getBlockState(pos);
				TileEntityPedestal pedestal = (TileEntityPedestal) te;
				if (blockState.getValue(BlockPedestal.ENABLED)) { //TODO this needs a field / method in TEPedestal instead of having to load blockstate
					for(int slot = 0; slot < pedestal.getSizeInventory(); slot++) {
						ItemStack slotStack = pedestal.getStackInSlot(slot);
						if(slotStack.getItem() == this && getType(slotStack) == type)
							return true;
						if(slotStack.getItem() == ModItems.mobCharmBelt && ModItems.mobCharmBelt.hasCharmType(slotStack, type))
							return true;
					}
				}
			}
		}

		return false;
	}
}
