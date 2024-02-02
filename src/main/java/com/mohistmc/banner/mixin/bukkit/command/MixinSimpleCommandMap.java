package com.mohistmc.banner.mixin.bukkit.command;

import com.mohistmc.banner.command.DumpCommand;
import com.mohistmc.banner.command.GetPluginListCommand;
import com.mohistmc.banner.command.ModListCommand;
import com.mohistmc.banner.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleCommandMap.class, remap = false)
public abstract class MixinSimpleCommandMap {

    @Shadow public abstract boolean register(@NotNull String fallbackPrefix, @NotNull Command command);

    @Inject(method = "setDefaultCommands", at = @At("RETURN"))
    private void banner$registerCommands(CallbackInfo ci) {
        register("banner", new ModListCommand("fabricmods"));
        register("banner", new DumpCommand("dump"));
        register("banner", new PluginCommand("plugin"));
        register("banner", new GetPluginListCommand("getpluginlist"));
    }
}
