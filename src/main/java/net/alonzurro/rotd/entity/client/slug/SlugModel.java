package net.alonzurro.rotd.entity.client.slug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.alonzurro.rotd.entity.custom.SlugEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SlugModel extends HierarchicalModel<SlugEntity> {
    private final ModelPart root;
    private final ModelPart slug;

    public SlugModel(ModelPart root) {
        this.root = root;
        this.slug = root.getChild("Slug");
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Slug = partdefinition.addOrReplaceChild("Slug", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.5F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(SlugEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.resetPose();
        this.slug.xScale = 1.0f;
        this.slug.yScale = 1.0f;
        this.slug.zScale = 1.0f;
        this.animateWalk(SlugAnimations.walk, limbSwing, limbSwingAmount, 1f, 1f);
        this.animate(entity.idleAnimationState, SlugAnimations.idle, ageInTicks, 1f);
    }



    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        slug.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
