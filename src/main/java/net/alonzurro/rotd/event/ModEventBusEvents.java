package net.alonzurro.rotd.event;

import net.alonzurro.rotd.entity.ModEntities;
import net.alonzurro.rotd.entity.client.ModModelLayers;
import net.alonzurro.rotd.entity.client.slug.SlugModel;
import net.alonzurro.rotd.entity.custom.SlugEntity;
import net.alonzurro.rotd.entity.custom.SlugProjectileEntity;
import net.alonzurro.rotd.item.ModItems;
import net.alonzurro.rotd.rotdMod;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = rotdMod.MOD_ID)
class ModEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.SLUG, SlugModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SLUG_PROJECTILE, SlugModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SLUG.get(), SlugEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof IronGolem
                && event.getNewAboutToBeSetTarget() instanceof SlugEntity slug
                && slug.isOwned()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() != ModItems.SLUG_ITEM.get()) return;

        Level level = player.level();
        if (level.isClientSide()) return;

        SlugProjectileEntity projectile = new SlugProjectileEntity(level, player);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
        level.addFreshEntity(projectile);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(stack.getItem(), 20);
    }
}
