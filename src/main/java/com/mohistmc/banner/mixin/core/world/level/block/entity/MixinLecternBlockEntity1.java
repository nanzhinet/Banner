package com.mohistmc.banner.mixin.core.world.level.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/level/block/entity/LecternBlockEntity$1")
public abstract class MixinLecternBlockEntity1 implements Container {

    @Shadow @Final private LecternBlockEntity field_17391;
    public List<HumanEntity> transaction = new ArrayList<>();
    private int maxStack = 1;

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            field_17391.setBook(stack);
            if (field_17391.getLevel() != null) {
                LecternBlock.resetBookState(null, field_17391.getLevel(), field_17391.getBlockPos(), field_17391.getBlockState(), field_17391.hasBook());
            }
        }
    }

    @Override
    public List<ItemStack> getContents() {
        return Collections.singletonList(field_17391.getBook());
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return field_17391.bridge$getOwner();
    }

    @Override
    public void setOwner(InventoryHolder owner) {
    }

    @Override
    public int getMaxStackSize() {
        if (maxStack == 0) maxStack = 1;
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public Location getLocation() {
        if (field_17391.getLevel() == null) return null;
        return new Location(field_17391.getLevel().getWorld(), field_17391.getBlockPos().getX(), field_17391.getBlockPos().getY(), field_17391.getBlockPos().getZ());
    }

    @Override
    public RecipeHolder<?> getCurrentRecipe() {
        return null;
    }

    @Override
    public void setCurrentRecipe(RecipeHolder<?> recipe) {
    }

    public LecternBlockEntity getLectern() {
        return field_17391;
    }
}
