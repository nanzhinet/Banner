/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_20_R4.block.impl;

public final class CraftCactus extends org.bukkit.craftbukkit.v1_20_R4.block.data.CraftBlockData implements org.bukkit.block.data.Ageable {

    public CraftCactus() {
        super();
    }

    public CraftCactus(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_20_R4.block.data.CraftAgeable

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE = getInteger(net.minecraft.world.level.block.CactusBlock.class, "age");

    @Override
    public int getAge() {
        return this.get(CraftCactus.AGE);
    }

    @Override
    public void setAge(int age) {
        this.set(CraftCactus.AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return getMax(CraftCactus.AGE);
    }
}
