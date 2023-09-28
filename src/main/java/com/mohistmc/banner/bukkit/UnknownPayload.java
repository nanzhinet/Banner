package com.mohistmc.banner.bukkit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// CraftBukkit start
public record UnknownPayload(ResourceLocation id, io.netty.buffer.ByteBuf data) implements CustomPacketPayload {

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBytes(data);
    }
    // CraftBukkit end
}
