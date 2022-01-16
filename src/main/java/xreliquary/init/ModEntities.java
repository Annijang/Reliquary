package xreliquary.init;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xreliquary.entities.EnderStaffProjectileEntity;
import xreliquary.entities.GlowingWaterEntity;
import xreliquary.entities.HolyHandGrenadeEntity;
import xreliquary.entities.KrakenSlimeEntity;
import xreliquary.entities.LyssaHook;
import xreliquary.entities.SpecialSnowballEntity;
import xreliquary.entities.XRTippedArrowEntity;
import xreliquary.entities.potion.AphroditePotionEntity;
import xreliquary.entities.potion.FertilePotionEntity;
import xreliquary.entities.potion.ThrownXRPotionEntity;
import xreliquary.entities.shot.BlazeShotEntity;
import xreliquary.entities.shot.BusterShotEntity;
import xreliquary.entities.shot.ConcussiveShotEntity;
import xreliquary.entities.shot.EnderShotEntity;
import xreliquary.entities.shot.ExorcismShotEntity;
import xreliquary.entities.shot.NeutralShotEntity;
import xreliquary.entities.shot.SandShotEntity;
import xreliquary.entities.shot.SeekerShotEntity;
import xreliquary.entities.shot.ShotEntityBase;
import xreliquary.entities.shot.StormShotEntity;
import xreliquary.reference.Reference;

public class ModEntities {
	private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Reference.MOD_ID);

	public static final RegistryObject<EntityType<AphroditePotionEntity>> APHRODITE_POTION = ENTITIES.register("aphrodite_potion", () -> getDefaultSizeEntityType(AphroditePotionEntity::new));
	public static final RegistryObject<EntityType<FertilePotionEntity>> FERTILE_POTION = ENTITIES.register("fertile_potion", () -> getDefaultSizeEntityType(FertilePotionEntity::new));
	public static final RegistryObject<EntityType<ThrownXRPotionEntity>> THROWN_POTION = ENTITIES.register("thrown_potion", () -> getDefaultSizeEntityType(ThrownXRPotionEntity::new));
	public static final RegistryObject<EntityType<BlazeShotEntity>> BLAZE_SHOT = ENTITIES.register("blaze_shot", () -> getShotEntityType(BlazeShotEntity::new));
	public static final RegistryObject<EntityType<BusterShotEntity>> BUSTER_SHOT = ENTITIES.register("buster_shot", () -> getShotEntityType(BusterShotEntity::new));
	public static final RegistryObject<EntityType<ConcussiveShotEntity>> CONCUSSIVE_SHOT = ENTITIES.register("concussive_shot", () -> getShotEntityType(ConcussiveShotEntity::new));
	public static final RegistryObject<EntityType<EnderShotEntity>> ENDER_SHOT = ENTITIES.register("ender_shot", () -> getShotEntityType(EnderShotEntity::new));
	public static final RegistryObject<EntityType<ExorcismShotEntity>> EXORCISM_SHOT = ENTITIES.register("exorcism_shot", () -> getShotEntityType(ExorcismShotEntity::new));
	public static final RegistryObject<EntityType<NeutralShotEntity>> NEUTRAL_SHOT = ENTITIES.register("neutral_shot", () -> getShotEntityType(NeutralShotEntity::new));
	public static final RegistryObject<EntityType<SandShotEntity>> SAND_SHOT = ENTITIES.register("sand_shot", () -> getShotEntityType(SandShotEntity::new));
	public static final RegistryObject<EntityType<SeekerShotEntity>> SEEKER_SHOT = ENTITIES.register("seeker_shot", () -> getShotEntityType(SeekerShotEntity::new));
	public static final RegistryObject<EntityType<StormShotEntity>> STORM_SHOT = ENTITIES.register("storm_shot", () -> getShotEntityType(StormShotEntity::new));
	public static final RegistryObject<EntityType<EnderStaffProjectileEntity>> ENDER_STAFF_PROJECTILE = ENTITIES.register("ender_staff_projectile", () -> getEntityType(EnderStaffProjectileEntity::new, 0.25F, 0.25F, 256));
	public static final RegistryObject<EntityType<GlowingWaterEntity>> GLOWING_WATER = ENTITIES.register("glowing_water", () -> getDefaultSizeEntityType(GlowingWaterEntity::new));
	public static final RegistryObject<EntityType<HolyHandGrenadeEntity>> HOLY_HAND_GRENADE = ENTITIES.register("holy_hand_grenade", () -> getDefaultSizeEntityType(HolyHandGrenadeEntity::new));
	public static final RegistryObject<EntityType<KrakenSlimeEntity>> KRAKEN_SLIME = ENTITIES.register("kraken_slime", () -> getDefaultSizeEntityType(KrakenSlimeEntity::new));
	public static final RegistryObject<EntityType<LyssaHook>> LYSSA_HOOK = ENTITIES.register("lyssa_hook", () -> getDefaultSizeEntityType(LyssaHook::new));
	public static final RegistryObject<EntityType<XRTippedArrowEntity>> TIPPED_ARROW = ENTITIES.register("tipped_arrow", () -> getDefaultSizeEntityType(XRTippedArrowEntity::new));
	public static final RegistryObject<EntityType<SpecialSnowballEntity>> SPECIAL_SNOWBALL = ENTITIES.register("special_snowball", () -> getEntityType(SpecialSnowballEntity::new, 0.01F, 0.01F));

	private ModEntities() {}

	public static void registerListeners(IEventBus modBus) {
		ENTITIES.register(modBus);
	}

	private static <T extends Entity> EntityType<T> getDefaultSizeEntityType(EntityType.EntityFactory<T> factory) {
		return getEntityType(factory, 0.25F, 0.25F);
	}

	private static <T extends ShotEntityBase> EntityType<T> getShotEntityType(EntityType.EntityFactory<T> factory) {
		return getEntityType(factory, 0.01F, 0.01F);
	}

	private static <T extends Entity> EntityType<T> getEntityType(EntityType.EntityFactory<T> factory, float width, float height) {
		return getEntityType(factory, width, height, 128);
	}

	private static <T extends Entity> EntityType<T> getEntityType(EntityType.EntityFactory<T> factory, float width, float height, int trackingRange) {
		return EntityType.Builder.of(factory, MobCategory.MISC)
				.sized(width, height).updateInterval(5).setTrackingRange(trackingRange).setShouldReceiveVelocityUpdates(true)
				.build("");
	}
}
