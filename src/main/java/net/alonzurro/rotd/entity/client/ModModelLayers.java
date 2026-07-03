package net.alonzurro.rotd.entity.client;

import net.alonzurro.rotd.rotdMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation SLUG = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(rotdMod.MOD_ID, "slug"), "main");
    public static final ModelLayerLocation SLUG_PROJECTILE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(rotdMod.MOD_ID, "slug_projectile"), "main");
}
