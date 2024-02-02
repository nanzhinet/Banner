package com.mohistmc.banner.mixin.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VersionCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = VersionCommand.class, remap = false)
public class MixinVersionCommand {

    @Redirect(method = "execute",
            at = @At(value = "INVOKE",
            target = "Lorg/bukkit/command/defaults/VersionCommand;sendVersion(Lorg/bukkit/command/CommandSender;)V"))
    private void banner$cancelCheckOnlineVersion(VersionCommand instance, CommandSender sender) {

    }
}
