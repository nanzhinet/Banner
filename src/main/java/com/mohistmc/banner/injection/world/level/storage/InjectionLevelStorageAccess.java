package com.mohistmc.banner.injection.world.level.storage;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

public interface InjectionLevelStorageAccess {

    default ResourceKey<LevelStem> bridge$getTypeKey() {
        return null;
    }

    default void bridge$setDimType(ResourceKey<LevelStem> typeKey) {
    }
}
