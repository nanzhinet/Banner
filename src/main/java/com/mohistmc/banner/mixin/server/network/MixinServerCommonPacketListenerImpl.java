package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.injection.server.network.InjectionServerCommonPacketListenerImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.Waitable;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ExecutionException;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class MixinServerCommonPacketListenerImpl implements InjectionServerCommonPacketListenerImpl {

    @Shadow
    @Final
    protected Connection connection;
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    protected MinecraftServer server;

    @Shadow
    public abstract void onDisconnect(Component p_300550_);

    @Shadow
    public abstract void send(Packet<?> p_300558_);

    @Shadow
    protected abstract boolean isSingleplayerOwner();
    // @formatter:on

    protected ServerPlayer player;
    protected CraftServer cserver;
    public boolean processedDisconnect;

    public CraftPlayer getCraftPlayer() {
        return (this.player == null) ? null : this.player.getBukkitEntity();
    }

    @Override
    public void banner$setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$init(MinecraftServer p_299469_, Connection p_300872_, CommonListenerCookie p_300277_, CallbackInfo ci) {
        this.cserver = ((CraftServer) Bukkit.getServer());
    }

    @ModifyConstant(method = "keepConnectionAlive", constant = @Constant(longValue = 15000L))
    private long banner$incrKeepaliveTimeout(long l) {
        return 25000L;
    }

    @Override
    public boolean bridge$processedDisconnect() {
        return this.processedDisconnect;
    }

    public final boolean isDisconnected() {
        return !((ServerPlayer) this.player).bridge$joining() && !this.connection.isConnected();
    }

    @Override
    public boolean banner$isDisconnected() {
        return this.isDisconnected();
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void disconnect(Component textComponent) {
        this.disconnect(CraftChatMessage.fromComponent(textComponent));
    }

    @Override
    public void disconnect(String s) {
        if (this.processedDisconnect) {
            return;
        }
        if (!this.cserver.isPrimaryThread()) {
            Waitable<?> waitable = new Waitable<>() {
                @Override
                protected Object evaluate() {
                    disconnect(s);
                    return null;
                }
            };

            ((MinecraftServer) this.server).bridge$queuedProcess(waitable);

            try {
                waitable.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        String leaveMessage = ChatFormatting.YELLOW + this.player.getScoreboardName() + " left the game.";
        PlayerKickEvent event = new PlayerKickEvent(getCraftPlayer(), s, leaveMessage);
        if (this.cserver.getServer().isRunning()) {
            this.cserver.getPluginManager().callEvent(event);
        }
        if (event.isCancelled()) {
            return;
        }
        BukkitSnapshotCaptures.captureQuitMessage(event.getLeaveMessage());
        Component textComponent = CraftChatMessage.fromString(event.getReason(), true)[0];
        this.connection.send(new ClientboundDisconnectPacket(textComponent), PacketSendListener.thenRun(() -> this.connection.disconnect(textComponent)));
        this.onDisconnect(textComponent);
        this.connection.setReadOnly();
        this.server.executeBlocking(this.connection::handleDisconnection);
    }

    @Inject(method = "onDisconnect", cancellable = true, at = @At("HEAD"))
    private void banner$returnIfProcessed(Component reason, CallbackInfo ci) {
        if (processedDisconnect) {
            ci.cancel();
        } else {
            processedDisconnect = true;
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", cancellable = true, at = @At("HEAD"))
    private void banner$updateCompassTarget(Packet<?> packetIn, PacketSendListener futureListeners, CallbackInfo ci) {
        if (packetIn == null || processedDisconnect) {
            ci.cancel();
            return;
        }
        if (packetIn instanceof ClientboundSetDefaultSpawnPositionPacket packet6) {
            ((ServerPlayer) this.player).banner$setCompassTarget(new Location(this.getCraftPlayer().getWorld(), packet6.pos.getX(), packet6.pos.getY(), packet6.pos.getZ()));
        }
    }


    private static final ResourceLocation CUSTOM_REGISTER = new ResourceLocation("register");
    private static final ResourceLocation CUSTOM_UNREGISTER = new ResourceLocation("unregister");

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void banner$customPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        // Banner TODO - fix bukkit channel
        /*
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, this.player.serverLevel());
        var readerIndex = packet.data.readerIndex();
        var buf = new byte[packet.data.readableBytes()];
        packet.data.readBytes(buf);
        packet.data.readerIndex(readerIndex);

        if (this.connection.isConnected()) {
            if (packet.identifier.equals(CUSTOM_REGISTER)) {
                try {
                    String channels = new String(buf, StandardCharsets.UTF_8);
                    for (String channel : channels.split("\0")) {
                        if (!StringUtil.isNullOrEmpty(channel)) {
                            this.getCraftPlayer().addChannel(channel);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error("Couldn't register custom payload", ex);
                    this.disconnect("Invalid payload REGISTER!");
                }
            } else if (packet.identifier.equals(CUSTOM_UNREGISTER)) {
                try {
                    String channels = new String(buf, StandardCharsets.UTF_8);
                    for (String channel : channels.split("\0")) {
                        if (!StringUtil.isNullOrEmpty(channel)) {
                            this.getCraftPlayer().removeChannel(channel);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error("Couldn't unregister custom payload", ex);
                    this.disconnect("Invalid payload UNREGISTER!");
                }
            } else {
                try {
                    this.cserver.getMessenger().dispatchIncomingMessage(this.player.getBukkitEntity(), packet.identifier.toString(), buf);
                } catch (Exception ex) {
                    LOGGER.error("Couldn't dispatch custom payload", ex);
                    this.disconnect("Invalid custom payload!");
                }
            }
        }
        */
    }

    @Inject(method = "handleResourcePackResponse", at = @At("RETURN"))
    private void banner$handleResourcePackStatus(ServerboundResourcePackPacket packetIn, CallbackInfo ci) {
        this.cserver.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(this.getCraftPlayer(), PlayerResourcePackStatusEvent.Status.values()[packetIn.getAction().ordinal()]));
    }
}
