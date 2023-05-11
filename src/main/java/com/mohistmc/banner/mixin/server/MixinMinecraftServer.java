package com.mohistmc.banner.mixin.server;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mohistmc.banner.bukkit.BukkitCaptures;
import com.mohistmc.banner.injection.server.InjectionMinecraftServer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.Main;
import com.mohistmc.banner.util.ServerUtils;
import com.mojang.datafixers.DataFixer;
import jline.console.ConsoleReader;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.server.*;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R3.util.LazyPlayerSet;
import org.bukkit.event.player.AsyncPlayerChatPreviewEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Banner - TODO fix inject method
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements InjectionMinecraftServer {

    // @formatter:off
    @Shadow public MinecraftServer.ReloadableResources resources;

    @Shadow public Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> levels;
    @Shadow private static void setInitialSpawn(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug) {}
    @Shadow protected abstract void setupDebugLevel(WorldData worldData);
    @Shadow public WorldData worldData;
    @Shadow @Final public static org.slf4j.Logger LOGGER;
    @Shadow private long nextTickTime;
    @Shadow public abstract boolean isSpawningMonsters();
    @Shadow public abstract boolean isSpawningAnimals();
    @Shadow private String localIp;
    @Shadow private boolean onlineMode;
    @Shadow private int tickCount;
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow private long lastOverloadWarning;
    @Shadow public abstract boolean isStopped();
    // @formatter:on

    @Shadow public ServerConnectionListener connection;
    // CraftBukkit start
    public WorldLoader.DataLoadContext worldLoader;
    public org.bukkit.craftbukkit.v1_19_R3.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    private static int currentTick = ServerUtils.getCurrentTick();
    public java.util.Queue<Runnable> processQueue = ServerUtils.bridge$processQueue;
    public int autosavePeriod = ServerUtils.bridge$autosavePeriod;
    private boolean forceTicks;
    public Commands vanillaCommandDispatcher;
    private boolean hasStopped = false;
    private final Object stopLock = new Object();
    // CraftBukkit end

    public MixinMinecraftServer(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void banner$initServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        String[] arguments = ManagementFactory.getRuntimeMXBean().getInputArguments().toArray(new String[0]);
        OptionParser parser = new Main();
        try {
            options = parser.parse(arguments);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
        }
        Main.handleParser(parser, options);
        this.vanillaCommandDispatcher = worldStem.dataPackResources().getCommands();
        this.worldLoader = BukkitCaptures.getDataLoadContext();
        // Try to see if we're actually running in a terminal, disable jline if not
        if (System.console() == null && System.getProperty("jline.terminal") == null) {
            System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
            org.bukkit.craftbukkit.Main.useJline = false;
        }
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", remap = false, ordinal = 0, shift = At.Shift.AFTER, target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V"))
    public void banner$unloadPlugins(CallbackInfo ci) {
        if (this.server != null) {
            this.server.disablePlugins();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void banner$stop(CallbackInfo ci) {
        synchronized (this.stopLock) {
            if (this.hasStopped)
                return;
            this.hasStopped = true;
        }
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;removeAll()V"))
    private void banner$stopThread(CallbackInfo ci) {
        try { Thread.sleep(100); } catch (InterruptedException ex) {} // CraftBukkit - SPIGOT-625 - give server at least a chance to send packets
    }

    @ModifyExpressionValue(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;lastOverloadWarning:J", ordinal = 0))
    private boolean banner$resetCKU(MinecraftServer instance) {
        return this.lastOverloadWarning >= 30000L;
    }

    @WrapWithCondition(method = "runServer",remap = false,
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"),
            slice = @Slice(to = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;lastOverloadWarning:J", ordinal = 1)))
    private boolean banner$addCKUCheck() {
        return server.getWarnOnOverload();
    }

    @Inject(method = "getServerModName", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void banner$setServerModName(CallbackInfoReturnable<String> cir) {
        if (this.server != null) {
            cir.setReturnValue(server.getServer().getServerName());
        }
    }

    @Inject(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;nextTickTime:J", shift = At.Shift.BEFORE))
    private void banner$currentTick(CallbackInfo ci) {
        ServerUtils.currentTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit
    }

    @Inject(method = "reloadResources", at = @At(value = "RETURN", target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private void banner$syncCommand(Collection<String> selectedIds, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        this.server.syncCommands();
    }

    @Override
    public boolean hasStopped() {
        synchronized (stopLock) {
            return hasStopped;
        }
    }

    @Override
    public void banner$setServer(CraftServer server) {
        this.server = server;
    }

    private static MinecraftServer getServer() {
        return Bukkit.getServer() instanceof CraftServer ? ((CraftServer) Bukkit.getServer()).getServer() : null;
    }

    @Override
    public void addLevel(ServerLevel level) {
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> oldLevels = this.levels;
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> newLevels = Maps.newLinkedHashMap(oldLevels);
        newLevels.put(level.dimension(), level);
        this.levels = Collections.unmodifiableMap(newLevels);
    }

    @Override
    public void removeLevel(ServerLevel level) {
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> oldLevels = this.levels;
        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> newLevels = Maps.newLinkedHashMap(oldLevels);
        newLevels.remove(level.dimension(), level);
        this.levels = Collections.unmodifiableMap(newLevels);
    }

    @Inject(method = "createLevels",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void banner$callInitEvent(ChunkProgressListener listener, CallbackInfo ci, ServerLevelData serverLevelData, boolean bl, Registry registry, WorldOptions worldOptions, long l, long m, List list, LevelStem levelStem, ServerLevel serverLevel) {
        this.server.getPluginManager().callEvent(new WorldInitEvent(serverLevel.getWorld()));
    }

    @Inject(method = "loadLevel", at = @At("TAIL"))
    private void banner$initPlugins(CallbackInfo ci) {
        this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
        this.server.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
        for (ServerLevel worldserver : ((MinecraftServer)(Object)this).getAllLevels()) {
            worldserver.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API
            this.server.getPluginManager().callEvent(new WorldLoadEvent(worldserver.getWorld()));
            this.connection.acceptConnections();
        }
    }

    @Inject(method = "saveAllChunks", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private void banner$skipSave(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!this.levels.isEmpty());
    }

    @Inject(method = "setInitialSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;getGenerator()Lnet/minecraft/world/level/chunk/ChunkGenerator;", shift = At.Shift.BEFORE), cancellable = true)
    private static void banner$spawnInit(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug, CallbackInfo ci) {
        // CraftBukkit start
        if (level.bridge$generator() != null) {
            Random rand = new Random(level.getSeed());
            org.bukkit.Location spawn = level.bridge$generator().getFixedSpawnLocation(level.getWorld(), rand);

            if (spawn != null) {
                if (spawn.getWorld() != level.getWorld()) {
                    throw new IllegalStateException("Cannot set spawn point for " + levelData.getLevelName() + " to be in another world (" + spawn.getWorld().getName() + ")");
                } else {
                    levelData.setSpawn(new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()), spawn.getYaw());
                    ci.cancel();
                }
            }
        }
    }

    @ModifyExpressionValue(method = "tickServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;tickCount:I", ordinal = 1))
    private boolean banner$addTaskCheck() {
        return autosavePeriod > 0 && this.tickCount % autosavePeriod == 0;
    }

    @Override
    public void initWorld(ServerLevel serverWorld, ServerLevelData worldInfo, WorldData saveData, WorldOptions worldOptions) {
        boolean flag = saveData.isDebugWorld();
        if ((serverWorld.bridge$generator() != null)) {
            serverWorld.getWorld().getPopulators().addAll(
                    serverWorld.bridge$generator().getDefaultPopulators(
                            (serverWorld.getWorld())));
        }
        WorldBorder worldborder = serverWorld.getWorldBorder();
        worldborder.applySettings(worldInfo.getWorldBorder());
        if (!worldInfo.isInitialized()) {
            try {
                setInitialSpawn(serverWorld, worldInfo, worldOptions.generateBonusChest(), flag);
                worldInfo.setInitialized(true);
                if (flag) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverWorld.fillReportDetails(crashreport);
                } catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashreport);
            }
            worldInfo.setInitialized(true);
        }
    }

    @Override
    public void prepareLevels(ChunkProgressListener listener, ServerLevel serverWorld) {
        ServerChunkCache serverchunkprovider = serverWorld.getChunkSource();
        this.forceTicks = true;
        if (serverWorld.getWorld().getKeepSpawnInMemory()) {
            LOGGER.info("Preparing start region for dimension {}", serverWorld.dimension().location());
            BlockPos blockpos = serverWorld.getSharedSpawnPos();
            listener.updateSpawnPos(new ChunkPos(blockpos));
            this.nextTickTime = Util.getMillis();
            serverchunkprovider.addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

            while (serverchunkprovider.getTickingGenerated() < 441) {
                this.executeModerately();
            }
        }
        ForcedChunksSavedData forcedchunkssavedata = serverWorld.getDataStorage().get(ForcedChunksSavedData::load, "chunks");

        if (forcedchunkssavedata != null) {
            LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

            while (longiterator.hasNext()) {
                long i = longiterator.nextLong();
                ChunkPos chunkpos = new ChunkPos(i);
                serverWorld.getChunkSource().updateChunkForced(chunkpos, true);
            }
        }

        this.executeModerately();
        if (serverWorld.getWorld().getKeepSpawnInMemory()) {
            listener.stop();
        }
        serverchunkprovider.getLightEngine().setTaskPerBatch(5);
        serverWorld.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        this.forceTicks = false;
    }

    @Override
    public void executeModerately() {
        this.runAllTasks();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    @Inject(method = "haveTime", cancellable = true, at = @At("HEAD"))
    private void banner$forceAheadOfTime(CallbackInfoReturnable<Boolean> cir) {
        if (this.forceTicks) cir.setReturnValue(true);
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void banner$processStart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        server.getScheduler().mainThreadHeartbeat(this.tickCount);
    }

    @Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private void banner$checkHeart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        // CraftBukkit start
        // Run tasks that are waiting on processing
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }

        // Send time updates to everyone, it will get the right time from the world the player is in.
        if (this.tickCount % 20 == 0) {
            for (int i = 0; i < this.getPlayerList().players.size(); ++i) {
                ServerPlayer entityplayer = (ServerPlayer) this.getPlayerList().players.get(i);
                entityplayer.connection.send(new ClientboundSetTimePacket(entityplayer.level.getGameTime(), entityplayer.getPlayerTime(), entityplayer.level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))); // Add support for per player time
            }
        }
    }

    // CraftBukkit start
    public final java.util.concurrent.ExecutorService chatExecutor = java.util.concurrent.Executors.newCachedThreadPool(
            new com.google.common.util.concurrent.ThreadFactoryBuilder().setDaemon(true).setNameFormat("Async Chat Thread - #%d").build());

    @ModifyReturnValue(method = "getChatDecorator", at = @At("RETURN"))
    private ChatDecorator banner$fireChatEvent() {
        return (entityplayer, ichatbasecomponent) -> {
            // SPIGOT-7127: Console /say and similar
            if (entityplayer == null) {
                return CompletableFuture.completedFuture(ichatbasecomponent);
            }

            return CompletableFuture.supplyAsync(() -> {
                AsyncPlayerChatPreviewEvent event = new AsyncPlayerChatPreviewEvent(true, entityplayer.getBukkitEntity(), CraftChatMessage.fromComponent(ichatbasecomponent), new LazyPlayerSet(((MinecraftServer) (Object) this)));
                String originalFormat = event.getFormat(), originalMessage = event.getMessage();
                this.server.getPluginManager().callEvent(event);

                if (originalFormat.equals(event.getFormat()) && originalMessage.equals(event.getMessage()) && event.getPlayer().getName().equalsIgnoreCase(event.getPlayer().getDisplayName())) {
                    return ichatbasecomponent;
                }
                return CraftChatMessage.fromStringOrNull(String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()));
            }, chatExecutor);
        };
    }

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setWorldBorder(Lnet/minecraft/world/level/border/WorldBorder$Settings;)V"))
    private void banner$cancel0(ServerLevelData instance, WorldBorder.Settings settings) {}

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;setCustomBossEvents(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void banner$cancel1(WorldData instance, CompoundTag compoundTag) {}

    @Redirect(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V"))
    private void banner$cancel2(LevelStorageSource.LevelStorageAccess instance, RegistryAccess registries, WorldData serverConfiguration, CompoundTag hostPlayerNBT) {}

    // Banner start -- add to support plugins
    public String u() {
        return this.localIp;
    }

    public boolean U() {
        return this.onlineMode;
    }

    // Banner end

    // Banner start
    @Override
    public WorldLoader.DataLoadContext bridge$worldLoader() {
        return worldLoader;
    }

    @Override
    public CraftServer bridge$server() {
        return server;
    }

    @Override
    public OptionSet bridge$options() {
        return options;
    }

    @Override
    public ConsoleCommandSender bridge$console() {
        return console;
    }

    @Override
    public RemoteConsoleCommandSender bridge$remoteConsole() {
        return remoteConsole;
    }

    @Override
    public ConsoleReader bridge$reader() {
        return reader;
    }

    @Override
    public boolean bridge$forceTicks() {
        return forceTicks;
    }

    @Override
    public boolean isDebugging() {
        return false;
    }

    @Override
    public void banner$setRemoteConsole(RemoteConsoleCommandSender remoteConsole) {
        this.remoteConsole = remoteConsole;
    }

    @Override
    public void banner$setConsole(ConsoleCommandSender console) {
        this.console = console;
    }

    // Banner end


    @Override
    public void bridge$queuedProcess(Runnable runnable) {
        processQueue.add(runnable);
    }

    @Override
    public Queue<Runnable> bridge$processQueue() {
        return processQueue;
    }

    @Override
    public void banner$setProcessQueue(Queue<Runnable> processQueue) {
        this.processQueue = processQueue;
    }


    @Override
    public Commands bridge$getVanillaCommands() {
        return this.vanillaCommandDispatcher;
    }

    @Override
    public java.util.concurrent.ExecutorService bridge$chatExecutor() {
        return chatExecutor;
    }

    @Override
    public boolean isSameThread() {
        return super.isSameThread() || this.isStopped(); // CraftBukkit - MC-142590
    }
}
