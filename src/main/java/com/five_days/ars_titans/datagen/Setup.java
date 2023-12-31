package com.five_days.ars_titans.datagen;

import com.five_days.ars_titans.ArsTitans;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = ArsTitans.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Setup {

    //use runData configuration to generate stuff, event.includeServer() for data, event.includeClient() for assets
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();

        gen.addProvider(event.includeServer(), new ArsProviders.ImbuementProvider(gen));
        gen.addProvider(event.includeServer(), new ArsProviders.GlyphProvider(gen));
        gen.addProvider(event.includeServer(), new ArsProviders.EnchantingAppProvider(gen));

        gen.addProvider(event.includeServer(), new ArsProviders.PatchouliProvider(gen));
    }

    //what tells which event this is going to listen for is the method's parameter FMLConstructModEvent
    //for example, the function above uses the GatherDataEvent as parameter, which means
    //that method will listen to the GatherDataEvent. This is a brand new way of listening
    //to events for me. I had never see this before. It could be that im just a newbie e.e
    @SubscribeEvent
    public static void myLifeCycleTest(FMLConstructModEvent event){
        System.out.println("Oh look. this is an event of mod construct :)");
    }



}
