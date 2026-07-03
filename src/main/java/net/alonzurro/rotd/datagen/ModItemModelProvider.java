package net.alonzurro.rotd.datagen;

import net.alonzurro.rotd.item.ModItems;
import net.alonzurro.rotd.rotdMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, rotdMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        withExistingParent(ModItems.SLUG_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));

    }
}
