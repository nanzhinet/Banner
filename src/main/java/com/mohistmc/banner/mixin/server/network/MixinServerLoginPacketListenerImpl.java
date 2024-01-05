package com.mohistmc.banner.mixin.server.network;

import com.mohistmc.banner.injection.server.network.InjectionServerLoginPacketListenerImpl;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import io.netty.channel.local.LocalAddress;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_20_R2.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinServerLoginPacketListenerImpl implements ServerLoginPacketListener, TickablePacketListener, InjectionServerLoginPacketListenerImpl {

    @Shadow public abstract void disconnect(Component component);

    @Shadow private volatile ServerLoginPacketListenerImpl.State state;

    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private byte[] challenge;

    @Shadow @Final public Connection connection;
    @Shadow @Final private static AtomicInteger UNIQUE_THREAD_ID;
    @Shadow @Nullable String requestedUsername;
    @Shadow @Final private static Logger LOGGER;
    @Shadow abstract void startClientVerification(GameProfile profile);

    @Shadow
    protected static GameProfile createOfflineProfile(String string) {
        return null;
    }

    @Shadow @Nullable private GameProfile authenticatedProfile;

    @Override
    public void disconnect(final String s) {
        this.disconnect(Component.literal(s));
    }

    /**
     * @author wdog5
     * @reason bukkit
     */
    @Overwrite
    public void handleKey(ServerboundKeyPacket serverboundKeyPacket) {
        Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet", new Object[0]);

        final String string;
        try {
            PrivateKey privateKey = this.server.getKeyPair().getPrivate();
            if (!serverboundKeyPacket.isChallengeValid(this.challenge, privateKey)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretKey = serverboundKeyPacket.getSecretKey(privateKey);
            Cipher cipher = Crypt.getCipher(2, secretKey);
            Cipher cipher2 = Crypt.getCipher(1, secretKey);
            string = (new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), secretKey))).toString(16);
            this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            this.connection.setEncryptionKey(cipher, cipher2);
        } catch (CryptException var7) {
            throw new IllegalStateException("Protocol error", var7);
        }

        class Handler extends Thread {

            Handler() {
                super("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet());
            }

            public void run() {
                String stringx = (String)Objects.requireNonNull(requestedUsername, "Player name not initialized");

                try {
                    ProfileResult profileResult = server.getSessionService().hasJoinedServer(stringx, string, this.getAddress());
                    if (profileResult != null) {
                        GameProfile gameProfile = profileResult.profile();
                        banner$preLogin();
                        startClientVerification(gameProfile);
                    } else if (server.isSingleplayer()) {
                        LOGGER.warn("Failed to verify username but will let them in anyway!");
                        startClientVerification(createOfflineProfile(stringx));
                    } else {
                        disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                        LOGGER.error("Username '{}' tried to join with an invalid session", stringx);
                    }
                } catch (AuthenticationUnavailableException var4) {
                    if (server.isSingleplayer()) {
                        LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        startClientVerification(createOfflineProfile(stringx));
                    } else {
                        disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                        LOGGER.error("Couldn't verify username because servers are unavailable");
                    }
                }

            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketAddress = connection.getRemoteAddress();
                return server.getPreventProxyConnections() && socketAddress instanceof InetSocketAddress ? ((InetSocketAddress)socketAddress).getAddress() : null;
            }
        };
        new Handler().setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        new Handler().start();
    }

    @Inject(method= "handleHello", at = @At("TAIL"))
    public void banner$handleHello(ServerboundHelloPacket serverboundHelloPacket, CallbackInfo ci) {
            // Spigot start
            new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
                @Override
                public void run() {
                    try {
                        banner$preLogin();
                    } catch (Exception ex) {
                        disconnect("Failed to verify username!");
                        LOGGER.warn("Exception verifying " + serverboundHelloPacket.name(), ex);
                    }
                }
            }.start();
            // Spigot end
    }

    public void banner$preLogin() {
        String playerName = authenticatedProfile.getName();
        java.net.InetAddress address;
        if (connection.getRawAddress() instanceof LocalAddress) {
            try {
                address = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else address = ((java.net.InetSocketAddress) connection.getRawAddress()).getAddress();
        UUID uniqueId = authenticatedProfile.getId();

        AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
        asyncEvent.callEvent();

        if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
            final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
            if (asyncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED)
                event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());

            Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                @Override
                protected PlayerPreLoginEvent.Result evaluate() {
                    event.callEvent();
                    return event.getResult();
                }
            };

            server.bridge$processQueue().add(waitable);
            try {
                if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                    disconnect(event.getKickMessage());
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                disconnect(asyncEvent.getKickMessage());
                return;
            }
        }
        LOGGER.info("UUID of player {} is {}", authenticatedProfile.getName(), authenticatedProfile.getId());
        this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
    }

}