package com.mohistmc.banner.mixin.bukkit;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.plugins.BannerPlugin;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import jline.console.ConsoleReader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.command.CraftCommandMap;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.v1_20_R3.help.SimpleHelpMap;
import org.bukkit.craftbukkit.v1_20_R3.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.v1_20_R3.util.permissions.CraftDefaultPermissions;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.util.permissions.DefaultPermissions;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mixin(value = CraftServer.class, remap = false)
public abstract class MixinCraftServer {

    @Mutable
    @Shadow @Final private String serverName;

    @Mutable
    @Shadow @Final private String serverVersion;

    @Shadow protected abstract void setVanillaCommands(boolean first);

    @Shadow @Final private CraftCommandMap commandMap;

    @Shadow protected abstract void loadCustomPermissions();

    @Shadow @Final private SimpleHelpMap helpMap;

    @Shadow public abstract void syncCommands();

    @Shadow protected abstract void enablePlugin(Plugin plugin);

    @Shadow @Final private SimplePluginManager pluginManager;

    @Mutable
    @Shadow @Final private List<CraftPlayer> playerView;

    @Shadow @Final protected DedicatedPlayerList playerList;

    @Shadow @Final protected DedicatedServer console;

    @Shadow public abstract Logger getLogger();

    @Shadow @Final private Map<String, World> worlds;

    @Shadow public abstract File getWorldContainer();

    @Shadow public abstract World getWorld(String name);

    @Shadow public abstract ChunkGenerator getGenerator(String world);

    @Shadow public abstract BiomeProvider getBiomeProvider(String world);

    @Shadow public abstract GameMode getDefaultGameMode();

    @Shadow public abstract DedicatedServer getServer();

    @Shadow protected abstract File getCommandsConfigFile();

    @Shadow private YamlConfiguration commandsConfiguration;

    @Shadow private YamlConfiguration configuration;

    @Shadow protected abstract File getConfigFile();

    @Shadow public int reloadCount;

    @Shadow @Final private Logger logger;

    @Shadow public abstract void reloadData();

    @Shadow private boolean overrideAllCommandBlockCommands;

    @Shadow public boolean ignoreVanillaPermissions;

