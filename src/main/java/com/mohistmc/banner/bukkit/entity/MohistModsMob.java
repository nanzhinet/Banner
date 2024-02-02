package com.mohistmc.banner.bukkit.entity;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftMob;

public class MohistModsMob extends CraftMob {

    public MohistModsMob(CraftServer server, Mob entity) {
        super(server, entity);
    }
}
