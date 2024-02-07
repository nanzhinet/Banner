package com.mohistmc.banner.mixin.bukkit.util;

import com.mojang.logging.LogQueues;
import jline.console.ConsoleReader;
import org.bukkit.craftbukkit.v1_20_R3.util.TerminalConsoleWriterThread;
import org.fusesource.jansi.Ansi;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mixin(value = TerminalConsoleWriterThread.class, remap = false)
public class MixinTerminalConsoleWriterThread {

    @Shadow @Final private ConsoleReader reader;

    @Shadow @Final private OutputStream output;

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public void run() {
        String message;

        // Using name from log4j config in vanilla jar
        while (true) {
            message = LogQueues.getNextLogEvent("TerminalConsole");
            if (message == null) {
                continue;
            }

            try {
                if (true) {
                    reader.print(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + ConsoleReader.RESET_LINE);
                    reader.flush();
                    output.write(message.getBytes());
                    output.flush();

                    try {
                        reader.drawLine();
                    } catch (Throwable ex) {
                        reader.getCursorBuffer().clear();
                    }
                    reader.flush();
                } else {
                    output.write(message.getBytes());
                    output.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(TerminalConsoleWriterThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
