package xreliquary.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class CauldronBubbleParticle extends SpriteTexturedParticle {
	private final IAnimatedSprite spriteSet;

	public CauldronBubbleParticle(World world, ColorParticleData particleData, double x, double y, double z, IAnimatedSprite spriteSet) {
		super(world, x, y, z, 0D, 0D, 0D);
		this.spriteSet = spriteSet;
		setSize(0.02F, 0.02F);
		particleScale = 0.5F + (world.rand.nextFloat() - 0.5F) * 0.4F;
		motionX = 0D;
		motionY = 0D;
		motionZ = 0D;
		particleRed = particleData.getRed();
		particleGreen = particleData.getGreen();
		particleBlue = particleData.getBlue();
		maxAge = 20;
		age = 0;
	}

	@Override
	public void tick() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		setAlphaF((float) age / (float) maxAge);

		if (age++ >= maxAge) {
			setExpired();
		} else {
			selectSpriteWithAge(spriteSet);
		}
	}

	@Override
	public float getScale(float partialTicks) {
		return 0.1F * particleScale * (1.0F + (float) age / 20F);
	}

	@Override
	public void selectSpriteWithAge(IAnimatedSprite animatedSprite) {
		int ageFromDeath = maxAge - age;
		if (ageFromDeath <= 6) {
			animatedSprite.get(4 - Math.max(ageFromDeath / 2, 1), maxAge);
		} else {
			animatedSprite.get(0, maxAge);
		}
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<ColorParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite spriteSet) {
			this.spriteSet = spriteSet;
		}

		@Nullable
		@Override
		public Particle makeParticle(ColorParticleData particleData, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			CauldronBubbleParticle particle = new CauldronBubbleParticle(world, particleData, x, y, z, spriteSet);
			particle.setSprite(spriteSet.get(particle.age, particle.maxAge));
			return particle;
		}
	}
}
