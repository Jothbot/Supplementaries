package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ModSetup {

    public static void init(final FMLClientSetupEvent event){

        Dispenser.registerBehaviors();
    }
}