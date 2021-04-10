package xreliquary.entities.shot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import xreliquary.reference.Settings;
import xreliquary.util.potions.XRPotionHelper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("squid:S2160")
public abstract class ShotEntityBase extends ProjectileEntity {
	private static final DataParameter<Byte> CRITICAL = EntityDataManager.createKey(ShotEntityBase.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> COLOR = EntityDataManager.createKey(ShotEntityBase.class, DataSerializers.VARINT);
	private List<EffectInstance> potionEffects = Collections.emptyList();

	/**
	 * The owner of this arrow.
	 */
	protected int ticksInAir = 0;

	private int ricochetCounter = 0;
	private boolean scheduledForDeath = false;

	protected <T extends ShotEntityBase> ShotEntityBase(EntityType<T> entityType, World world) {
		super(entityType, world);
	}

	protected <T extends ShotEntityBase> ShotEntityBase(EntityType<T> entityType, World world, PlayerEntity player, Hand hand) {
		this(entityType, world);
		setShooter(player);
		setLocationAndAngles(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ(), player.rotationYaw, player.rotationPitch);
		setPosition(
				getPosX() - MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * (hand == Hand.MAIN_HAND ? 1 : -1) * 0.16F,
				getPosY() - 0.2D,
				getPosZ() - MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * (hand == Hand.MAIN_HAND ? 1 : -1) * 0.16F
		);
	}

	protected Optional<PlayerEntity> getShooterPlayer() {
		return Optional.ofNullable((PlayerEntity) func_234616_v_());
	}

	@Override
	protected void registerData() {
		dataManager.register(CRITICAL, (byte) 0);
		dataManager.register(COLOR, 0);
	}

	public ShotEntityBase addPotionEffects(List<EffectInstance> effects) {
		if (!effects.isEmpty()) {
			potionEffects = effects;
			dataManager.set(COLOR, PotionUtils.getPotionColorFromEffectList(effects));
		}

		return this;
	}

	@Override
	public void shoot(double motionX, double motionY, double motionZ, float velocity, float inaccuracy) {
		float var9 = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		motionX /= var9;
		motionY /= var9;
		motionZ /= var9;
		motionX += rand.nextGaussian() * 0.0075D * inaccuracy;
		motionY += rand.nextGaussian() * 0.0075D * inaccuracy;
		motionZ += rand.nextGaussian() * 0.0075D * inaccuracy;
		motionX *= velocity;
		motionY *= velocity;
		motionZ *= velocity;
		setMotion(motionX, motionY, motionZ);
		float var10 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
		prevRotationYaw = rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
		prevRotationPitch = rotationPitch = (float) (Math.atan2(motionY, var10) * 180.0D / Math.PI);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		setPosition(x, y, z);
		setRotation(yaw, pitch);
	}

	/**
	 * Sets the velocity to the args. Args: x, y, z
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void setVelocity(double motionX, double motionY, double motionZ) {
		setMotion(motionX, motionY, motionZ);

		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
			float var7 = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
			prevRotationYaw = rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
			prevRotationPitch = rotationPitch = (float) (Math.atan2(motionY, var7) * 180.0D / Math.PI);
			setLocationAndAngles(getPosX(), getPosY(), getPosZ(), rotationYaw, rotationPitch);
		}
	}

	/**
	 * Called to update the entity's position/logic. Special snippets of the
	 * usual projectile code have been removed so they can be handled manually
	 * in the onImpact methods of the base shot.
	 */
	@Override
	public void tick() {
		super.tick();
		if (ticksInAir > 200) {
			remove();
		}

		if (world.isRemote) {
			spawnPotionParticles();
		}
		Vector3d motionVec = getMotion();
		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
			float pythingy = MathHelper.sqrt(motionVec.getX() * motionVec.getX() + motionVec.getZ() * motionVec.getZ());
			prevRotationYaw = rotationYaw = (float) (Math.atan2(motionVec.getX(), motionVec.getZ()) * 180.0D / Math.PI);
			prevRotationPitch = rotationPitch = (float) (Math.atan2(motionVec.getY(), pythingy) * 180.0D / Math.PI);
		}

		++ticksInAir;
		if (ticksInAir == 2) {
			world.addParticle(ParticleTypes.FLAME, getPosX() + smallGauss(0.1D), getPosY() + smallGauss(0.1D), getPosZ() + smallGauss(0.1D), 0D, 0D, 0D);
			for (int particles = 0; particles < 3; particles++) {
				doFiringEffects();
			}

		} else {
			doFlightEffects();
		}

		Vector3d posVector = new Vector3d(getPosX(), getPosY(), getPosZ());
		Vector3d approachVector = new Vector3d(getPosX() + motionVec.getX(), getPosY() + motionVec.getY(), getPosZ() + motionVec.getZ());

		RayTraceResult objectStruckByVector = world.rayTraceBlocks(new RayTraceContext(posVector, approachVector, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));

		Entity hitEntity = null;
		List<Entity> struckEntitiesInAABB = world.getEntitiesWithinAABBExcludingEntity(this, getBoundingBox().expand(motionVec).grow(1.0D, 1.0D, 1.0D));
		double var7 = 0.0D;
		Iterator<Entity> struckEntityIterator = struckEntitiesInAABB.iterator();
		float var11;

		while (struckEntityIterator.hasNext()) {
			Entity struckEntity = struckEntityIterator.next();
			if (struckEntity.canBeCollidedWith() && (struckEntity != func_234616_v_() || ticksInAir >= 5)) {
				var11 = 0.5F;
				AxisAlignedBB var12 = struckEntity.getBoundingBox().grow(var11, var11, var11);
				Optional<Vector3d> hitResult = var12.rayTrace(posVector, approachVector);

				if (hitResult.isPresent()) {
					double var14 = posVector.distanceTo(hitResult.get());

					if (var14 < var7 || var7 == 0.0D) {
						hitEntity = struckEntity;
						var7 = var14;
					}
				}
			}
		}

		if (hitEntity != null) {
			objectStruckByVector = new EntityRayTraceResult(hitEntity);
		}

		//noinspection ConstantConditions - world.rayTraceBlocks can still produce null under certain conditions
		if (objectStruckByVector != null) {
			applyPotionEffects(objectStruckByVector);
			onImpact(objectStruckByVector);
		}

		if (scheduledForDeath) {
			remove();
		}

		Vector3d newPos = getPositionVec().add(getMotion());
		setPosition(newPos.x, newPos.y, newPos.z);
	}

