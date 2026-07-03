package net.alonzurro.rotd.entity.client.slug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.alonzurro.rotd.entity.client.ModModelLayers;
import net.alonzurro.rotd.entity.custom.SlugProjectileEntity;
import net.alonzurro.rotd.rotdMod;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SlugProjectileRenderer extends EntityRenderer<SlugProjectileEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(rotdMod.MOD_ID, "textures/entity/slug/slug.png");

    private final ModelPart root;
    private final ModelPart slug;

    public SlugProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.root = context.bakeLayer(ModModelLayers.SLUG_PROJECTILE);
        this.slug = this.root.getChild("Slug");
    }

    @Override
    public ResourceLocation getTextureLocation(SlugProjectileEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(SlugProjectileEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        root.resetPose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        slug.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
