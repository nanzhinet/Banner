/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_20_R4.block.impl;

public final class CraftPotatoes extends org.bukkit.craftbukkit.v1_20_R4.block.data.CraftBlockData implements org.bukkit.block.data.Ageable {

    public CraftPotatoes() {
        super();
    }

    public CraftPotatoes(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_20_R4.block.data.CraftAgeable

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE = getInteger(net.minecraft.world.level.block.PotatoBlock.class, "age");

    @Override
    public int getAge() {
        return this.get(CraftPotatoes.AGE);
    }

    @Override
    public void setAge(int age) {
        this.set(CraftPotatoes.AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return getMax(CraftPotatoes.AGE);
    }
}
