package com.mohistmc.banner.mixin.network.protocol;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static com.mohistmc.banner.BannerServer.LOGGER;

@Mixin(PacketUtils.class)
public class MixinPacketUtils {

    /**
     * @author wdog5
     * @reason bukkit reason
     */
    @Overwrite
    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T processor, BlockableEventLoop<?> executor) throws RunningOnDifferentThreadException {
        if (!executor.isSameThread()) {
            executor.executeIfPossible(() -> {
                if (BukkitExtraConstants.getServer().hasStopped() || (processor instanceof ServerCommonPacketListener && ((ServerCommonPacketListener) processor).bridge$processedDisconnect())) return; // CraftBukkit, MC-142590
                if (processor.isAcceptingMessages()) {
                    try {
                        packet.handle(processor);
                    } catch (Exception var3) {
                        if (var3 instanceof ReportedException reportedexception) {
                            if (reportedexception.getCause() instanceof OutOfMemoryError) {
                                throw var3;
                            }
                        }
                        if (processor.shouldPropagateHandlingExceptions()) {
                            throw var3;
                        }

                        LOGGER.error("Failed to handle packet {}, suppressing error", packet, var3);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
                }

            });
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
            // CraftBukkit start - SPIGOT-5477, MC-142590
        } else if (BukkitExtraConstants.getServer().hasStopped() || (processor instanceof ServerCommonPacketListener && ((ServerCommonPacketListener) processor).bridge$processedDisconnect())) {
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
            // CraftBukkit end
        }
    }
}
