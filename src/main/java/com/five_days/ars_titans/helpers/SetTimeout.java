package com.five_days.ars_titans.helpers;

/*
*
* This helper will call the method provided after tickDelay number of ticks have passed
*
* */

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SetTimeout {

    int ticks = 0;
    Runnable method;
    int tickDelay;

    public SetTimeout(Runnable method, int tickDelay){
        this.method = method;
        this.tickDelay = tickDelay;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.START){


            if(ticks >= tickDelay){
                System.out.println("time to run the method");
                MinecraftForge.EVENT_BUS.unregister(this);
                this.method.run();
            }
            ticks++;
        }
    }

}
