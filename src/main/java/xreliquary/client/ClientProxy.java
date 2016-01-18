package xreliquary.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import xreliquary.blocks.tile.TileEntityMortar;
import xreliquary.client.render.*;
import xreliquary.common.CommonProxy;
import xreliquary.entities.*;
import xreliquary.entities.potion.*;
import xreliquary.entities.shot.*;
import xreliquary.event.ClientEventHandler;
import xreliquary.init.ModBlocks;
import xreliquary.init.ModItems;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();

    }

    @Override
    public void init() {
        super.init();
        FMLCommonHandler.instance().bus().register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        this.registerRenderers();
        ModBlocks.initModels();
        ModItems.initModels();
    }


    public void registerRenderers() {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        //TODO:replace deprecated call
        RenderingRegistry.registerEntityRenderingHandler( EntityBlazeShot.class, new RenderShot(renderManager) );
        RenderingRegistry.registerEntityRenderingHandler(EntityBusterShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityConcussiveShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityExorcismShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityNeutralShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySeekerShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntitySandShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityStormShot.class, new RenderShot(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowingWater.class, new RenderThrown(renderManager,0));
        RenderingRegistry.registerEntityRenderingHandler(EntityAttractionPotion.class, new RenderThrownAttractionPotion(renderManager,renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityFertilePotion.class, new RenderThrownFertilePotion(renderManager, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityHolyHandGrenade.class, new RenderThrown(renderManager, 12));
        RenderingRegistry.registerEntityRenderingHandler(EntityKrakenSlime.class, new RenderThrown(renderManager, 13));
        RenderingRegistry.registerEntityRenderingHandler(EntitySpecialSnowball.class, new RenderSnowball(renderManager, Items.snowball, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderStaffProjectile.class, new RenderSnowball(renderManager, Items.ender_pearl, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownXRPotion.class, new RenderThrownXRPotion(renderManager, renderItem));

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMortar.class, new RenderApothecaryMortar());

        //TODO: add rendering for these custom items
        // MinecraftForgeClient.registerItemRenderer(ItemBlock.getItemFromBlock(Reliquary.CONTENT.getBlock(Names.apothecary_mortar)), new ItemRendererApothecaryMortar());
        // MinecraftForgeClient.registerItemRenderer(Reliquary.CONTENT.getItem(Names.handgun), new ItemRendererHandgun());
    }

}
