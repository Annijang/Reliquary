package xreliquary.client;

import lib.enderwizards.sandstone.util.LanguageHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import xreliquary.blocks.tile.TileEntityMortar;
import xreliquary.client.init.ItemBlockModels;
import xreliquary.client.init.ItemModels;
import xreliquary.client.render.*;
import xreliquary.common.CommonProxy;
import xreliquary.entities.*;
import xreliquary.entities.potion.*;
import xreliquary.entities.shot.*;
import xreliquary.event.ClientEventHandler;
import xreliquary.init.ModBlocks;
import xreliquary.init.ModItems;

//TODO refactor proxy so that it has functionally named methods that get called from main mod class
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();

        ItemModels.registerItemModels();
        ItemBlockModels.registerItemBlockModels();
    }

    @Override
    public void init() {
        super.init();
        FMLCommonHandler.instance().bus().register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        this.registerRenderers();
    }

    public void registerRenderers() {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        //TODO:replace deprecated call
        RenderingRegistry.registerEntityRenderingHandler(EntityBlazeShot.class, new RenderShot<EntityBlazeShot>(renderManager) );
        RenderingRegistry.registerEntityRenderingHandler(EntityBusterShot.class, new RenderShot<EntityBusterShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityConcussiveShot.class, new RenderShot<EntityConcussiveShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderShot.class, new RenderShot<EntityEnderShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityExorcismShot.class, new RenderShot<EntityExorcismShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityNeutralShot.class, new RenderShot<EntityNeutralShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySeekerShot.class, new RenderShot<EntitySeekerShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySandShot.class, new RenderShot<EntitySandShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityStormShot.class, new RenderShot<EntityStormShot>(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowingWater.class, new RenderThrown<EntityGlowingWater>(renderManager,ModItems.glowingWater, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityAttractionPotion.class, new RenderThrown<EntityAttractionPotion>(renderManager, ModItems.attractionPotion, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityFertilePotion.class, new RenderThrown<EntityFertilePotion>(renderManager, ModItems.fertilePotion, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityHolyHandGrenade.class, new RenderThrown<EntityHolyHandGrenade>(renderManager, ModItems.holyHandGrenade, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityKrakenSlime.class, new RenderThrownKrakenSlime(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpecialSnowball.class, new RenderSnowball<EntitySpecialSnowball>(renderManager, Items.snowball, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderStaffProjectile.class, new RenderSnowball<EntityEnderStaffProjectile>(renderManager, Items.ender_pearl, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownXRPotion.class, new RenderThrownXRPotion(renderManager, renderItem));

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMortar.class, new RenderApothecaryMortar());
    }

    @Override
    public void initColors() {
         /* Unicode colors that you can use in the tooltips/names lang files.
         Use by calling {{!name}}, with name being the name being colors.color. */
        LanguageHelper.globals.put("colors.black", "\u00A70");
        LanguageHelper.globals.put("colors.navy", "\u00A71");
        LanguageHelper.globals.put("colors.green", "\u00A72");
        LanguageHelper.globals.put("colors.blue", "\u00A73");
        LanguageHelper.globals.put("colors.red", "\u00A74");
        LanguageHelper.globals.put("colors.purple", "\u00A75");
        LanguageHelper.globals.put("colors.gold", "\u00A76");
        LanguageHelper.globals.put("colors.light_gray", "\u00A77");
        LanguageHelper.globals.put("colors.gray", "\u00A78");
        LanguageHelper.globals.put("colors.dark_purple", "\u00A79");
        LanguageHelper.globals.put("colors.light_green", "\u00A7a");
        LanguageHelper.globals.put("colors.light_blue", "\u00A7b");
        LanguageHelper.globals.put("colors.rose", "\u00A7c");
        LanguageHelper.globals.put("colors.light_purple", "\u00A7d");
        LanguageHelper.globals.put("colors.yellow", "\u00A7e");
        LanguageHelper.globals.put("colors.white", "\u00A7f");
        LanguageHelper.globals.put("colors.reset", EnumChatFormatting.RESET.toString());

    }
}
