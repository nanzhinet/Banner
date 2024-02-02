package com.mohistmc.banner.mixin.bukkit.entity;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.Component;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(targets = "org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer$1", remap = false)
public abstract class MixinCraftPlayer_1 {

    @Shadow @Final
    CraftPlayer this$0;

    @Inject(method = "sendMessage(Lnet/md_5/bungee/api/ChatMessageType;Ljava/util/UUID;[Lnet/md_5/bungee/api/chat/BaseComponent;)V",
            at = @At("HEAD"), cancellable = true)
    private void banner$sendMsgFix(ChatMessageType position, UUID sender, BaseComponent[] components, CallbackInfo ci) {
        banner$sendMessage(position, sender, components);
        ci.cancel();
    }

    public void banner$sendMessage(net.md_5.bungee.api.ChatMessageType position, UUID sender, BaseComponent... components) {
        if (this$0.getHandle().connection == null ) return;
        this$0.getHandle().connection.send(new
                net.minecraft.network.protocol.game.ClientboundSystemChatPacket(
                Component.Serializer.fromJson(ComponentSerializer.
                        toString(components)),
                position == net.md_5.bungee.api.ChatMessageType.ACTION_BAR));
    }
}
