package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftMob;

public class MohistModsMob extends CraftMob {

    public MohistModsMob(CraftServer server, Mob entity) {
        super(server, entity);
    }
}
