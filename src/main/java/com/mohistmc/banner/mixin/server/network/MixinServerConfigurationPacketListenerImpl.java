package com.mohistmc.banner.mixin.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.SocketAddress;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class MixinServerConfigurationPacketListenerImpl extends MixinServerCommonPacketListenerImpl{

    @Redirect(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"))
    private Component banner$skipLoginCheck(PlayerList instance, SocketAddress address, GameProfile gameProfile) {
        return null;
    }

    @Redirect(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer banner$useCurrentPlayer(PlayerList instance, GameProfile profile, ClientInformation clientInformation) {
        return instance.getPlayerForLogin(profile, clientInformation, this.player); // CraftBukkit
    }
}
