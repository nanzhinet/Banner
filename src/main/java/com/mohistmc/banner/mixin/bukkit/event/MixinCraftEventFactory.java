package com.mohistmc.banner.mixin.bukkit.event;

import com.google.common.base.Function;
import com.mohistmc.banner.bukkit.BukkitSnapshotCaptures;
import com.mohistmc.banner.bukkit.DistValidate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = CraftEventFactory.class, remap = false)
public abstract class MixinCraftEventFactory {

    @Shadow public static Entity entityDamage;

    @Shadow public static Block blockDamage;

    @Shadow
    public static <T extends Event> T callEvent(T event) {
        return null;
    }

    @Shadow
    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<EntityDamageEvent.DamageModifier, Double> modifiers, Map<EntityDamageEvent.DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        return null;
    }

    // Banner start - add DamageSource Types
    private static final ResourceKey<DamageType> MELTING = DamageTypes.ON_FIRE;
    private static final ResourceKey<DamageType> POISON = DamageTypes.MAGIC;
    // Banner end

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<EntityDamageEvent.DamageModifier, Double> modifiers, Map<EntityDamageEvent.DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        // Banner start - compat for mod
        Entity damageEventEntity = BukkitSnapshotCaptures.getDamageEventEntity();
        BlockPos damageEventBlock = BukkitSnapshotCaptures.getDamageEventBlock();
        if (damageEventEntity != null && entityDamage == null) {
            if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                entityDamage = damageEventEntity;
            }
        }
        if (damageEventBlock != null && blockDamage == null) {
            if (source.is(DamageTypes.CACTUS)
                    || source.is(DamageTypes.SWEET_BERRY_BUSH)
                    || source.is(DamageTypes.HOT_FLOOR)) {
                blockDamage = CraftBlock.at(entity.getCommandSenderWorld(), damageEventBlock);
            }
        }
        // Banner end
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            EntityDamageEvent.DamageCause damageCause;
            Entity damager = entityDamage;
            entityDamage = null;
            EntityDamageEvent event;
            if (damager == null) {
                event = new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.BLOCK_EXPLOSION, modifiers, modifierFunctions);
            } else if (entity instanceof EnderDragon && /*PAIL FIXME ((EnderDragon) entity).target == damager*/ false) {
                event = new EntityDamageEvent(entity.getBukkitEntity(), DamageCause.ENTITY_EXPLOSION, modifiers, modifierFunctions);
            } else {
                if (damager instanceof org.bukkit.entity.TNTPrimed) {
                    damageCause = DamageCause.BLOCK_EXPLOSION;
                } else {
                    damageCause = DamageCause.ENTITY_EXPLOSION;
                }
                event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), entity.getBukkitEntity(), damageCause, modifiers, modifierFunctions);
            }
            event.setCancelled(cancelled);

            callEvent(event);

            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.banner$setLastDamageCancelled(true); // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        } else if (source.getEntity() != null || source.getDirectEntity() != null) {
            Entity damager = source.getEntity();
            DamageCause cause = (source.isSweep()) ? DamageCause.ENTITY_SWEEP_ATTACK : DamageCause.ENTITY_ATTACK;

            if (source.isIndirect() && source.getDirectEntity() != null) {
                damager = source.getDirectEntity();
            }

            if (damager instanceof net.minecraft.world.entity.projectile.Projectile) {
                if (damager.getBukkitEntity() instanceof org.bukkit.entity.ThrownPotion) {
                    cause = DamageCause.MAGIC;
                } else if (damager.getBukkitEntity() instanceof Projectile) {
                    cause = DamageCause.PROJECTILE;
                }
            } else if (source.is(DamageTypes.THORNS)) {
                cause = DamageCause.THORNS;
            } else if (source.is(DamageTypes.SONIC_BOOM)) {
                cause = DamageCause.SONIC_BOOM;
            }

            return callEntityDamageEvent(damager, entity, cause, modifiers, modifierFunctions, cancelled);
        } else if (source.is(DamageTypes.OUTSIDE_BORDER)) {
            EntityDamageEvent event = new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.VOID, modifiers, modifierFunctions);
            event.setCancelled(cancelled);
            callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.banner$setLastDamageCancelled(true); // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        } else if (source.is(DamageTypes.LAVA)) {
            EntityDamageEvent event = (new EntityDamageByBlockEvent(blockDamage, entity.getBukkitEntity(), DamageCause.LAVA, modifiers, modifierFunctions));
            event.setCancelled(cancelled);

            Block damager = blockDamage;
            blockDamage = null; // SPIGOT-6639: Clear blockDamage to allow other entity damage during event call
            callEvent(event);
            blockDamage = damager; // SPIGOT-6639: Re-set blockDamage so that other entities which are also getting damaged have the right cause
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            }
            return event;
        } else if (blockDamage != null) {
            DamageCause cause = null;
            Block damager = blockDamage;
            if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH) || source.is(DamageTypes.STALAGMITE) || source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.FALLING_ANVIL)) {
                cause = DamageCause.CONTACT;
            } else if (source.is(DamageTypes.HOT_FLOOR)) {
                cause = DamageCause.HOT_FLOOR;
            } else if (source.is(DamageTypes.MAGIC)) {
                cause = DamageCause.MAGIC;
            } else if (source.is(DamageTypes.IN_FIRE)) {
                cause = DamageCause.FIRE;
                // Banner start - handle missing damage types
            } else if (source.is(DamageTypes.ON_FIRE)) {
                cause = DamageCause.FIRE_TICK;
            } else if (source.is(DamageTypes.LAVA)) {
                cause = DamageCause.LAVA;
            } else if (damager instanceof LightningStrike) {
                cause = DamageCause.LIGHTNING;
            } else if (source.is(MELTING)) {
                cause = DamageCause.MELTING;
            } else if (source.is(POISON)) {
                cause = DamageCause.POISON;
            } else if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                cause = DamageCause.LIGHTNING;
            } else if (source.is(DamageTypes.GENERIC)) {
                cause = DamageCause.CUSTOM;
            } else {
                cause = DamageCause.CUSTOM; // Banner - handle unknown cause
            }
            // Banner end

            EntityDamageEvent event = new EntityDamageByBlockEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions);
            event.setCancelled(cancelled);
            blockDamage = null; // SPIGOT-6639: Clear blockDamage to allow other entity damage during event call
            callEvent(event);
            blockDamage = damager; // SPIGOT-6639: Re-set blockDamage so that other entities which are also getting damaged have the right cause
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.banner$setLastDamageCancelled(true); // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        } else if (entityDamage != null) {
            DamageCause cause = null;
            CraftEntity damager = entityDamage.getBukkitEntity();
            entityDamage = null;
            if (source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL)) {
                cause = DamageCause.FALLING_BLOCK;
            } else if (damager instanceof LightningStrike) {
                cause = DamageCause.LIGHTNING;
            } else if (source.is(DamageTypes.FALL)) {
                cause = DamageCause.FALL;
            } else if (source.is(DamageTypes.DRAGON_BREATH)) {
                cause = DamageCause.DRAGON_BREATH;
            } else if (source.is(DamageTypes.MAGIC)) {
                cause = DamageCause.MAGIC;
                // Banner start - handle missing damage types
            } else if (source.is(DamageTypes.CACTUS)) {
                cause = DamageCause.CONTACT;
            } else if (source.is(DamageTypes.IN_FIRE)) {
                cause = DamageCause.FIRE;
            } else if (source.is(DamageTypes.ON_FIRE)) {
                cause = DamageCause.FIRE_TICK;
            } else if (source.is(DamageTypes.LAVA)) {
                cause = DamageCause.LAVA;
            } else if (source.is(MELTING)) {
                cause = DamageCause.MELTING;
            } else if (source.is(POISON)) {
                cause = DamageCause.POISON;
            } else {
                cause = DamageCause.CUSTOM; // Banner - handle unknown cause
            }
            // Banner end
            EntityDamageEvent event = new EntityDamageByEntityEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions);
            event.setCancelled(cancelled);
            callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.banner$setLastDamageCancelled(true); // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        }

        DamageCause cause = null;
        if (source.is(DamageTypes.IN_FIRE)) {
            cause = DamageCause.FIRE;
        } else if (source.is(DamageTypes.STARVE)) {
            cause = DamageCause.STARVATION;
        } else if (source.is(DamageTypes.WITHER)) {
            cause = DamageCause.WITHER;
        } else if (source.is(DamageTypes.IN_WALL)) {
            cause = DamageCause.SUFFOCATION;
        } else if (source.is(DamageTypes.DROWN)) {
            cause = DamageCause.DROWNING;
        } else if (source.is(DamageTypes.ON_FIRE)) {
            cause = DamageCause.FIRE_TICK;
        } else if (source.isMelting()) {
            cause = DamageCause.MELTING;
        } else if (source.isPoison()) {
            cause = DamageCause.POISON;
        } else if (source.is(DamageTypes.MAGIC)) {
            cause = DamageCause.MAGIC;
        } else if (source.is(DamageTypes.FALL)) {
            cause = DamageCause.FALL;
        } else if (source.is(DamageTypes.FLY_INTO_WALL)) {
            cause = DamageCause.FLY_INTO_WALL;
        } else if (source.is(DamageTypes.CRAMMING)) {
            cause = DamageCause.CRAMMING;
        } else if (source.is(DamageTypes.DRY_OUT)) {
            cause = DamageCause.DRYOUT;
        } else if (source.is(DamageTypes.FREEZE)) {
            cause = DamageCause.FREEZE;
        } else if (source.is(DamageTypes.GENERIC_KILL)) {
            cause = DamageCause.KILL;
        } else if (source.is(DamageTypes.OUTSIDE_BORDER)) {
            cause = DamageCause.WORLD_BORDER;
        } else {
            cause = DamageCause.CUSTOM;
        }

        if (cause != null) {
            return callEntityDamageEvent(null, entity, cause, modifiers, modifierFunctions, cancelled);
        }

        // Banner start - handle damage
        // todo blockDamage is lost
        EntityDamageEvent event;
        if (source.getEntity() != null) {
            event = new EntityDamageByEntityEvent(source.getEntity().getBukkitEntity(), entity.getBukkitEntity(), EntityDamageEvent.DamageCause.CUSTOM, modifiers, modifierFunctions);
        } else {
            event = new EntityDamageEvent(entity.getBukkitEntity(), EntityDamageEvent.DamageCause.CUSTOM, modifiers, modifierFunctions);
        }
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            entity.getBukkitEntity().setLastDamageCause(event);
        }
        return event;
        // Banner end
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static boolean handleBlockSpreadEvent(LevelAccessor world, BlockPos source, BlockPos target, net.minecraft.world.level.block.state.BlockState block, int flag) {
        // Suppress during worldgen
        if (!(world instanceof Level) || !DistValidate.isValid(world)) {
            world.setBlock(target, block, flag);
            return true;
        }

        CraftBlockState state = CraftBlockStates.getBlockState(world, target, flag);
        state.setData(block);

        BlockSpreadEvent event = new BlockSpreadEvent(state.getBlock(), CraftBlock.at(world, source), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newData, int flag) {
        // Suppress during worldgen
        if (!DistValidate.isValid(world)) {
            world.setBlock(pos, newData, flag);
            return true;
        }
        Block block = world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setData(newData);

        BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag, @Nullable Entity entity) {
        // Suppress during worldgen
        if (!DistValidate.isValid(world)) {
            world.setBlock(pos, block, flag);
            return true;
        }
        CraftBlockState blockState = CraftBlockStates.getBlockState(world, pos, flag);
        blockState.setData(block);

        BlockFormEvent event = (entity == null) ? new BlockFormEvent(blockState.getBlock(), blockState) : new EntityBlockFormEvent(entity.getBukkitEntity(), blockState.getBlock(), blockState);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            blockState.update(true);
        }

        return !event.isCancelled();
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static BlockFadeEvent callBlockFadeEvent(LevelAccessor world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock) {
        // Suppress during worldgen
        if (!(world instanceof Level) || !DistValidate.isValid(world)) {
            return new BlockFadeEvent(CraftBlock.at(world, pos), CraftBlockStates.getBlockState(CraftMagicNumbers.getMaterial(newBlock.getBlock()), null));
        }
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos);
        state.setData(newBlock);

        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static BlockPhysicsEvent callBlockPhysicsEvent(LevelAccessor world, BlockPos blockposition) {
        org.bukkit.block.Block block = CraftBlock.at(world, blockposition);
        BlockPhysicsEvent event = new BlockPhysicsEvent(block, block.getBlockData());
        // Suppress during worldgen
        if (world instanceof Level && DistValidate.isValid(world)) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return event;
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock, boolean cancelled) {
        Block block = CraftBlock.at(entity.level(), position);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity.getBukkitEntity(), block, CraftBlockData.fromData(newBlock));
        event.setCancelled(cancelled);
        // Suppress during worldgen
        if (DistValidate.isValid(entity.level())) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return !event.isCancelled();
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static BlockRedstoneEvent callRedstoneChange(Level world, BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(CraftBlock.at(world, pos), oldCurrent, newCurrent);
        // Suppress during worldgen
        if (DistValidate.isValid(world)) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return event;
    }

    /**
     * @author wdog5
     * @reason banner things
     */
    @Overwrite
    public static NotePlayEvent callNotePlayEvent(Level world, BlockPos pos, NoteBlockInstrument instrument, int note) {
        NotePlayEvent event = new NotePlayEvent(CraftBlock.at(world, pos), org.bukkit.Instrument.getByType((byte) instrument.ordinal()), new org.bukkit.Note(note));
        // Suppress during worldgen
        if (DistValidate.isValid(world)) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return event;
    }

    @Inject(method = "callItemSpawnEvent", cancellable = true, at = @At("HEAD"))
    private static void banner$noAirDrops(ItemEntity itemEntity, CallbackInfoReturnable<ItemSpawnEvent> cir) {
        if (itemEntity.getItem().isEmpty()) {
            Item entity = (Item) itemEntity.getBukkitEntity();
            ItemSpawnEvent event = new ItemSpawnEvent(entity);
            event.setCancelled(true);
            cir.setReturnValue(event);
        }
    }
}
