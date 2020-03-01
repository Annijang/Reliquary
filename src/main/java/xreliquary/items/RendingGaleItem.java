package xreliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import xreliquary.items.util.FilteredBigItemStack;
import xreliquary.items.util.FilteredItemHandlerProvider;
import xreliquary.items.util.FilteredItemStackHandler;
import xreliquary.reference.Settings;
import xreliquary.util.InventoryHelper;
import xreliquary.util.LanguageHelper;
import xreliquary.util.NBTHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class RendingGaleItem extends ToggleableItem {
	private static final String FLIGHT_TAG = "flight";
	private static final String COUNT_TAG = "count";

	public RendingGaleItem() {
		super("rending_gale", new Properties().maxStackSize(1));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void addMoreInformation(ItemStack rendingGale, @Nullable World world, List<ITextComponent> tooltip) {
		LanguageHelper.formatTooltip(getTranslationKey() + ".tooltip2",
				ImmutableMap.of("charge", String.valueOf(getFeatherCount(rendingGale, true) / 100)), tooltip);

		if (isEnabled(rendingGale)) {
			LanguageHelper.formatTooltip("tooltip.absorb_active", ImmutableMap.of("item", TextFormatting.RED + Items.FEATHER.getDisplayName(new ItemStack(Items.FEATHER)).getString()), tooltip);
		} else {
			LanguageHelper.formatTooltip("tooltip.absorb", tooltip);
		}
	}

	@Override
	protected boolean hasMoreInformation(ItemStack stack) {
		return true;
	}

	private static int getChargeLimit() {
		return Settings.COMMON.items.rendingGale.chargeLimit.get();
	}

	public static int getChargeCost() {
		return Settings.COMMON.items.rendingGale.castChargeCost.get();
	}

	private static int getFeathersWorth() {
		return Settings.COMMON.items.rendingGale.chargeFeatherWorth.get();
	}

	private static int getBoltChargeCost() {
		return Settings.COMMON.items.rendingGale.boltChargeCost.get();
	}

	private static int getBoltTargetRange() {
		return Settings.COMMON.items.rendingGale.blockTargetRange.get();
	}

	private static int getRadialPushRadius() {
		return Settings.COMMON.items.rendingGale.pushPullRadius.get();
	}

	private void attemptFlight(LivingEntity entityLiving) {
		if (!(entityLiving instanceof PlayerEntity)) {
			return;
		}

		PlayerEntity player = (PlayerEntity) entityLiving;

		RayTraceResult rayTrace = rayTrace(player.world, player, RayTraceContext.FluidMode.NONE);

		Vec3d motion = player.getLookVec();
		if (rayTrace.getType() == RayTraceResult.Type.BLOCK) {
			double distance = player.getPosition().distanceSq(((BlockRayTraceResult) rayTrace).getPos());
			if (distance < 40) {
				double slowDownFactor = distance / 40;
				motion = player.getLookVec().mul(slowDownFactor, slowDownFactor, slowDownFactor);
			}
		}

		player.setMotion(motion);
		player.move(MoverType.SELF, player.getMotion());

		player.fallDistance = 0.0F;
	}

	@Override
	public void inventoryTick(ItemStack rendingGale, World world, Entity entity, int slotNumber, boolean isSelected) {
		if (world.isRemote || !(entity instanceof PlayerEntity)) {
			return;
		}

		PlayerEntity player = (PlayerEntity) entity;

		if (isEnabled(rendingGale) && getFeatherCount(rendingGale) + getFeathersWorth() <= getChargeLimit() && InventoryHelper.consumeItem(new ItemStack(Items.FEATHER), player)) {
			setFeatherCount(rendingGale, getFeatherCount(rendingGale) + getFeathersWorth(), !player.isHandActive());
		}
	}

	public String getMode(ItemStack stack) {
		if (NBTHelper.getString("mode", stack).equals("")) {
			setMode(stack, FLIGHT_TAG);
		}
		return NBTHelper.getString("mode", stack);
	}

	private void setMode(ItemStack stack, String s) {
		NBTHelper.putString("mode", stack, s);
	}

	private void cycleMode(ItemStack stack, boolean isRaining) {
		if (isFlightMode(stack)) {
			setMode(stack, "push");
		} else if (isPushMode(stack)) {
			setMode(stack, "pull");
		} else if (isPullMode(stack) && isRaining) {
			setMode(stack, "bolt");
		} else {
			setMode(stack, FLIGHT_TAG);
		}
	}

	private boolean isPullMode(ItemStack stack) {
		return getMode(stack).equals("pull");
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entityLiving) {
		if (entityLiving.world.isRemote || !(entityLiving instanceof PlayerEntity)) {
			return false;
		}
		PlayerEntity player = (PlayerEntity) entityLiving;
		if (player.isSneaking()) {
			cycleMode(stack, player.world.isRaining());
			return true;
		}
		return false;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return new FilteredItemHandlerProvider(Collections.singletonList(new FilteredItemStackHandler.RemovableStack(
				new FilteredBigItemStack(Items.FEATHER, Settings.COMMON.items.rendingGale.chargeFeatherWorth.get(),
						Settings.COMMON.items.rendingGale.chargeLimit.get())
				,false)));
	}

	@Override
	public int getUseDuration(ItemStack par1ItemStack) {
		return 6000;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (player.isSneaking()) {
			super.onItemRightClick(world, player, hand);
		} else {
			player.setActiveHand(hand);
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack rendingGale, LivingEntity entity, int count) {
		if (!(entity instanceof PlayerEntity)) {
			return;
		}

		PlayerEntity player = (PlayerEntity) entity;

		if (getFeatherCount(rendingGale, player.world.isRemote) <= 0) {
			player.stopActiveHand();
			return;
		}

		if (isBoltMode(rendingGale)) {
			if (count % 8 == 0) {
				spawnBolt(rendingGale, player);
			}
		} else {
			if (isFlightMode(rendingGale)) {
				attemptFlight(player);
				spawnFlightParticles(player.world, player.posX, player.posY + player.getEyeHeight(), player.posZ, player);
			} else if (isPushMode(rendingGale)) {
				doRadialPush(player.world, player.posX, player.posY, player.posZ, player, false);
			} else if (isPullMode(rendingGale)) {
				doRadialPush(player.world, player.posX, player.posY, player.posZ, player, true);
			}
			if (!player.world.isRemote) {
				setFeatherCount(rendingGale, Math.max(0, getFeatherCount(rendingGale) - getChargeCost()), false);
			}
		}
	}

	private void spawnBolt(ItemStack rendingGale, PlayerEntity player) {
		RayTraceResult rayTraceResult = player.func_213324_a(getBoltTargetRange(), 1, true);
		if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
			BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
			int attemptedY = blockRayTraceResult.getPos().getY();
			if (!player.world.isRainingAt(blockRayTraceResult.getPos())) {
				attemptedY++;
			}
			if (!player.world.isRemote && player.world.isRainingAt(new BlockPos(blockRayTraceResult.getPos().getX(), attemptedY, blockRayTraceResult.getPos().getZ()))) {
				((ServerWorld) player.world).addLightningBolt(new LightningBoltEntity(player.world, blockRayTraceResult.getPos().getX(), blockRayTraceResult.getPos().getY(), blockRayTraceResult.getPos().getZ(), false));
				setFeatherCount(rendingGale, Math.max(0, getFeatherCount(rendingGale) - (getBoltChargeCost())), false);
			}
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack rendingGale, World world, LivingEntity entityLiving, int timeLeft) {
		if (world.isRemote) {
			return;
		}

		NBTHelper.putInt(COUNT_TAG, rendingGale, getFeatherCount(rendingGale));
	}

	private boolean isPushMode(ItemStack stack) {
		return getMode(stack).equals("push");
	}

	public boolean isFlightMode(ItemStack stack) {
		return getMode(stack).equals(FLIGHT_TAG);
	}

	public boolean hasFlightCharge(ItemStack stack) {
		return getFeatherCount(stack) > 0;
	}

	private boolean isBoltMode(ItemStack stack) {
		return getMode(stack).equals("bolt");
	}

	public int getFeatherCount(ItemStack rendingGale) {
		return getFeatherCount(rendingGale, false);
	}

	private int getFeatherCount(ItemStack rendingGale, boolean isClient) {
		if (isClient) {
			return NBTHelper.getInt(COUNT_TAG, rendingGale);
		}

		return rendingGale.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.filter(FilteredItemStackHandler.class::isInstance).map(handler -> ((FilteredItemStackHandler) handler).getTotalAmount(0)).orElse(0);
	}

	public void setFeatherCount(ItemStack stack, int featherCount, boolean updateNBT) {
		stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).filter(FilteredItemStackHandler.class::isInstance)
				.ifPresent(handler -> {
					((FilteredItemStackHandler) handler).setTotalAmount(0, featherCount);
					if (updateNBT) {
						NBTHelper.putInt(COUNT_TAG, stack, featherCount);
					}
				});
	}

	public void doRadialPush(World world, double posX, double posY, double posZ, @Nullable PlayerEntity player, boolean pull) {
		//push effect free at the moment, if you restore cost, remember to change this to getFeatherCount
		spawnRadialHurricaneParticles(world, posX, posY, posZ, player, pull);
		if (world.isRemote) {
			return;
		}

		double lowerX = posX - getRadialPushRadius();
		double lowerY = posY - (double) getRadialPushRadius() / 5D;
		double lowerZ = posZ - getRadialPushRadius();
		double upperX = posX + getRadialPushRadius();
		double upperY = posY + (double) getRadialPushRadius() / 2D;
		double upperZ = posZ + getRadialPushRadius();

		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(lowerX, lowerY, lowerZ, upperX, upperY, upperZ),
				e -> (e instanceof MobEntity || e instanceof IProjectile));

		for (Entity entity : entities) {
			double distance = getDistanceToEntity(posX, posY, posZ, entity);
			if (distance < getRadialPushRadius()) {
				if (isBlacklistedEntity(entity) || entity.equals(player)) {
					continue;
				}

				Vec3d pushVector;
				if (pull) {
					pushVector = new Vec3d(posX - entity.posX, posY - entity.posY, posZ - entity.posZ);
				} else {
					pushVector = new Vec3d(entity.posX - posX, entity.posY - posY, entity.posZ - posZ);
				}
				pushVector = pushVector.normalize();
				entity.move(MoverType.PLAYER, new Vec3d(0.0D, 0.2D, 0.0D));
				entity.move(MoverType.PLAYER, new Vec3d(pushVector.x, Math.min(pushVector.y, 0.1D) * 1.5D, pushVector.z));
			}
		}
	}

	private boolean isBlacklistedEntity(Entity entity) {
		if (entity.getType().getRegistryName() == null) {
			return false;
		}

		String entityName = entity.getType().getRegistryName().toString();
		return isBlacklistedLivingEntity(entity, entityName) || Settings.COMMON.items.rendingGale.canPushProjectiles.get() && isBlacklistedProjectile(entity, entityName);
	}

	private boolean isBlacklistedProjectile(Entity entity, String entityName) {
		return entity instanceof IProjectile && Settings.COMMON.items.rendingGale.pushableProjectilesBlacklist.get().contains(entityName);
	}

	private boolean isBlacklistedLivingEntity(Entity entity, String entityName) {
		return entity instanceof MobEntity && Settings.COMMON.items.rendingGale.pushableEntitiesBlacklist.get().contains(entityName);
	}

	private float getDistanceToEntity(double posX, double posY, double posZ, Entity entityIn) {
		float f = (float) (posX - entityIn.posX);
		float f1 = (float) (posY - entityIn.posY);
		float f2 = (float) (posZ - entityIn.posZ);
		return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
	}

	private void spawnFlightParticles(World world, double x, double y, double z, PlayerEntity player) {
		Vec3d lookVector = player.getLookVec();

		BlockParticleData blockParticleData = new BlockParticleData(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.getDefaultState());

		//spawn a whole mess of particles every tick.
		for (int i = 0; i < 8; ++i) {
			float randX = 10F * (random.nextFloat() - 0.5F);
			float randY = 10F * (random.nextFloat() - 0.5F);
			float randZ = 10F * (random.nextFloat() - 0.5F);

			world.addParticle(blockParticleData, x + randX + lookVector.x * 20, y + randY + lookVector.y * 20, z + randZ + lookVector.z * 20, -lookVector.x * 5, -lookVector.y * 5, -lookVector.z * 5);
		}
	}

	private void spawnRadialHurricaneParticles(World world, double posX, double posY, double posZ, @Nullable PlayerEntity player, boolean pull) {
		BlockParticleData blockParticleData = new BlockParticleData(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.getDefaultState());

		//spawn a whole mess of particles every tick.
		for (int i = 0; i < 3; ++i) {
			float randX = world.rand.nextFloat() - 0.5F;
			float randZ = world.rand.nextFloat() - 0.5F;
			float motX = randX * 10F;
			float motZ = randZ * 10F;
			if (pull) {
				randX *= 10F;
				randZ *= 10F;
				motX *= -1F;
				motZ *= -1F;
			}

			double posYAdjusted = player == null ? posY : (posY + player.getEyeHeight()) - (player.getHeight() / 2);

			world.addParticle(blockParticleData, posX + randX, posYAdjusted, posZ + randZ, motX, 0.0D, motZ);
		}
	}

	public int getFeatherCountClient(ItemStack rendingGale, PlayerEntity player) {
		int featherCount = getFeatherCount(rendingGale, true);
		String mode = getMode(rendingGale);
		int ticksInUse = getUseDuration(rendingGale) - player.getItemInUseCount();
		if (player.isHandActive()) {
			featherCount = Math.max(0, featherCount - (mode.equals("bolt") ? getBoltChargeCost() * (ticksInUse / 8) : (getChargeCost() * ticksInUse)));
		}
		return featherCount;
	}
}
