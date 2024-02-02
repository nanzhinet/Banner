package com.mohistmc.banner.mixin.bukkit.inventory;

import com.mohistmc.banner.bukkit.inventory.MohistSpecialIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CraftRecipe.class, remap = false)
public interface MixinCraftRecipe {

    /**
     * @author wdog5
     * @reason mod compat
     */
    @Overwrite
    default Ingredient toNMS(RecipeChoice bukkit, boolean requireNotEmpty) {
        Ingredient stack;
        if (bukkit == null) {
            stack = Ingredient.EMPTY;
        } else if (bukkit instanceof RecipeChoice.MaterialChoice) {
            stack = new Ingredient(((RecipeChoice.MaterialChoice) bukkit).getChoices().stream().map((mat) -> {
                return new Ingredient.ItemValue(CraftItemStack.asNMSCopy(new ItemStack(mat)));
            }));
        } else if (bukkit instanceof RecipeChoice.ExactChoice) {
            stack = new Ingredient(((RecipeChoice.ExactChoice) bukkit).getChoices().stream().map((mat) -> {
                return new Ingredient.ItemValue(CraftItemStack.asNMSCopy(mat));
            }));
             stack.banner$setExact(true);
        } else if (bukkit instanceof MohistSpecialIngredient) {
            stack = ((MohistSpecialIngredient) bukkit).ingredient();
        } else {
            throw new IllegalArgumentException("Unknown recipe stack instance " + bukkit);
        }

        stack.getItems();
        if (stack.isVanilla() && requireNotEmpty && stack.getItems().length == 0) {
            throw new IllegalArgumentException("Recipe requires at least one non-air choice!");
        } else {
            return stack;
        }
    }

    /**
     * @author wdog5
     * @reason mod compat
     */
    @Overwrite
    static RecipeChoice toBukkit(Ingredient list) {
        list.getItems();
        if (!list.isVanilla()) {
            return new MohistSpecialIngredient(list);
        }
        net.minecraft.world.item.ItemStack[] items = list.getItems();
        if (items.length == 0) {
            return null;
        } else {
            if (list.bridge$exact()) {
                List<ItemStack> choices = new ArrayList<>(items.length);
                for (net.minecraft.world.item.ItemStack i : items) {
                    choices.add(CraftItemStack.asBukkitCopy(i));
                }
                return new RecipeChoice.ExactChoice(choices);
            } else {
                List<org.bukkit.Material> choices = new ArrayList<>(items.length);
                for (net.minecraft.world.item.ItemStack i : items) {
                    choices.add(CraftMagicNumbers.getMaterial(i.getItem()));
                }
                return new RecipeChoice.MaterialChoice(choices);
            }
        }
    }
}
