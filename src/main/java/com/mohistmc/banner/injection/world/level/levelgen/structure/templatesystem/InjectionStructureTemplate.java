package com.mohistmc.banner.injection.world.level.levelgen.structure.templatesystem;

import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataContainer;

public interface InjectionStructureTemplate {

    default CraftPersistentDataContainer bridge$persistentDataContainer() {
        return null;
    }
}
