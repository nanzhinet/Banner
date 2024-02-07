package com.mohistmc.banner.mixin.bukkit.util;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.bukkit.craftbukkit.v1_20_R3.util.ServerShutdownThread;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerShutdownThread.class, remap = false)
public class MixinServerShutdownThread {

    @Shadow @Final private MinecraftServer server;

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public void run() {

        // FORGE: Halting as GameTestServer will cause issues as it always calls System#exit on both crash and normal exit, so skip it
        if (!(server instanceof net.minecraft.gametest.framework.GameTestServer))
            server.halt(true);
        LogManager.shutdown(); // we're manually managing the logging shutdown on the server. Make sure we do it here at the end.

        try {
            server.close();
        } finally {
            try {
                net.minecrell.terminalconsole.TerminalConsoleAppender.close();
            } catch (Exception e) {
            }
        }
    }
}
