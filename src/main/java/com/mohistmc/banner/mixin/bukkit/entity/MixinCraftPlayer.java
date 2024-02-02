package com.mohistmc.banner.mixin.bukkit.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(value = CraftPlayer.class, remap = false)
public abstract class MixinCraftPlayer extends CraftHumanEntity {

    @Shadow @Final private Set<String> channels;

    public MixinCraftPlayer(CraftServer server, net.minecraft.world.entity.player.Player entity) {
        super(server, entity);
    }

    /**
     * @author wdog5
     * @reason fix channels
     */
    @Overwrite
    @SuppressWarnings("deprecation")
    public void addChannel(String channel) {
        Preconditions.checkState(this.channels.size() < 1024, "Cannot register channel '%s'. Too many channels registered!", channel);
        channel = StandardMessenger.validateAndCorrectChannel(channel);
        if (this.channels.add(channel)) {
            this.server.getPluginManager().callEvent(new PlayerRegisterChannelEvent((CraftPlayer) (Object) this, channel));
        }
    }
}
