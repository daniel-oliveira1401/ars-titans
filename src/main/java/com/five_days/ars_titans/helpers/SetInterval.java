package com.five_days.ars_titans.helpers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/*
* Run the method passed to this every tickInterval number of ticks
*
* After timeToLive number of ticks have passed, unregister itself.
*
* */

public class SetInterval {

    int ticks = 0;
    Runnable method;
    int tickInterval = 0; //How many ticks have to pass before the method is called again
    int timeToLive = 0;

    public SetInterval(Runnable method, int tickInterval, int timeToLive){
        //function, tick rate, time to live
        this.method = method;
        this.tickInterval = tickInterval;
        this.timeToLive = timeToLive;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.START){

            //subtract 1 tickInterval from the time to live to account for the extra tick that runs when
            //unregistering the listener
            if(ticks >= (timeToLive - tickInterval)){
                //System.out.println("Time to unregister this listener, i guess ;-;");
                MinecraftForge.EVENT_BUS.unregister(this);
            }

            if(ticks % tickInterval == 0){
                //System.out.println("On tick event called :)");
                this.method.run();
            }
            ticks++;
        }
    }

    //TODO: implement an abort() function that will unregister this ahead of time

}

/*
* This class will auto unregister itself, so i could use it an a setInterval() that has expiration time.
*
* like, when the form is cast, call a function that will register a setInterval. The setInterval will receive
* an expiration time, tick rate and a function to call as parameters
*
* */
