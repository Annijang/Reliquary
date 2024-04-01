package reliquary.entities;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import reliquary.init.ModEntities;
import reliquary.init.ModItems;
import reliquary.network.PacketHandler;
import reliquary.network.SpawnThrownPotionImpactParticlesPacket;
import reliquary.reference.Colors;

import java.util.List;

public class GlowingWaterEntity extends ThrowableProjectile implements ItemSupplier {
	public GlowingWaterEntity(EntityType<GlowingWaterEntity> entityType, Level world) {
		super(entityType, world);
	}

	public GlowingWaterEntity(Level world, Player player) {
		super(ModEntities.GLOWING_WATER.get(), player, world);
	}

	public GlowingWaterEntity(Level world, double x, double y, double z) {
		super(ModEntities.GLOWING_WATER.get(), x, y, z, world);
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	@Override
	protected float getGravity() {
		return 0.05F;
	}

	private boolean isUndead(LivingEntity e) {
		return e.getMobType() == MobType.UNDEAD;
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	@Override
	protected void onHit(HitResult result) {
		if (!level().isClientSide) {
			spawnParticles();
			AABB bb = getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
			List<Mob> eList = level().getEntitiesOfClass(Mob.class, bb);
			eList.stream().filter(this::isUndead).forEach(e -> {
				float amount = 18f + random.nextInt(17);
				Entity thrower = getOwner();
				if (thrower instanceof Player player) {
					e.hurt(damageSources().playerAttack(player), amount);
				} else {
					e.hurt(damageSources().magic(), amount);
				}
			});

			level().levelEvent(2002, blockPosition(), 0);
			discard();
		}
	}

	private void spawnParticles() {
		double x = getX();
		double y = getY();
		double z = getZ();

		ItemParticleOption itemParticleData = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.GLOWING_WATER.get()));
		for (int particleNum = 0; particleNum < 8; ++particleNum) {
			level().addParticle(itemParticleData, x, y, z, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
		}

		level().playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, level().random.nextFloat() * 0.1F + 0.9F);
		PacketHandler.sendToAllNear(this, new SpawnThrownPotionImpactParticlesPacket(Colors.get(Colors.BLUE), getX(), getY(), getZ()), 32.0D);
	}

	@Override
	protected void defineSynchedData() {
		//noop
	}

	@Override
	public ItemStack getItem() {
		return new ItemStack(ModItems.GLOWING_WATER.get());
	}
}
