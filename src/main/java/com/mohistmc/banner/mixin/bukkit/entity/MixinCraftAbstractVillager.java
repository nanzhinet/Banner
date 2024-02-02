package com.mohistmc.banner.mixin.bukkit.entity;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftAbstractVillager;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftAgeable;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CraftAbstractVillager.class, remap = false)
public abstract class MixinCraftAbstractVillager extends CraftAgeable implements org.bukkit.entity.AbstractVillager, InventoryHolder {

    public MixinCraftAbstractVillager(CraftServer server, AgeableMob entity) {
        super(server, entity);
    }

    /**
     * @author wdog5
     * @reason fix bugs
     */
    @Overwrite
    public AbstractVillager getHandle() {
        return (AbstractVillager) this.entity;
    }
}
