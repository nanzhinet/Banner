package com.mohistmc.banner.injection.world.entity;

import net.minecraft.world.item.ItemStack;

public interface InjectionAnimal {

    default ItemStack getBreedItem() {
        return null;
    }
}
