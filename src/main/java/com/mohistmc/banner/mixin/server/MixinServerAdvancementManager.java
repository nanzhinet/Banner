package com.mohistmc.banner.mixin.server;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class MixinServerAdvancementManager {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private LootDataManager lootData;

    @Shadow private AdvancementTree tree;

    @Shadow public Map<ResourceLocation, AdvancementHolder> advancements;

    /**
     * @author wdog5
     * @reason spigot config
     */
    @Overwrite
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, AdvancementHolder> builder = ImmutableMap.builder();
        object.forEach((resourceLocation, jsonElement) -> {
            // Spigot start
            if (org.spigotmc.SpigotConfig.disabledAdvancements != null
                    && (org.spigotmc.SpigotConfig.disabledAdvancements.contains("*")
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.toString())
                    || org.spigotmc.SpigotConfig.disabledAdvancements.contains(resourceLocation.getNamespace()))) {
                return;
            }
            // Spigot end
            try {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "advancement");
                Advancement advancement = Advancement.fromJson(jsonObject, new DeserializationContext(resourceLocation, this.lootData));
                builder.put(resourceLocation, new AdvancementHolder(resourceLocation, advancement));
            } catch (Exception var6) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", resourceLocation, var6.getMessage());
            }

        });
        this.advancements = builder.buildOrThrow();
        AdvancementTree advancementTree = new AdvancementTree();
        advancementTree.addAll(this.advancements.values());
        Iterator var6 = advancementTree.roots().iterator();

        while(var6.hasNext()) {
            AdvancementNode advancementNode = (AdvancementNode)var6.next();
            if (advancementNode.holder().value().display().isPresent()) {
                TreeNodePosition.run(advancementNode);
            }
        }

        this.tree = advancementTree;
    }

}
