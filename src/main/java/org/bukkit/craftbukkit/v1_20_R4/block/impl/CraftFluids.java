/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_20_R4.block.impl;

public final class CraftFluids extends org.bukkit.craftbukkit.v1_20_R4.block.data.CraftBlockData implements org.bukkit.block.data.Levelled {

    public CraftFluids() {
        super();
    }

    public CraftFluids(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_20_R4.block.data.CraftLevelled

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty LEVEL = getInteger(net.minecraft.world.level.block.LiquidBlock.class, "level");

    @Override
    public int getLevel() {
        return this.get(CraftFluids.LEVEL);
    }

    @Override
    public void setLevel(int level) {
        this.set(CraftFluids.LEVEL, level);
    }

    @Override
    public int getMaximumLevel() {
        return getMax(CraftFluids.LEVEL);
    }
}
