/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_20_R4.block.impl;

public final class CraftStem extends org.bukkit.craftbukkit.v1_20_R4.block.data.CraftBlockData implements org.bukkit.block.data.Ageable {

    public CraftStem() {
        super();
    }

    public CraftStem(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_20_R4.block.data.CraftAgeable

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE = getInteger(net.minecraft.world.level.block.StemBlock.class, "age");

    @Override
    public int getAge() {
        return this.get(CraftStem.AGE);
    }

    @Override
    public void setAge(int age) {
        this.set(CraftStem.AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return getMax(CraftStem.AGE);
    }
}
