package com.mohistmc.banner.mixin.bukkit;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_20_R3.command.CraftCommandMap;
import org.bukkit.craftbukkit.v1_20_R3.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(CraftServer.class)
public abstract class MixinCraftServer {

    @Mutable @Shadow @Final private String serverName;
    @Shadow @Final protected DedicatedServer console;
    @Shadow @Final private CraftCommandMap commandMap;
    @Shadow public abstract DedicatedPlayerList getHandle();
    @Mutable @Shadow @Final private List<CraftPlayer> playerView;
    @Shadow @Final protected DedicatedPlayerList playerList;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$initCbServer(DedicatedServer console, PlayerList playerList, CallbackInfo ci) {
        this.serverName = "Banner";
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public void syncCommands() {
        // Clear existing commands // Banner - do not clear
        Commands dispatcher = console.resources.managers().commands;

        // Register all commands, vanilla ones will be using the old dispatcher references
        for (Map.Entry<String, Command> entry : commandMap.getKnownCommands().entrySet()) {
            String label = entry.getKey();
            Command command = entry.getValue();

            if (command instanceof VanillaCommandWrapper) {
                LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) ((VanillaCommandWrapper) command).vanillaCommand;
                if (!node.getLiteral().equals(label)) {
                    LiteralCommandNode<CommandSourceStack> clone = new LiteralCommandNode(label, node.getCommand(), node.getRequirement(), node.getRedirect(), node.getRedirectModifier(), node.isFork());

                    for (CommandNode<CommandSourceStack> child : node.getChildren()) {
                        clone.addChild(child);
                    }
                    node = clone;
                }

                dispatcher.getDispatcher().getRoot().addChild(node);
            } else {
                new BukkitCommandWrapper(((CraftServer) (Object) this), entry.getValue()).register(dispatcher.getDispatcher(), label);
            }
        }

        // Refresh commands
        for (ServerPlayer player : getHandle().players) {
            dispatcher.sendCommands(player);
        }
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public List<CraftPlayer> getOnlinePlayers() {
        // Banner start - refresh online players
        this.playerView = Collections.unmodifiableList(Lists.transform(playerList.players, new Function<ServerPlayer, CraftPlayer>() {
            @Override
            public CraftPlayer apply(ServerPlayer player) {
                return player.getBukkitEntity();
            }
        }));
        // Banner end
        return this.playerView;
    }
}