    @Shadow public abstract CraftScheduler getScheduler();
    @Shadow public abstract PluginManager getPluginManager();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void banner$setBrand(DedicatedServer console, PlayerList playerList, CallbackInfo ci) {
        this.serverName = "Banner";
        this.serverVersion = BannerMCStart.getVersion();
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public String getVersion() {
        return BannerMCStart.getVersion() + " (MC: " + this.console.getServerVersion() + ")";
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public void enablePlugins(PluginLoadOrder type) {
        if (type == PluginLoadOrder.STARTUP) {
            helpMap.clear();
            helpMap.initializeGeneralTopics();
        }

        Plugin[] plugins = pluginManager.getPlugins();

        for (Plugin plugin : plugins) {
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type)) {
                enablePlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            BannerPlugin.init(); // Banner
            // Spigot start - Allow vanilla commands to be forced to be the main command
            setVanillaCommands(true);
            commandMap.setFallbackCommands();
            setVanillaCommands(false);
            // Spigot end
            commandMap.registerServerAliases();
            DefaultPermissions.registerCorePermissions();
            CraftDefaultPermissions.registerCorePermissions();
            loadCustomPermissions();
            helpMap.initializeCommands();
            syncCommands();
        }
    }

    @Inject(method = "getOfflinePlayers", at = @At("HEAD"))
    private void banner$refreshPlayerAllTime(CallbackInfoReturnable<OfflinePlayer[]> cir) {
        // Banner start - refresh online players
        this.playerView = Collections.unmodifiableList(Lists.transform(playerList.players, new Function<ServerPlayer, CraftPlayer>() {
            @Override
            public CraftPlayer apply(ServerPlayer player) {
                return player.getBukkitEntity();
            }
        }));
        // Banner end
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public boolean unloadWorld(World world, boolean save) {
        if (world == null) {
            return false;
        } else {
            ServerLevel handle = ((CraftWorld)world).getHandle();
            if (this.console.getLevel(handle.dimension()) == null) {
                return false;
            } else if (handle.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                return false;
            } else if (handle.players().size() > 0) {
                return false;
            } else {
                WorldUnloadEvent e = new WorldUnloadEvent(handle.getWorld());
                this.pluginManager.callEvent(e);
                if (e.isCancelled()) {
                    return false;
                } else {
                    try {
                        if (save) {
                            handle.save((ProgressListener)null, true, true);
                        }

                        handle.getChunkSource().close(save);
                        handle.entityManager.close(save);
                        handle.bridge$convertable().close();
                    } catch (Exception var6) {
                        this.getLogger().log(Level.SEVERE, (String)null, var6);
                    }

                    this.worlds.remove(world.getName().toLowerCase(Locale.ROOT)); // Banner - use Root instead of English
                    this.console.removeLevel(handle);
                    return true;
                }
            }
        }
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public World createWorld(WorldCreator creator) {
        Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");
        Preconditions.checkArgument(creator != null, "WorldCreator cannot be null");

        String name = creator.name();
        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(getWorldContainer(), name);
        World world = getWorld(name);

        if (world != null) {
            return world;
        }

        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name);
        }

        if (generator == null) {
            generator = getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension;
        switch (creator.environment()) {
            case NORMAL:
                actualDimension = LevelStem.OVERWORLD;
                break;
            case NETHER:
                actualDimension = LevelStem.NETHER;
                break;
            case THE_END:
                actualDimension = LevelStem.END;
                break;
            default:
                throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        }

        LevelStorageSource.LevelStorageAccess worldSession;
        try {
            worldSession = LevelStorageSource.createDefault(getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
        } catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }

        Dynamic<?> dynamic;
        if (worldSession.hasWorldData()) {
            net.minecraft.world.level.storage.LevelSummary worldinfo;

            try {
                dynamic = worldSession.getDataTag();
                worldinfo = worldSession.getSummary(dynamic);
            } catch (NbtException | ReportedNbtException | IOException ioexception) {
                LevelStorageSource.LevelDirectory convertable_b = worldSession.getLevelDirectory();

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception);
                MinecraftServer.LOGGER.info("Attempting to use fallback");

                try {
                    dynamic = worldSession.getDataTagFallback();
                    worldinfo = worldSession.getSummary(dynamic);
                } catch (NbtException | ReportedNbtException | IOException ioexception1) {
                    MinecraftServer.LOGGER.error("Failed to load world data from {}", convertable_b.oldDataFile(), ioexception1);
                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", convertable_b.dataFile(), convertable_b.oldDataFile());
                    return null;
                }

                worldSession.restoreLevelDataFromOld();
            }

            if (worldinfo.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return null;
            }

            if (!worldinfo.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return null;
            }
        } else {
            dynamic = null;
        }

        boolean hardcore = creator.hardcore();

        PrimaryLevelData worlddata;
        WorldLoader.DataLoadContext worldloader_a = console.bridge$worldLoader();
        Registry<LevelStem> iregistry = worldloader_a.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
        if (dynamic != null) {
            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen());
            worlddata = (PrimaryLevelData) leveldataanddimensions.worldData();
            iregistry = leveldataanddimensions.dimensions().dimensions();
        } else {
            LevelSettings worldsettings;
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            WorldDimensions worlddimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));

            worldsettings = new LevelSettings(name, GameType.byId(getDefaultGameMode().getValue()), hardcore, Difficulty.EASY, false, new GameRules(), worldloader_a.dataConfiguration());
            worlddimensions = properties.create(worldloader_a.datapackWorldgen());

            WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(iregistry);
            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());

            worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
            iregistry = worlddimensions_b.dimensions();
        }
        worlddata.banner$setCustomDimensions(iregistry);
        worlddata.checkName(name);
        worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        if (console.bridge$options().has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(worldSession, DataFixers.getDataFixer(), console.bridge$options().has("eraseCache"), () -> true, iregistry);
        }

        long j = BiomeManager.obfuscateSeed(worlddata.worldGenOptions().seed()); // Paper - use world seed
        List<CustomSpawner> list = ImmutableList.of(new PatrolSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
        LevelStem worlddimension = iregistry.get(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), worlddimension.type().value());
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        ResourceKey<net.minecraft.world.level.Level> worldKey;
        String levelName = this.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            worldKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            worldKey = net.minecraft.world.level.Level.END;
        } else {
            worldKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(name.toLowerCase(java.util.Locale.ENGLISH)));
        }

        ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worlddimension, getServer().progressListenerFactory.create(11),
                worlddata.isDebugWorld(), j, creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(), true, console.overworld().getRandomSequences());
        if (name.contains("/")) {
            String[] strings = name.split("/");
            name = strings[strings.length - 1];
        }
        internal.banner$setGenerator(generator);
        internal.banner$setBiomeProvider(biomeProvider);
        if (!(worlds.containsKey(name.toLowerCase(java.util.Locale.ENGLISH)))) {
            return null;
        }

        console.initWorld(internal, worlddata, worlddata, worlddata.worldGenOptions());

        internal.setSpawnSettings(true, true);
        console.addLevel(internal);

        getServer().prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);
        internal.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API

        pluginManager.callEvent(new WorldLoadEvent(internal.getWorld()));
        ServerWorldEvents.LOAD.invoker().onWorldLoad(console, internal);// Banner - add for fabric loading
        BiomeModificationImpl.INSTANCE.finalizeWorldGen(console.registryAccess());// Banner - add for fabric api
        return internal.getWorld();
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite(remap = false)
    public String getName() {
        return "Banner";
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite(remap = false)
    public ConsoleReader getReader() {
        return null;
    }

    @Inject(method = "dispatchCommand", remap = false, cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lorg/spigotmc/AsyncCatcher;catchOp(Ljava/lang/String;)V"))
    private void banner$returnIfFail(CommandSender sender, String commandLine, CallbackInfoReturnable<Boolean> cir) {
        if (commandLine == null) {
            cir.setReturnValue(false);
        }
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite(remap = false)
    public void reload() {
        ++this.reloadCount;
        this.configuration = YamlConfiguration.loadConfiguration(this.getConfigFile());
        this.commandsConfiguration = YamlConfiguration.loadConfiguration(this.getCommandsConfigFile());

        try {
            this.playerList.getIpBans().load();
        } catch (IOException var12) {
            this.logger.log(Level.WARNING, "Failed to load banned-ips.json, " + var12.getMessage());
        }

        try {
            this.playerList.getBans().load();
        } catch (IOException var11) {
            this.logger.log(Level.WARNING, "Failed to load banned-players.json, " + var11.getMessage());
        }

        this.pluginManager.clearPlugins();
        this.commandMap.clearCommands();
        this.reloadData();
        SpigotConfig.registerCommands();
        this.overrideAllCommandBlockCommands = this.commandsConfiguration.getStringList("command-block-overrides").contains("*");
        this.ignoreVanillaPermissions= this.commandsConfiguration.getBoolean("ignore-vanilla-permissions");

        for (int pollCount = 0; pollCount < 50 && this.getScheduler().getActiveWorkers().size() > 0; ++pollCount) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException var10) {
            }
        }

        List<BukkitWorker> overdueWorkers = this.getScheduler().getActiveWorkers();

        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            this.getLogger().log(Level.SEVERE, String.format("Nag author(s): '%s' of '%s' about the following: %s", plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), "This plugin is not properly shutting down its async tasks when it is being reloaded.  This may cause conflicts with the newly loaded version of the plugin"));
        }

        this.loadPlugins();
        this.enablePlugins(PluginLoadOrder.STARTUP);
        this.enablePlugins(PluginLoadOrder.POSTWORLD);
        this.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.RELOAD));
        ServerLifecycleEvents.SERVER_STARTING.invoker().onServerStarting(console);// Banner - add for fire fabric lifecycle event
    }

    /**
     * @author wdog5
     * @reason custom modify
     */
    @Overwrite
    public void loadPlugins() {
        this.pluginManager.registerInterface(JavaPluginLoader.class);
        File pluginFolder = (File)this.console.bridge$options().valueOf("plugins");
        if (pluginFolder.exists()) {
            Plugin[] plugins = this.pluginManager.loadPlugins(pluginFolder);
            Plugin[] var6 = plugins;
            int var5 = plugins.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                Plugin plugin = var6[var4];

                try {
                    String message = String.format(BannerMCStart.I18N.as("bukkit.plugin.loading"), plugin.getDescription().getFullName());
                    plugin.getLogger().info(message);
                    plugin.onLoad();
                } catch (Throwable var8) {
                    Logger.getLogger(CraftServer.class.getName()).log(Level.SEVERE, var8.getMessage() + " " + BannerMCStart.I18N.as("bukkit.plugin.initializing") + " " + plugin.getDescription().getFullName() + " " + BannerMCStart.I18N.as("bukkit.plugin.ifUpToDate"), var8);
                }
            }
        } else {
            pluginFolder.mkdir();
        }

    }
}
