package com.mohistmc.banner.injection.world.item.crafting;

import org.bukkit.NamespacedKey;

public interface InjectionRecipe {

    default org.bukkit.inventory.Recipe toBukkitRecipe(NamespacedKey id) {
        return null;
    }
}