	private void spawnPotionParticles() {
		int color = getColor();

		if (color != 0) {
			double d0 = (double) (color >> 16 & 255) / 255.0D;
			double d1 = (double) (color >> 8 & 255) / 255.0D;
			double d2 = (double) (color & 255) / 255.0D;

			for (int j = 0; j < 2; ++j) {
				world.addParticle(ParticleTypes.ENTITY_EFFECT, getPosX() + (rand.nextDouble() - 0.5D) * (double) getWidth(), getPosY() + rand.nextDouble() * (double) getHeight(), getPosZ() + (rand.nextDouble() - 0.5D) * (double) getWidth(), d0, d1, d2);
			}
		}
	}

	public int getColor() {
		return dataManager.get(COLOR);
	}

	private void applyPotionEffects(RayTraceResult objectStruckByVector) {
		if (objectStruckByVector.getType() == RayTraceResult.Type.ENTITY) {
			EntityRayTraceResult entityStruckResult = ((EntityRayTraceResult) objectStruckByVector);
			if (entityStruckResult.getEntity() instanceof LivingEntity && potionEffects != null && !potionEffects.isEmpty()) {
				LivingEntity living = (LivingEntity) entityStruckResult.getEntity();
				XRPotionHelper.applyEffectsToEntity(potionEffects, this, func_234616_v_(), living);
			}
		}
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		potionEffects = XRPotionHelper.getPotionEffectsFromCompoundTag(compound);
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		XRPotionHelper.addPotionEffectsToCompoundTag(compound, potionEffects);
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = 64.0D;
		return distance < d0 * d0;
	}

	/**
	 * If returns false, the item will not inflict any damage against entities.
	 */
	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	// these are just simulated dice rolls, they make it slightly easier to
	// adjust damage
	int d3() {
		return rand.nextInt(3) + 1;
	}

	int d6() {
		return rand.nextInt(6) + 1;
	}

	int d12() {
		return rand.nextInt(12) + 1;
	}

	void doDamage(LivingEntity e) {
		// minor modification here, the shots are quite strong
		// so I've made it so they only do half damage against player entities.
		e.attackEntityFrom(getDamageSource(), (e instanceof PlayerEntity ? 0.5F : 1F) * adjustDamageForPotionShots(getDamageOfShot(e)));
	}

