/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_20_R4.block.impl;

public final class CraftFurnaceFurace extends org.bukkit.craftbukkit.v1_20_R4.block.data.CraftBlockData implements org.bukkit.block.data.type.Furnace, org.bukkit.block.data.Directional, org.bukkit.block.data.Lightable {

    public CraftFurnaceFurace() {
        super();
    }

    public CraftFurnaceFurace(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_20_R4.block.data.CraftDirectional

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> FACING = getEnum(net.minecraft.world.level.block.FurnaceBlock.class, "facing");

    @Override
    public org.bukkit.block.BlockFace getFacing() {
        return this.get(CraftFurnaceFurace.FACING, org.bukkit.block.BlockFace.class);
    }

    @Override
    public void setFacing(org.bukkit.block.BlockFace facing) {
        this.set(CraftFurnaceFurace.FACING, facing);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        return this.getValues(CraftFurnaceFurace.FACING, org.bukkit.block.BlockFace.class);
    }

    // org.bukkit.craftbukkit.v1_20_R4.block.data.CraftLightable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT = getBoolean(net.minecraft.world.level.block.FurnaceBlock.class, "lit");

    @Override
    public boolean isLit() {
        return this.get(CraftFurnaceFurace.LIT);
    }

    @Override
    public void setLit(boolean lit) {
        this.set(CraftFurnaceFurace.LIT, lit);
    }
}
