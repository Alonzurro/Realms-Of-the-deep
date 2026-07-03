package net.alonzurro.rotd;

import net.alonzurro.rotd.entity.ModEntities;
import net.alonzurro.rotd.entity.client.slug.SlugProjectileRenderer;
import net.alonzurro.rotd.entity.client.slug.SlugRenderer;
import net.alonzurro.rotd.entity.custom.SlugProjectileEntity;
import net.alonzurro.rotd.item.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(net.alonzurro.rotd.rotdMod.MOD_ID)
public class rotdMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "rotd";
    // Directly reference a slf4j logger
    static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public rotdMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);




    ModItems.register(modEventBus);
    ModEntities.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DispenserBlock.registerBehavior(ModItems.SLUG_ITEM.get(),
                new DefaultDispenseItemBehavior() {
                    @Override
                    protected ItemStack execute(BlockSource source, ItemStack stack) {
                        Level level = source.level();
                        Direction direction = source.state().getValue(DispenserBlock.FACING);
                        double x = source.pos().getX() + 0.5 + direction.getStepX() * 0.6;
                        double y = source.pos().getY() + 0.5 + direction.getStepY() * 0.6;
                        double z = source.pos().getZ() + 0.5 + direction.getStepZ() * 0.6;
                        SlugProjectileEntity projectile = new SlugProjectileEntity(level, x, y, z);
                        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1, direction.getStepZ(), 1.1f, 6.0f);
                        level.addFreshEntity(projectile);
                        stack.shrink(1);
                        return stack;
                    }
                }));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.SLUG_SPAWN_EGG);
        }
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.SLUG_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.SLUG.get(), SlugRenderer::new);
            EntityRenderers.register(ModEntities.SLUG_PROJECTILE.get(), SlugProjectileRenderer::new);
        }
    }
}