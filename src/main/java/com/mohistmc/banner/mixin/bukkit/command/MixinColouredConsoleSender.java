package com.mohistmc.banner.mixin.bukkit.command;

import jline.Terminal;
import jline.console.ConsoleReader;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_20_R3.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_20_R3.command.CraftConsoleCommandSender;
import org.fusesource.jansi.Ansi;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(value = ColouredConsoleSender.class, remap = false)
public abstract class MixinColouredConsoleSender extends CraftConsoleCommandSender {

    @Shadow
    private static String convertRGBColors(String input) {
        return null;
    }

    @Shadow @Final private ChatColor[] colors;
    @Shadow @Final private Map<ChatColor, String> replacements;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljline/console/ConsoleReader;getTerminal()Ljline/Terminal;"))
    private Terminal banner$removeTerminal(ConsoleReader instance) {
        return null;
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public void sendMessage(String message) {
        if (TerminalConsoleAppender.isAnsiSupported()) {
            if (!conversationTracker.isConversingModaly()) {
                String result = convertRGBColors(message);
                for (ChatColor color : colors) {
                    if (replacements.containsKey(color)) {
                        result = result.replaceAll("(?i)" + color.toString(), replacements.get(color));
                    } else {
                        result = result.replaceAll("(?i)" + color.toString(), "");
                    }
                }
                System.out.println(result + Ansi.ansi().reset().toString());
            }
        } else {
            super.sendMessage(message);
        }
    }
}
