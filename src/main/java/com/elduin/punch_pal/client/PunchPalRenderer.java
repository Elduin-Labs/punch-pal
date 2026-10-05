package com.elduin.punch_pal.client;

import com.elduin.punch_pal.PunchPal;
import com.elduin.punch_pal.entity.PunchPalEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** Punch Pal looks like a player: the player model, with its own plain white skin. */
public class PunchPalRenderer extends HumanoidMobRenderer<PunchPalEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

	private static final Identifier SKIN = PunchPal.id("textures/entity/punch_pal/punch_pal.png");

	public PunchPalRenderer(EntityRendererProvider.Context context) {
		// the player layer, so the jacket, sleeves and trousers layers show too
		super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return SKIN;
	}
}
