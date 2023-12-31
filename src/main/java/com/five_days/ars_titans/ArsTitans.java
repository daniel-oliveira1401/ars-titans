package com.five_days.ars_titans;

import com.five_days.ars_titans.helpers.SetInterval;
import com.five_days.ars_titans.registry.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArsTitans.MODID)
public class ArsTitans
{
    public static final String MODID = "ars_titans";

    private static final Logger LOGGER = LogManager.getLogger();

    public ArsTitans() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        //registries
        ModRegistry.registerRegistries(modbus);
        ArsNouveauRegistry.registerGlyphs();

        GeckoLib.initialize();

        modbus.addListener(this::setup);
        modbus.addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation prefix(String path){
        return new ResourceLocation(MODID, path);
    }


    private void setup(final FMLCommonSetupEvent event)
    {
        ArsNouveauRegistry.registerSounds();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        System.out.println("Server starting :)");
        LOGGER.info("HELLO from server starting");
    }

    public static void setInterval(Runnable method, int tickInterval, int timeToLive){
        MinecraftForge.EVENT_BUS.register(new SetInterval(method, tickInterval, timeToLive));
    }

    /*
    * Goal here: i want to be able to register and unregister the function that i will use to tick the
    * aura form.
    *
    * It seems there is no way to register and unregister a single function, only a full class. So
    * i could either create one class per listener, or create one class that has all the listeners.
    *
    *
    *
    * */




}
