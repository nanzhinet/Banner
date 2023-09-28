package com.mohistmc.banner.mixin.network.protocol.common;

import com.mohistmc.banner.bukkit.UnknownPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerboundCustomPayloadPacket.class)
public class MixinServerboundCustomPayloadPacket {

    private static UnknownPayload readUnknownPayload(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
        int i = friendlyByteBuf.readableBytes();
        if (i >= 0 && i <= 32767) {
            return new UnknownPayload(resourceLocation, friendlyByteBuf.readBytes(i));
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

}
