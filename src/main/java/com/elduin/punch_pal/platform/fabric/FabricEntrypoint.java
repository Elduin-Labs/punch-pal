package com.elduin.punch_pal.platform.fabric;

//? fabric {

import com.elduin.punch_pal.PunchPal;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		PunchPal.onInitialize();
		FabricEventSubscriber.registerEvents();
	}
}
//?}
