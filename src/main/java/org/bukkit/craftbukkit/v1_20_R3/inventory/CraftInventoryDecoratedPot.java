package org.bukkit.craftbukkit.v1_20_R3.inventory;

import net.minecraft.world.Container;
import org.bukkit.inventory.DecoratedPotInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Spliterator;
import java.util.function.Consumer;

public class CraftInventoryDecoratedPot extends CraftInventory implements DecoratedPotInventory {

    public CraftInventoryDecoratedPot(Container inventory) {
        super(inventory);
    }

    @Override
    public void setItem(ItemStack item) {
        setItem(0, item);
    }

    @Override
    public ItemStack getItem() {
        return getItem(0);
    }

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        super.forEach(action);
    }

    @Override
    public Spliterator<ItemStack> spliterator() {
        return super.spliterator();
    }
}
