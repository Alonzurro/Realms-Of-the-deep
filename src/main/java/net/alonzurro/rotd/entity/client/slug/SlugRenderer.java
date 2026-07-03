package net.alonzurro.rotd.entity.client.slug;

import net.alonzurro.rotd.entity.client.ModModelLayers;
import net.alonzurro.rotd.entity.custom.SlugEntity;
import net.alonzurro.rotd.rotdMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SlugRenderer extends MobRenderer<SlugEntity, SlugModel> {
    public SlugRenderer(EntityRendererProvider.Context context) {
        super(context, new SlugModel(context.bakeLayer(ModModelLayers.SLUG)), 0.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(SlugEntity SlugEntity) {
        return ResourceLocation.fromNamespaceAndPath(rotdMod.MOD_ID, "textures/entity/slug/slug.png");
    }
}
