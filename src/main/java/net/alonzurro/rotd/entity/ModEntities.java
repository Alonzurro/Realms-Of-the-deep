package net.alonzurro.rotd.entity;

import net.alonzurro.rotd.entity.custom.SlugEntity;
import net.alonzurro.rotd.entity.custom.SlugProjectileEntity;
import net.alonzurro.rotd.rotdMod;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, rotdMod.MOD_ID);
    public static final Supplier<EntityType<SlugEntity>> SLUG =
            ENTITY_TYPES.register("slug", () -> EntityType.Builder.of(SlugEntity::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.25f).build("slug"));
    public static final Supplier<EntityType<SlugProjectileEntity>> SLUG_PROJECTILE =
            ENTITY_TYPES.register("slug_projectile", () -> EntityType.Builder.<SlugProjectileEntity>of(SlugProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).build("slug_projectile"));
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
