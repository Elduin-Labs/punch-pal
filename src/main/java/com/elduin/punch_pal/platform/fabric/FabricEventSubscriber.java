package com.elduin.punch_pal.platform.fabric;

//? fabric {

import com.elduin.punch_pal.entity.ModEntities;
import net.minecraft.world.item.CreativeModeTabs;
//? if >=26 {
/*import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
*///? } else {
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
//? }

public class FabricEventSubscriber {

	public static void registerEvents() {
		// Fabric API renamed "item groups" to "creative mode tabs" in 26.
		//? if >=26 {
		/*CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
				.register(output -> output.accept(ModEntities.PUNCH_PAL_SPAWN_EGG));
		*///? } else {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
				.register(entries -> entries.accept(ModEntities.PUNCH_PAL_SPAWN_EGG));
		//? }
	}
}
//?}
