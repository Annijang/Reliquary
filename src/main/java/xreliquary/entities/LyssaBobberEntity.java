package xreliquary.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import xreliquary.init.ModEntities;
import xreliquary.init.ModItems;
import xreliquary.reference.Settings;
import xreliquary.util.LogHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class LyssaBobberEntity extends ProjectileEntity implements IEntityAdditionalSpawnData {
	private final Random field_234596_b_ = new Random();
	private boolean field_234597_c_;
	private int field_234598_d_;
	private static final DataParameter<Integer> DATA_HOOKED_ENTITY = EntityDataManager.createKey(FishingBobberEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> field_234599_f_ = EntityDataManager.createKey(FishingBobberEntity.class, DataSerializers.BOOLEAN);
	private int ticksInGround;
	private int ticksCatchable;
	private int ticksCaughtDelay;
	private int ticksCatchableDelay;
	private float fishApproachAngle;
	private boolean field_234595_aq_ = true;
	private Entity caughtEntity;
	private State currentState = State.FLYING;
	private int luck;
	private int lureSpeed;

	public LyssaBobberEntity(EntityType<LyssaBobberEntity> entityType, World world) {
		super(entityType, world);
		prevPosX = getPosX();
		prevPosY = getPosY();
		prevPosZ = getPosZ();
	}

	public LyssaBobberEntity(World world, PlayerEntity fishingPlayer, int lureSpeed, int luck) {
		super(ModEntities.LYSSA_HOOK, world);
		ignoreFrustumCheck = true;
		setShooter(fishingPlayer);
		this.luck = Math.max(0, luck);
		this.lureSpeed = Math.max(0, lureSpeed);
		shoot(fishingPlayer);

		//Reliquary
		speedUp();
	}

	private void shoot(PlayerEntity fishingPlayer) {
		float f = fishingPlayer.rotationPitch;
		float f1 = fishingPlayer.rotationYaw;
		float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
		float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
		double d0 = fishingPlayer.getPosX() - (double) f3 * 0.3D;
		double d1 = fishingPlayer.getPosY() + (double) fishingPlayer.getEyeHeight();
		double d2 = fishingPlayer.getPosZ() - (double) f2 * 0.3D;
		setLocationAndAngles(d0, d1, d2, f1, f);
		Vector3d vec3d = new Vector3d(-f3, MathHelper.clamp(-(f5 / f4), -5.0F, 5.0F), -f2);
		double d3 = vec3d.length();
		vec3d = vec3d.mul(0.6D / d3 + 0.5D + rand.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + rand.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + rand.nextGaussian() * 0.0045D);
		setMotion(vec3d);
		rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI));
		rotationPitch = (float) (MathHelper.atan2(vec3d.y, MathHelper.sqrt(getDistanceSq(vec3d))) * (double) (180F / (float) Math.PI));
		prevRotationYaw = rotationYaw;
		prevRotationPitch = rotationPitch;
	}

	@Override
	protected void registerData() {
		getDataManager().register(DATA_HOOKED_ENTITY, 0);
		getDataManager().register(field_234599_f_, false);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (DATA_HOOKED_ENTITY.equals(key)) {
			int i = getDataManager().get(DATA_HOOKED_ENTITY);
			caughtEntity = i > 0 ? world.getEntityByID(i - 1) : null;
		}

		if (field_234599_f_.equals(key)) {
			field_234597_c_ = getDataManager().get(field_234599_f_);
			if (field_234597_c_) {
				setMotion(getMotion().x, -0.4F * MathHelper.nextFloat(field_234596_b_, 0.6F, 1.0F), getMotion().z);
			}
		}

		super.notifyDataManagerChange(key);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		//noop
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	private void tickOriginal() {
		field_234596_b_.setSeed(getUniqueID().getLeastSignificantBits() ^ world.getGameTime());
		super.tick();
		PlayerEntity playerentity = getFishingPlayer();
		if (playerentity == null) {
			remove();
		} else if (world.isRemote || !shouldStopFishing(playerentity)) {
			if (onGround) {
				++ticksInGround;
				if (ticksInGround >= 1200) {
					remove();
					return;
				}
			} else {
				ticksInGround = 0;
			}

			float f = 0.0F;
			BlockPos blockpos = getPosition();
			FluidState fluidstate = world.getFluidState(blockpos);
			if (fluidstate.isTagged(FluidTags.WATER)) {
				f = fluidstate.getActualHeight(world, blockpos);
			}

			boolean flag = f > 0.0F;
			if (currentState == State.FLYING) {
				if (caughtEntity != null) {
					setMotion(Vector3d.ZERO);
					currentState = State.HOOKED_IN_ENTITY;
					return;
				}

				if (flag) {
					setMotion(getMotion().mul(0.3D, 0.2D, 0.3D));
					currentState = State.BOBBING;
					return;
				}

				checkCollision();
			} else {
				if (currentState == State.HOOKED_IN_ENTITY) {
					if (caughtEntity != null) {
						if (caughtEntity.removed) {
							caughtEntity = null;
							currentState = State.FLYING;
						} else {
							setPosition(caughtEntity.getPosX(), caughtEntity.getPosYHeight(0.8D), caughtEntity.getPosZ());
						}
					}

					return;
				}

				if (currentState == State.BOBBING) {
					Vector3d vector3d = getMotion();
					double d0 = getPosY() + vector3d.y - (double) blockpos.getY() - (double) f;
					if (Math.abs(d0) < 0.01D) {
						d0 += Math.signum(d0) * 0.1D;
					}

					setMotion(vector3d.x * 0.9D, vector3d.y - d0 * (double) rand.nextFloat() * 0.2D, vector3d.z * 0.9D);
					if (ticksCatchable <= 0 && ticksCatchableDelay <= 0) {
						field_234595_aq_ = true;
					} else {
						field_234595_aq_ = field_234595_aq_ && field_234598_d_ < 10 && func_234603_b_(blockpos);
					}

					if (flag) {
						field_234598_d_ = Math.max(0, field_234598_d_ - 1);
						if (field_234597_c_) {
							setMotion(getMotion().add(0.0D, -0.1D * (double) field_234596_b_.nextFloat() * (double) field_234596_b_.nextFloat(), 0.0D));
						}

						if (!world.isRemote) {
							catchingFish(blockpos);
						}
					} else {
						field_234598_d_ = Math.min(10, field_234598_d_ + 1);
					}
				}
			}

			if (!fluidstate.isTagged(FluidTags.WATER)) {
				setMotion(getMotion().add(0.0D, -0.03D, 0.0D));
			}

			move(MoverType.SELF, getMotion());
			func_234617_x_();
			if (currentState == State.FLYING && (onGround || collidedHorizontally)) {
				setMotion(Vector3d.ZERO);
			}

			setMotion(getMotion().scale(0.92D));
			recenterBoundingBox();
		}
	}

	@Nullable
	public PlayerEntity getFishingPlayer() {
		Entity entity = func_234616_v_();
		return entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
	}

	private boolean func_234603_b_(BlockPos p_234603_1_) {
		WaterType waterType = WaterType.INVALID;

		for (int i = -1; i <= 2; ++i) {
			WaterType fishingbobberentity$watertype1 = func_234602_a_(p_234603_1_.add(-2, i, -2), p_234603_1_.add(2, i, 2));
			switch (fishingbobberentity$watertype1) {
				case INVALID:
					return false;
				case ABOVE_WATER:
					if (waterType == WaterType.INVALID) {
						return false;
					}
					break;
				case INSIDE_WATER:
					if (waterType == WaterType.ABOVE_WATER) {
						return false;
					}
			}

			waterType = fishingbobberentity$watertype1;
		}

		return true;
	}

	private WaterType func_234602_a_(BlockPos p_234602_1_, BlockPos p_234602_2_) {
		return BlockPos.getAllInBox(p_234602_1_, p_234602_2_).map(this::func_234604_c_).reduce((p_234601_0_, p_234601_1_) -> {
			return p_234601_0_ == p_234601_1_ ? p_234601_0_ : WaterType.INVALID;
		}).orElse(WaterType.INVALID);
	}

	private WaterType func_234604_c_(BlockPos p_234604_1_) {
		BlockState blockstate = world.getBlockState(p_234604_1_);
		if (!blockstate.isAir() && !blockstate.isIn(Blocks.LILY_PAD)) {
			FluidState fluidstate = blockstate.getFluidState();
			return fluidstate.isTagged(FluidTags.WATER) && fluidstate.isSource() && blockstate.getCollisionShape(world, p_234604_1_).isEmpty() ? WaterType.INSIDE_WATER : WaterType.INVALID;
		} else {
			return WaterType.ABOVE_WATER;
		}
	}

	private void updateRotation() {
		Vector3d vec3d = getMotion();
		float f = MathHelper.sqrt(getDistanceSq(vec3d));
		rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI));

		rotationPitch = (float) (MathHelper.atan2(vec3d.y, f) * (double) (180F / (float) Math.PI));
		while (rotationPitch - prevRotationPitch < -180.0F) {
			prevRotationPitch -= 360.0F;
		}

		while (rotationPitch - prevRotationPitch >= 180.0F) {
			prevRotationPitch += 360.0F;
		}

		while (rotationYaw - prevRotationYaw < -180.0F) {
			prevRotationYaw -= 360.0F;
		}

		while (rotationYaw - prevRotationYaw >= 180.0F) {
			prevRotationYaw += 360.0F;
		}

		rotationPitch = MathHelper.lerp(0.2F, prevRotationPitch, rotationPitch);
		rotationYaw = MathHelper.lerp(0.2F, prevRotationYaw, rotationYaw);
	}

	private void checkCollision() {
		RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, this::func_230298_a_);
		onImpact(raytraceresult);
	}

	protected boolean func_230298_a_(Entity p_230298_1_) {
		return super.func_230298_a_(p_230298_1_) || p_230298_1_.isAlive() && p_230298_1_ instanceof ItemEntity;
	}

	@Override
	protected void onEntityHit(EntityRayTraceResult p_213868_1_) {
		super.onEntityHit(p_213868_1_);
		if (!world.isRemote) {
			caughtEntity = p_213868_1_.getEntity();
			setHookedEntity();
		}
	}

	@Override
	protected void func_230299_a_(BlockRayTraceResult p_230299_1_) {
		super.func_230299_a_(p_230299_1_);
		setMotion(getMotion().normalize().scale(p_230299_1_.func_237486_a_(this)));
	}

	private void setHookedEntity() {
		getDataManager().set(DATA_HOOKED_ENTITY, caughtEntity.getEntityId() + 1);
	}

	private void catchingFish(BlockPos p_190621_1_) {
		ServerWorld serverworld = (ServerWorld) world;
		int i = 1;
		BlockPos blockpos = p_190621_1_.up();
		if (rand.nextFloat() < 0.25F && world.isRainingAt(blockpos)) {
			++i;
		}

		if (rand.nextFloat() < 0.5F && !world.canSeeSky(blockpos)) {
			--i;
		}

		if (ticksCatchable > 0) {
			--ticksCatchable;
			if (ticksCatchable <= 0) {
				ticksCaughtDelay = 0;
				ticksCatchableDelay = 0;
				getDataManager().set(field_234599_f_, false);
			}
		} else if (ticksCatchableDelay > 0) {
			ticksCatchableDelay -= i;
			if (ticksCatchableDelay > 0) {
				fishApproachAngle = (float) ((double) fishApproachAngle + rand.nextGaussian() * 4.0D);
				float f = fishApproachAngle * ((float) Math.PI / 180F);
				float f1 = MathHelper.sin(f);
				float f2 = MathHelper.cos(f);
				double d0 = getPosX() + (double) (f1 * (float) ticksCatchableDelay * 0.1F);
				double d1 = (float) MathHelper.floor(getPosY()) + 1.0F;
				double d2 = getPosZ() + (double) (f2 * (float) ticksCatchableDelay * 0.1F);
				BlockState blockstate = serverworld.getBlockState(new BlockPos(d0, d1 - 1.0D, d2));
				if (serverworld.getBlockState(new BlockPos((int) d0, (int) d1 - 1, (int) d2)).getMaterial() == net.minecraft.block.material.Material.WATER) {
					if (rand.nextFloat() < 0.15F) {
						serverworld.spawnParticle(ParticleTypes.BUBBLE, d0, d1 - (double) 0.1F, d2, 1, f1, 0.1D, f2, 0.0D);
					}

					float f3 = f1 * 0.04F;
					float f4 = f2 * 0.04F;
					serverworld.spawnParticle(ParticleTypes.FISHING, d0, d1, d2, 0, f4, 0.01D, -f3, 1.0D);
					serverworld.spawnParticle(ParticleTypes.FISHING, d0, d1, d2, 0, -f4, 0.01D, f3, 1.0D);
				}
			} else {
				playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
				double d3 = getPosY() + 0.5D;
				serverworld.spawnParticle(ParticleTypes.BUBBLE, getPosX(), d3, getPosZ(), (int) (1.0F + getWidth() * 20.0F), getWidth(), 0.0D, getWidth(), 0.2F);
				serverworld.spawnParticle(ParticleTypes.FISHING, getPosX(), d3, getPosZ(), (int) (1.0F + getWidth() * 20.0F), getWidth(), 0.0D, getWidth(), 0.2F);
				ticksCatchable = MathHelper.nextInt(rand, 20, 40);
				getDataManager().set(field_234599_f_, true);
			}
		} else if (ticksCaughtDelay > 0) {
			ticksCaughtDelay -= i;
			float f5 = 0.15F;
			if (ticksCaughtDelay < 20) {
				f5 = (float) ((double) f5 + (double) (20 - ticksCaughtDelay) * 0.05D);
			} else if (ticksCaughtDelay < 40) {
				f5 = (float) ((double) f5 + (double) (40 - ticksCaughtDelay) * 0.02D);
			} else if (ticksCaughtDelay < 60) {
				f5 = (float) ((double) f5 + (double) (60 - ticksCaughtDelay) * 0.01D);
			}

			if (rand.nextFloat() < f5) {
				float f6 = MathHelper.nextFloat(rand, 0.0F, 360.0F) * ((float) Math.PI / 180F);
				float f7 = MathHelper.nextFloat(rand, 25.0F, 60.0F);
				double d4 = getPosX() + (double) (MathHelper.sin(f6) * f7 * 0.1F);
				double d5 = (float) MathHelper.floor(getPosY()) + 1.0F;
				double d6 = getPosZ() + (double) (MathHelper.cos(f6) * f7 * 0.1F);
				BlockState blockstate1 = serverworld.getBlockState(new BlockPos(d4, d5 - 1.0D, d6));
				if (serverworld.getBlockState(new BlockPos(d4, d5 - 1.0D, d6)).getMaterial() == net.minecraft.block.material.Material.WATER) {
					serverworld.spawnParticle(ParticleTypes.SPLASH, d4, d5, d6, 2 + rand.nextInt(2), 0.1F, 0.0D, 0.1F, 0.0D);
				}
			}

			if (ticksCaughtDelay <= 0) {
				fishApproachAngle = MathHelper.nextFloat(rand, 0.0F, 360.0F);
				ticksCatchableDelay = MathHelper.nextInt(rand, 20, 80);
			}
		} else {
			ticksCaughtDelay = MathHelper.nextInt(rand, 100, 600);
			ticksCaughtDelay -= lureSpeed * 20 * 5;
		}

	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeAdditional(CompoundNBT compound) {
		//noop
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditional(CompoundNBT compound) {
		//noop
	}

	private int handleHookRetractionOriginal(ItemStack stack) {
		PlayerEntity playerentity = getFishingPlayer();
		if (!world.isRemote && playerentity != null) {
			int i = 0;
			if (caughtEntity != null) {
				bringInHookedEntity();
				world.setEntityState(this, (byte) 31);
				i = caughtEntity instanceof ItemEntity ? 3 : 5;
			} else if (ticksCatchable > 0) {
				LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) world)).withParameter(LootParameters.field_237457_g_, getPositionVec()).withParameter(LootParameters.TOOL, stack).withParameter(LootParameters.THIS_ENTITY, this).withRandom(rand).withLuck((float) luck + playerentity.getLuck());
				lootcontext$builder.withParameter(LootParameters.KILLER_ENTITY, func_234616_v_()).withParameter(LootParameters.THIS_ENTITY, this);
				LootTable loottable = world.getServer().getLootTableManager().getLootTableFromLocation(LootTables.GAMEPLAY_FISHING);
				List<ItemStack> list = loottable.generate(lootcontext$builder.build(LootParameterSets.FISHING));

				for (ItemStack itemstack : list) {
					ItemEntity itementity = new ItemEntity(world, getPosX(), getPosY(), getPosZ(), itemstack);
					double d0 = playerentity.getPosX() - getPosX();
					double d1 = playerentity.getPosY() - getPosY();
					double d2 = playerentity.getPosZ() - getPosZ();
					double d3 = 0.1D;
					itementity.setMotion(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
					world.addEntity(itementity);
					playerentity.world.addEntity(new ExperienceOrbEntity(playerentity.world, playerentity.getPosX(), playerentity.getPosY() + 0.5D, playerentity.getPosZ() + 0.5D, rand.nextInt(6) + 1));
					if (itemstack.getItem().isIn(ItemTags.FISHES)) {
						playerentity.addStat(Stats.FISH_CAUGHT, 1);
					}
				}

				i = 1;
			}

			if (onGround) {
				i = 2;
			}

			remove();
			return i;
		} else {
			return 0;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 31 && world.isRemote && caughtEntity instanceof PlayerEntity && ((PlayerEntity) caughtEntity).isUser()) {
			bringInHookedEntity();
		}

		super.handleStatusUpdate(id);
	}

	private void bringInHookedEntityOriginal() {
		Entity entity = func_234616_v_();
		if (entity != null) {
			Vector3d vector3d = (new Vector3d(entity.getPosX() - getPosX(), entity.getPosY() - getPosY(), entity.getPosZ() - getPosZ())).scale(0.1D);
			caughtEntity.setMotion(caughtEntity.getMotion().add(vector3d));
		}
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	/**
	 * Will get destroyed next tick.
	 */
	@Override
	public void remove(boolean keepData) {
		super.remove(keepData);
		PlayerEntity playerentity = getFishingPlayer();
		if (playerentity != null) {
			playerentity.fishingBobber = null;
		}
	}

	@Override
	public boolean isNonBoss() {
		return false;
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		PlayerEntity fishingPlayer = getFishingPlayer();
		buffer.writeInt(fishingPlayer != null ? fishingPlayer.getEntityId() : 0);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		int entityId = additionalData.readInt();
		if (entityId != 0) {
			setShooter(world.getEntityByID(entityId));
		}
	}

	private enum State {
		FLYING, HOOKED_IN_ENTITY, BOBBING
	}

	enum WaterType {
		ABOVE_WATER,
		INSIDE_WATER,
		INVALID
	}

	/*
		Reliquary customizations to the default FishingBobberEntity behavior
	*/

	private Optional<PlayerEntity> getFishingPlayerOptional() {
		return Optional.ofNullable(getFishingPlayer());
	}

	private void speedUp() {
		//faster speed of the hook except for casting down

		if (getMotion().getY() >= 0) {
			setMotion(getMotion().mul(2, 2, 2));
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distance) {
		//much higher visible range than regular hook
		return distance < 16384;
	}

	@Override
	public void tick() {
		tickOriginal();

		pullItemEntitiesTowardsHook();
	}

	private boolean shouldStopFishing(PlayerEntity fishingPlayer) {
		ItemStack itemstack = fishingPlayer.getHeldItemMainhand();
		ItemStack itemstack1 = fishingPlayer.getHeldItemOffhand();
		boolean flag = itemstack.getItem() == ModItems.ROD_OF_LYSSA.get();
		boolean flag1 = itemstack1.getItem() == ModItems.ROD_OF_LYSSA.get();
		if (fishingPlayer.isAlive() && (flag || flag1) && getDistanceSq(fishingPlayer) <= 10240.0D) {
			return false;
		} else {
			remove();
			return true;
		}
	}

	private void pullItemEntitiesTowardsHook() {
		if (isAlive() && caughtEntity == null) {
			float f = 0.0F;
			BlockPos blockpos = getPosition();

			FluidState fluidState = world.getFluidState(blockpos);
			if (fluidState.isTagged(FluidTags.WATER)) {
				f = fluidState.getActualHeight(world, blockpos);
			}

			if (f <= 0F) {
				List<Entity> list = world.getEntitiesWithinAABB(ItemEntity.class, getBoundingBox().expand(getMotion()).grow(3.0D));

				for (Entity e : list) {
					Vector3d pullVector = new Vector3d(getPosX() - e.getPosX(), getPosY() - e.getPosY(), getPosZ() - e.getPosZ()).normalize();
					e.setMotion(pullVector.mul(0.4D, 0.4D, 0.4D));
				}
			}
		}
	}

	private void bringInHookedEntity() {
		bringInHookedEntityOriginal();

		if (caughtEntity instanceof ItemEntity) {
			caughtEntity.setMotion(caughtEntity.getMotion().mul(4D, 4D, 4D));
		} else if (caughtEntity instanceof LivingEntity) {
			caughtEntity.setMotion(caughtEntity.getMotion().mul(1, 1.5D, 1));
		}
	}

	public void handleHookRetraction(ItemStack stack) {
		if (!world.isRemote) {
			if (caughtEntity != null && getFishingPlayerOptional().map(Entity::isCrouching).orElse(false) && canStealFromEntity()) {
				stealFromLivingEntity();
				remove();
			} else {
				handleHookRetractionOriginal(stack);
			}

			pullItemEntitiesWithHook();
		}

	}

	private boolean canStealFromEntity() {
		return caughtEntity instanceof LivingEntity && (Settings.COMMON.items.rodOfLyssa.stealFromPlayers.get() || !(caughtEntity instanceof PlayerEntity));
	}

	private void pullItemEntitiesWithHook() {
		List<ItemEntity> pullingItemsList = world.getEntitiesWithinAABB(ItemEntity.class, getBoundingBox().expand(getMotion()).grow(1.0D, 1.0D, 1.0D));

		getFishingPlayerOptional().ifPresent(p -> {
			for (ItemEntity e : pullingItemsList) {
				double d1 = p.getPosX() - getPosX();
				double d3 = p.getPosY() - getPosY();
				double d5 = p.getPosZ() - getPosZ();
				double d7 = MathHelper.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
				double d9 = 0.1D;
				e.setMotion(d1 * d9, d3 * d9 + (double) MathHelper.sqrt(d7) * 0.08D, d5 * d9);
			}
		});
	}

	private void stealFromLivingEntity() {
		LivingEntity livingEntity = (LivingEntity) caughtEntity;
		EquipmentSlotType slotBeingStolenFrom = EquipmentSlotType.values()[world.rand.nextInt(EquipmentSlotType.values().length)];

		ItemStack stolenStack = livingEntity.getItemStackFromSlot(slotBeingStolenFrom);
		if (stolenStack.isEmpty() && Boolean.TRUE.equals(Settings.COMMON.items.rodOfLyssa.stealFromVacantSlots.get())) {
			for (EquipmentSlotType slot : EquipmentSlotType.values()) {
				stolenStack = livingEntity.getItemStackFromSlot(slot);
				if (!stolenStack.isEmpty() && canDropFromSlot(livingEntity, slot)) {
					slotBeingStolenFrom = slot;
					break;
				}
			}
		}

		float failProbabilityFactor;

		Optional<PlayerEntity> p = getFishingPlayerOptional();

		if (!p.isPresent()) {
			return;
		}

		PlayerEntity fishingPlayer = p.get();

		if (Boolean.TRUE.equals(Settings.COMMON.items.rodOfLyssa.useLeveledFailureRate.get())) {
			failProbabilityFactor = 1F / ((float) Math.sqrt(Math.max(1, Math.min(fishingPlayer.experienceLevel, Settings.COMMON.items.rodOfLyssa.levelCapForLeveledFormula.get()))) * 2);
		} else {
			failProbabilityFactor = Settings.COMMON.items.rodOfLyssa.flatStealFailurePercentRate.get() / 100F;
		}

		if ((rand.nextFloat() <= failProbabilityFactor || (stolenStack.isEmpty() && Settings.COMMON.items.rodOfLyssa.failStealFromVacantSlots.get())) && Boolean.TRUE.equals(Settings.COMMON.items.rodOfLyssa.angerOnStealFailure.get())) {
			livingEntity.attackEntityFrom(DamageSource.causePlayerDamage(fishingPlayer), 0.0F);
		}
		if (!stolenStack.isEmpty()) {
			int randomItemDamage = world.rand.nextInt(3);
			stolenStack.damageItem(randomItemDamage, livingEntity, e -> {});
			ItemEntity entityitem = new ItemEntity(world, getPosX(), getPosY(), getPosZ(), stolenStack);
			entityitem.setPickupDelay(5);
			double d1 = fishingPlayer.getPosX() - getPosX();
			double d3 = fishingPlayer.getPosY() - getPosY();
			double d5 = fishingPlayer.getPosZ() - getPosZ();
			double d7 = MathHelper.sqrt(d1 * d1 + d3 * d3 + d5 * d5);
			double d9 = 0.1D;
			entityitem.setMotion(d1 * d9, d3 * d9 + (double) MathHelper.sqrt(d7) * 0.08D, d5 * d9);
			world.addEntity(entityitem);

			livingEntity.setItemStackToSlot(slotBeingStolenFrom, ItemStack.EMPTY);
		}
	}

	private boolean canDropFromSlot(LivingEntity entity, EquipmentSlotType slot) {
		if (!(entity instanceof MobEntity)) {
			return true;
		}
		MobEntity livingEntity = (MobEntity) entity;

		try {
			if (slot.getSlotType() == EquipmentSlotType.Group.HAND) {
				return ((float[]) HANDS_CHANCES.get(livingEntity))[slot.getIndex()] > -1;
			} else {
				return ((float[]) ARMOR_CHANCES.get(livingEntity))[slot.getIndex()] > -1;
			}
		}
		catch (IllegalAccessException e) {
			LogHelper.error(e);
		}

		return false;
	}

	private static final Field HANDS_CHANCES = ObfuscationReflectionHelper.findField(MobEntity.class, "field_82174_bp");
	private static final Field ARMOR_CHANCES = ObfuscationReflectionHelper.findField(MobEntity.class, "field_184655_bs");

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