	private float adjustDamageForPotionShots(int damageOfShot) {
		return potionEffects != null && !potionEffects.isEmpty() ? 4 : damageOfShot; //setting the cap to damage 4 for potion shots
	}

	protected void spawnMotionBasedParticle(IParticleData particleData) {
		spawnMotionBasedParticle(particleData, getPosY());
	}

	protected void spawnMotionBasedParticle(IParticleData particleData, double y) {
		Vector3d motion = getMotion();
		world.addParticle(particleData, getPosX(), y, getPosZ(), gaussian(motion.getX()), gaussian(motion.getY()), gaussian(motion.getZ()));
	}

	protected DamageSource getDamageSource() {
		return new IndirectEntityDamageSource("bullet", this, func_234616_v_());
	}

	protected void groundImpact(Direction sideHit) {
		ricochet(sideHit);
	}

	/**
	 * @param d is the factor of the double
	 * @return a negative or positive value with limits of 50% of d
	 */
	protected double smallGauss(double d) {
		return (world.rand.nextFloat() - 0.5D) * d;
	}

	/**
	 * @param d is the factor of the double
	 * @return a positive value between 0% and 50% of d
	 */
	double posGauss(double d) {
		return rand.nextFloat() * 0.5D * d;
	}

	/**
	 * @param d haphazardly determines the upper bounds of the [always
	 *          positive] gaussian
	 * @return a [comparatively] normal gaussian ranging from 75% to 125% of the
	 * parameter d
	 */
	double gaussian(double d) {
		return d + d * ((rand.nextFloat() - 0.5D) / 4);
	}

	/**
	 * @param d haphazardly determines the upper bounds of the [always
	 *          positive] gaussian
	 * @return a [comparatively] low gaussian ranging from 25% to 75% of the
	 * parameter d
	 */
	double lowGauss(double d) {
		return d - d * (rand.nextFloat() / 4 + 0.5);
	}

	/**
	 * Handles the ricochet "event", more or less determines when the entity
	 * does or doesn't ricochet. If the ricochet limit is set to 0, it will
	 * still "ricochet" once, but will immediately self destruct by calling its
	 * burstEffect and setting itself to dead.
	 */
	private void ricochet(Direction sideHit) {
		switch (sideHit) {
			case DOWN:
			case UP:
				setMotion(getMotion().mul(1, -1, 1));
				break;
			case WEST:
			case EAST:
				setMotion(getMotion().mul(-1, 1, 1));
				break;
			case SOUTH:
			case NORTH:
				setMotion(getMotion().mul(1, 1, -1));
				break;
		}
		ricochetCounter++;
		if (ricochetCounter > getRicochetMax()) {
			doBurstEffect(sideHit);
			scheduledForDeath = true;
			for (int particles = 0; particles < 4; particles++) {
				switch (sideHit) {
					case DOWN:
						world.addParticle(ParticleTypes.SMOKE, getPosX(), getPosY(), getPosZ(), gaussian(0.1D), -gaussian(0.1D), gaussian(0.1D));
						break;
					case UP:
					case SOUTH:
					case EAST:
						world.addParticle(ParticleTypes.SMOKE, getPosX(), getPosY(), getPosZ(), gaussian(0.1D), gaussian(0.1D), gaussian(0.1D));
						break;
					case NORTH:
						world.addParticle(ParticleTypes.SMOKE, getPosX(), getPosY(), getPosZ(), gaussian(0.1D), gaussian(0.1D), -gaussian(0.1D));
						break;
					case WEST:
						world.addParticle(ParticleTypes.SMOKE, getPosX(), getPosY(), getPosZ(), -gaussian(0.1D), gaussian(0.1D), gaussian(0.1D));
						break;
				}
			}
		}
	}

