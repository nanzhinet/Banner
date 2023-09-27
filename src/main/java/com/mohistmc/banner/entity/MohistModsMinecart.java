package com.mohistmc.banner.entity;

import com.mohistmc.banner.api.EntityAPI;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftMinecart;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;


public class MohistModsMinecart extends CraftMinecart {

    public String entityName;

    public MohistModsMinecart(CraftServer server, AbstractMinecart entity) {
        super(server, entity);
        this.entityName = EntityAPI.entityName(entity);
    }

    @Override
    public AbstractMinecart getHandle() {
        return (AbstractMinecart) this.entity;
    }


    @NotNull
    @Override
    public EntityType getType() {
        return EntityAPI.entityType(entityName);
    }

    @Override
    public String toString() {
        return "MohistModsMinecart{" + entityName + '}';
    }
}
