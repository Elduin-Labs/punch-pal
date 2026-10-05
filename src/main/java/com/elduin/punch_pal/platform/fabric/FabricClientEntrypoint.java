package com.elduin.punch_pal.platform.fabric;

//? fabric {

import com.elduin.punch_pal.PunchPal;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		PunchPal.onInitializeClient();
	}

}
//?}
