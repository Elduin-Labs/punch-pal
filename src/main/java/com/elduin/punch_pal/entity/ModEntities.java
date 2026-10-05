package com.elduin.punch_pal.entity;

import com.elduin.punch_pal.PunchPal;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModEntities {

	public static final ResourceKey<EntityType<?>> PUNCH_PAL_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, PunchPal.id("punch_pal"));

	// The size of a player. It only comes from the spawn egg, so there is no spawn rule.
	public static final EntityType<PunchPalEntity> PUNCH_PAL = Registry.register(BuiltInRegistries.ENTITY_TYPE, PUNCH_PAL_KEY,
			FabricEntityType.Builder.createMob(PunchPalEntity::new, MobCategory.CREATURE, mob -> mob
							.defaultAttributes(PunchPalEntity::createAttributes))
					.sized(0.6F, 1.8F)
					.clientTrackingRange(10)
					.build(PUNCH_PAL_KEY));

	public static final ResourceKey<Item> SPAWN_EGG_KEY =
			ResourceKey.create(Registries.ITEM, PunchPal.id("punch_pal_spawn_egg"));

	public static final Item PUNCH_PAL_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, SPAWN_EGG_KEY,
			new SpawnEggItem(new Item.Properties().spawnEgg(PUNCH_PAL).setId(SPAWN_EGG_KEY)));

	private ModEntities() {
	}

	public static void register() {
		// Touching this class registers everything above.
	}
}
