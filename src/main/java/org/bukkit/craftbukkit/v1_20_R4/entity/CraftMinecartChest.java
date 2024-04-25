package org.bukkit.craftbukkit.v1_20_R4.entity;

import net.minecraft.world.entity.vehicle.MinecartChest;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventory;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.Inventory;

@SuppressWarnings("deprecation")
public class CraftMinecartChest extends CraftMinecartContainer implements StorageMinecart {
    private final CraftInventory inventory;

    public CraftMinecartChest(CraftServer server, MinecartChest entity) {
        super(server, entity);
        this.inventory = new CraftInventory(entity);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public String toString() {
        return "CraftMinecartChest{" + "inventory=" + this.inventory + '}';
    }
}