	/*
	 * custom seek method, currently only being used in seeker and ender shots.
	 * it could be better, but it works. As of writing this, both shots share
	 * the same formulas.
	 */
	void seekTarget() {
		Entity closestTarget = null;
		List<String> huntableEntitiesBlacklist = Settings.COMMON.items.seekerShot.huntableEntitiesBlacklist.get();
		List<Entity> targetsList = world.getEntitiesInAABBexcluding(this,
				new AxisAlignedBB(getPosX() - 5, getPosY() - 5, getPosZ() - 5, getPosX() + 5, getPosY() + 5, getPosZ() + 5),
				e -> e instanceof MobEntity);
		Iterator<Entity> iTarget = targetsList.iterator();
		double closestDistance = Double.MAX_VALUE;
		while (iTarget.hasNext()) {
			Entity currentTarget = iTarget.next();

			//noinspection ConstantConditions
			String entityName = currentTarget.getType().getRegistryName().toString();
			if (huntableEntitiesBlacklist.contains(entityName) || (currentTarget == func_234616_v_()) || (!currentTarget.isAlive()
					|| (currentTarget instanceof LivingEntity && ((LivingEntity) currentTarget).getHealth() <= 0))) {
				continue;
			}
			// goes for the closest thing it can
			if (getDistance(currentTarget) < closestDistance) {
				closestDistance = getDistance(currentTarget);
				closestTarget = currentTarget;
			}
		}
		// these are extremely touchy, tune them lightly.
		if (closestTarget != null && func_234616_v_() != null) {
			double x = closestTarget.getBoundingBox().minX + closestTarget.getBoundingBox().maxX;
			x /= 2D;
			double y = closestTarget.getBoundingBox().minY + Math.max(closestTarget.getYOffset(), closestTarget.getHeight());
			y -= closestTarget.getHeight() / 2D;
			double z = closestTarget.getBoundingBox().minZ + closestTarget.getBoundingBox().maxZ;
			z /= 2D;
			double trueX = getBoundingBox().minX + getBoundingBox().maxX;
			trueX /= 2D;
			double trueY = getBoundingBox().minY + getYOffset();
			trueY -= getHeight() / 2D;
			double trueZ = getBoundingBox().minZ + getBoundingBox().maxZ;
			trueZ /= 2D;
			Vector3d seekVector = new Vector3d(x - trueX, y - trueY, z - trueZ);
			seekVector = seekVector.normalize();
			setMotion(seekVector.mul(0.4D, 0.4D, 0.4D));

			if (world.isRemote) {
				setVelocity(getMotion().getX(), getMotion().getY(), getMotion().getZ());
			}
		}
	}

	/**
	 * Determines the maximum number of bounces a bullet gets before burstEffect
	 * + dying. 0 means it is destroyed the moment it makes impact. This only
	 * matters if you actually call "groundImpact" from your onImpact(MOP)
	 * method.
	 */
	abstract int getRicochetMax();

	/**
	 * @param e an optional parameter, some shots need to know what entity
	 *          they're hitting for damage counts
	 * @return the int of damage the shot should deal. Most of these use my "dX"
	 * methods of this class to randomize damage, to a degree.
	 */
	abstract int getDamageOfShot(LivingEntity e);

	/**
	 * Additional entity impact effects should go here
	 *
	 * @param entityLiving the entity being struck
	 */
	protected void onImpact(LivingEntity entityLiving) {
		if (!world.isRemote) {
			if (entityLiving != func_234616_v_() || ticksInAir > 3) {
				doDamage(entityLiving);
			}
			spawnHitParticles(8);
			scheduledForDeath = true;
		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult) result).getEntity();
			if (entity == func_234616_v_() || !(entity instanceof LivingEntity)) {
				return;
			}
			onImpact((LivingEntity) entity);
		} else if (result.getType() == RayTraceResult.Type.BLOCK) {
			groundImpact(((BlockRayTraceResult) result).getFace());
		}
	}

	/**
	 * This is the effect called when the shot reaches the ground and has no
	 * ricochets remaining. It can also be called at any time by hooking into
	 * flight/firing effects.
	 *
	 * @param sideHit is sometimes used when you need particles to fly in a certain
	 *                direction.
	 */
	abstract void doBurstEffect(Direction sideHit);

	/**
	 * The particle/effect window when the gun is initially fired, between
	 * "ticksAlive" 1 and 3.
	 */
	abstract void doFiringEffects();

	/**
	 * The particle/effect window any time after the initial firing window of
	 * doFiringEffects. (ticksAlive >= 3)
	 */
	abstract void doFlightEffects();

	/**
	 * The particles which spawn when the bullet impacts an entity.
	 *
	 * @param i the number of times you want the inner for loop to produce a
	 *          single particle at random velocity
	 */
	abstract void spawnHitParticles(int i);

	// used by the renderer to pull the shot texture directly from the entity.
	// This might not work.
	public abstract ResourceLocation getShotTexture();

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
