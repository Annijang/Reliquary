package xreliquary;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import slimeknights.tconstruct.tools.item.BroadSword;
import slimeknights.tconstruct.tools.item.Cleaver;
import xreliquary.common.CommonProxy;
import xreliquary.compat.ICompat;
import xreliquary.handler.ConfigurationHandler;
import xreliquary.handler.config.PotionConfiguration;
import xreliquary.init.*;
import xreliquary.items.ItemRendingGale;
import xreliquary.network.PacketHandler;
import xreliquary.reference.Compatibility;
import xreliquary.reference.Reference;
import xreliquary.reference.Settings;
import xreliquary.util.LogHelper;
import xreliquary.util.alkahestry.AlkahestCraftRecipe;
import xreliquary.util.pedestal.*;

//@ModstatInfo(prefix = "reliquary")
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS, dependencies = Reference.DEPENDENCIES)
public class Reliquary {

	@Instance(Reference.MOD_ID)
	public static Reliquary INSTANCE;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.COMMON_PROXY)
	public static CommonProxy PROXY;

	public static CreativeTabs CREATIVE_TAB = new CreativeTabXR(CreativeTabs.getNextID(), Reference.MOD_ID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigurationHandler.init(event.getSuggestedConfigurationFile());

		PROXY.initColors();

		ModBlocks.init();

		ModItems.init();

		ModFluids.init();

		ModLoot.init();

		ModPotions.init();

		ModCapabilities.init();

		PROXY.preInit();

		//TODO figure out a better way to handle this if possible
		PotionConfiguration.loadPotionMap();

		PROXY.initPotionsJEI();

		PacketHandler.init();

		ModCompat.registerModCompat();
		ModCompat.loadCompat(ICompat.InitializationPhase.PRE_INIT, null);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		//TODO: put modstats back in when ready
		//Modstats.instance().getReporter().registerMod(this);

		PROXY.init();
		MinecraftForge.EVENT_BUS.register(this);

		ModCompat.loadCompat(ICompat.InitializationPhase.INIT, null);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		PROXY.postInit();

		ConfigurationHandler.postInit();

		ModCompat.loadCompat(ICompat.InitializationPhase.POST_INIT, null);

		ModFluids.postInit();

		PedestalRegistry.registerItemWrapper(ItemSword.class, PedestalMeleeWeaponWrapper.class);
		PedestalRegistry.registerItemWrapper(ItemBucket.class, PedestalBucketWrapper.class);
		PedestalRegistry.registerItemWrapper(ItemShears.class, PedestalShearsWrapper.class);
		PedestalRegistry.registerItemWrapper(ItemRendingGale.class, PedestalRendingGaleWrapper.class);
		if(Loader.isModLoaded(Compatibility.MOD_ID.TINKERS_CONSTRUCT)) {
			PedestalRegistry.registerItemWrapper(Cleaver.class, PedestalMeleeWeaponWrapper.class);
			PedestalRegistry.registerItemWrapper(BroadSword.class, PedestalMeleeWeaponWrapper.class);
			//not implemented currently in TiCon
			//PedestalRegistry.registerItemWrapper(BattleAxe.class, new PedestalMeleeWeaponWrapper());
			//PedestalRegistry.registerItemWrapper(Scythe.class, new PedestalMeleeWeaponWrapper());
		}

		LogHelper.info("Loaded successfully!");
	}

	@EventHandler
	public void onMessage(IMCEvent event) {
		for(IMCMessage message : event.getMessages()) {
			if(message.key.equals("Alkahest")) {
				NBTTagCompound tag = message.getNBTValue();
				if(tag != null && ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item")) != null && tag.hasKey("yield") && tag.hasKey("cost")) {
					if(tag.hasKey("dictionaryName"))
						Settings.AlkahestryTome.craftingRecipes.put("OreDictionary:" + tag.getString("dictionaryName"), new AlkahestCraftRecipe(tag.getString("dictionaryName"), tag.getInteger("yield"), tag.getInteger("cost")));
					else
						Settings.AlkahestryTome.craftingRecipes.put(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item")).getItem().getRegistryName(), new AlkahestCraftRecipe(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item")), tag.getInteger("yield"), tag.getInteger("cost")));
					LogHelper.info("[IMC] Added AlkahestRecipe ID: " + Item.itemRegistry.getNameForObject(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item")).getItem()) + " from " + message.getSender() + " to registry.");
				} else {
					LogHelper.warn("[IMC] Invalid AlkahestRecipe from " + message.getSender() + "! Please contact the mod author if you see this error occurring.");
				}
			}
		}
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		//event.registerServerCommand(new CommandGenLootChest());
	}
}