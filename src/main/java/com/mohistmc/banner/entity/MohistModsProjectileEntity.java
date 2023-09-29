package com.mohistmc.banner.entity;

import com.mohistmc.banner.api.EntityAPI;
import net.minecraft.world.entity.projectile.Projectile;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftProjectile;
import org.bukkit.entity.EntityType;

public class MohistModsProjectileEntity extends CraftProjectile {

    public String entityName;

    public MohistModsProjectileEntity(CraftServer server, Projectile entity) {
        super(server, entity);
        this.entityName = EntityAPI.entityName(entity);
    }

    @Override
    public Projectile getHandle() {
        return (Projectile) this.entity;
    }

    @Override
    public String toString() {
        return "MohistModsProjectileEntity{" + entityName + '}';
    }
}

