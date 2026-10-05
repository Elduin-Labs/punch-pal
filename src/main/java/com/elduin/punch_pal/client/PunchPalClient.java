package com.elduin.punch_pal.client;

import com.elduin.punch_pal.entity.ModEntities;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class PunchPalClient {

	private PunchPalClient() {
	}

	public static void register() {
		// Punch Pal wears the game's own player model, so there is no model to register.
		EntityRendererRegistry.register(ModEntities.PUNCH_PAL, PunchPalRenderer::new);
	}
}
