package xreliquary.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import xreliquary.Reliquary;
import xreliquary.common.gui.GUIHandler;
import xreliquary.entities.*;
import xreliquary.entities.potion.EntityAttractionPotion;
import xreliquary.entities.potion.EntityFertilePotion;
import xreliquary.entities.potion.EntityThrownXRPotion;
import xreliquary.entities.shot.*;
import xreliquary.handler.CommonEventHandler;
import xreliquary.handler.ConfigurationHandler;
import xreliquary.init.*;

public class CommonProxy {

	//TODO: rewrite proxy to the EE style so that it has area specific method names rather than generic preInit/init/postInit
	public void preInit() {
		try {
			XRRecipes.init();

			ModBlocks.initTileEntities();
		}
		catch(Exception e) {
			e.printStackTrace();
			FMLCommonHandler.instance().raiseException(e, "Reliquary failed to initiate recipes.", true);
		}
	}

	public void init() {
		MinecraftForge.EVENT_BUS.register(new ConfigurationHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(Reliquary.INSTANCE, new GUIHandler());
		MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
		MinecraftForge.EVENT_BUS.register(new ModCompat());
		MinecraftForge.EVENT_BUS.register(new ModCapabilities());
		MinecraftForge.EVENT_BUS.register(new ModLoot());

		this.registerEntities();
	}

	public void postInit() {

	}

	private void registerEntities() {
		EntityRegistry.registerModEntity(EntityHolyHandGrenade.class, "entityHGrenade", 0, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityGlowingWater.class, "entityHolyWater", 1, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntitySpecialSnowball.class, "entitySpecialSnowball", 2, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityNeutralShot.class, "entityNeutralShot", 3, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityExorcismShot.class, "entityExorcismShot", 4, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityBlazeShot.class, "entityBlazeShot", 5, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityEnderShot.class, "entityEnderShot", 6, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityConcussiveShot.class, "entityConcussiveShot", 7, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityBusterShot.class, "entityBusterShot", 8, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntitySeekerShot.class, "entitySeekerShot", 9, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntitySandShot.class, "entitySandShot", 10, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityStormShot.class, "entityStormShot", 11, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityAttractionPotion.class, "entitySplashAphrodite", 12, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityThrownXRPotion.class, "entityThrownXRPotion", 13, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityFertilePotion.class, "entitySplashFertility", 21, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityKrakenSlime.class, "entityKSlime", 22, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityEnderStaffProjectile.class, "entityEnderStaffProjectile", 23, Reliquary.INSTANCE, 128, 5, true);
		EntityRegistry.registerModEntity(EntityXRTippedArrow.class, "entityTippedArrow", 24, Reliquary.INSTANCE, 128, 5, true);
	}

	public void initColors() {
	}

	public void registerJEI(Item item, String name) {
	}

	public void registerJEI(Block block, String name) {
	}
	public void registerJEI(Block block, String name, boolean oneDescription) {

	}
	public void initPotionsJEI() {
	}
}
