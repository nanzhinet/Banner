package com.mohistmc.banner.injection.world.item.crafting;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

/**
 * @author Mgazul by MohistMC
 * @date 2023/9/27 21:41:04
 */
public interface InjectionRecipe {

    Recipe toBukkitRecipe(NamespacedKey id);
}
