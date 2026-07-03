package net.alonzurro.rotd.item;

import net.alonzurro.rotd.entity.ModEntities;
import net.alonzurro.rotd.rotdMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister .Items ITEMS = DeferredRegister.createItems(rotdMod.MOD_ID);

    public static final DeferredItem<Item> SLUG_ITEM = ITEMS.registerSimpleItem("slug_item");
    public static final DeferredItem<Item> SLUG_SPAWN_EGG = ITEMS.register("slug_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SLUG, 0x035cff, 0xffd300,
                    new Item.Properties()));

    public static void register(IEventBus eventBus)     {ITEMS.register(eventBus);
    }
}
