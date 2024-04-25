package com.mohistmc.banner.mixin.core.interaction.dispenser;

import com.mohistmc.banner.bukkit.BukkitExtraConstants;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.v1_20_R4.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShearsDispenseItemBehavior.class)
public abstract class MixinShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {

    private static transient org.bukkit.block.Block banner$bukkitBlock;
    private static transient CraftItemStack banner$craftItem;
    @Shadow
    protected static boolean tryShearBeehive(ServerLevel level, BlockPos pos) {
        return false;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel serverLevel = blockSource.level();
        // CraftBukkit start
        org.bukkit.block.Block bukkitBlock = serverLevel.getWorld().getBlockAt(blockSource.pos().getX(), blockSource.pos().getY(), blockSource.pos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack);

        BlockDispenseEvent event = new BlockDispenseEvent(bukkitBlock, craftItem.clone(), new org.bukkit.util.Vector(0, 0, 0));
        if (!BukkitExtraConstants.dispenser_eventFired) {
            serverLevel.getCraftServer().getPluginManager().callEvent(event);
        }

        if (event.isCancelled()) {
            return itemStack;
        }

        if (!event.getItem().equals(craftItem)) {
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(blockSource, eventStack);
                return itemStack;
            }
        }
        // CraftBukkit end
        if (!serverLevel.isClientSide()) {
            BlockPos blockPos = blockSource.pos().relative((Direction)blockSource.state().getValue(DispenserBlock.FACING));
            this.setSuccess(tryShearBeehive(serverLevel, blockPos) || tryShearLivingEntity(serverLevel, blockPos));
            if (this.isSuccess()) {
                itemStack.hurtAndBreak(1, serverLevel.getRandom(), (ServerPlayer)null, () -> {
                    itemStack.setCount(0);
                });
            }
        }

        return itemStack;
    }

    private static boolean tryShearLivingEntity(ServerLevel worldserver, BlockPos blockposition, org.bukkit.block.Block bukkitBlock, CraftItemStack craftItem) { // CraftBukkit - add args
        banner$bukkitBlock = bukkitBlock;
        banner$craftItem = craftItem;
        return tryShearLivingEntity(worldserver, blockposition);
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    private static boolean tryShearLivingEntity(ServerLevel level, BlockPos pos) {
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), EntitySelector.NO_SPECTATORS);

        for (LivingEntity livingEntity : list) {
            if (livingEntity instanceof Shearable shearable) {
                if (shearable.readyForShearing()) {
                    // CraftBukkit start
                    if (CraftEventFactory.callBlockShearEntityEvent(livingEntity, banner$bukkitBlock, banner$craftItem).isCancelled()) {
                        shearable.shear(SoundSource.BLOCKS);
                        level.gameEvent((Entity) null, GameEvent.SHEAR, pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
