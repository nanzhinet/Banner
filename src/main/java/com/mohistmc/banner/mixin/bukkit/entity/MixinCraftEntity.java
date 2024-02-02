package com.mohistmc.banner.mixin.bukkit.entity;

import com.mohistmc.banner.bukkit.entity.MohistModsAbstractHorse;
import com.mohistmc.banner.bukkit.entity.MohistModsChestHorse;
import com.mohistmc.banner.bukkit.entity.MohistModsEntity;
import com.mohistmc.banner.bukkit.entity.MohistModsMinecart;
import com.mohistmc.banner.bukkit.entity.MohistModsMinecartContainer;
import com.mohistmc.banner.bukkit.entity.MohistModsMinecraft;
import com.mohistmc.banner.bukkit.entity.MohistModsMob;
import com.mohistmc.banner.bukkit.entity.MohistModsMonster;
import com.mohistmc.banner.bukkit.entity.MohistModsProjectileEntity;
import com.mohistmc.banner.bukkit.entity.MohistModsRaider;
import com.mohistmc.banner.bukkit.entity.MohistModsSkeleton;
import com.mohistmc.banner.bukkit.entity.MohistModsTameableEntity;
import com.mohistmc.banner.bukkit.entity.MohistModsThrowableEntity;
import com.mohistmc.banner.bukkit.entity.MohistModsThrowableProjectile;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftFlying;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftGolem;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTameableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftEntity.class, remap = false)
public class MixinCraftEntity {

    @Inject(method = "getEntity", cancellable = true, at = @At(value = "NEW", args = "class=java/lang/AssertionError"))
    private static void banner$modEntity(CraftServer server, Entity entity, CallbackInfoReturnable<CraftEntity> cir) {
        if (entity instanceof LivingEntity) {
            if (entity instanceof Mob) {
                if (entity instanceof AgeableMob) {
                    if (entity instanceof AbstractHorse) {
                        if (entity instanceof AbstractChestedHorse) {
                            cir.setReturnValue(new MohistModsChestHorse(server, (AbstractChestedHorse) entity));
                            return;
                        }
                        cir.setReturnValue(new MohistModsAbstractHorse(server, (AbstractHorse) entity));
                        return;
                    }
                    if (entity instanceof TamableAnimal) {
                        cir.setReturnValue(new CraftTameableAnimal(server, (TamableAnimal) entity));
                        return;
                    }
                    cir.setReturnValue(new CraftAgeable(server, (AgeableMob) entity));
                    return;
                }
                if (entity instanceof FlyingMob) {
                    cir.setReturnValue(new CraftFlying(server, (FlyingMob) entity));
                    return;
                }
                if (entity instanceof Raider) {
                    cir.setReturnValue(new MohistModsRaider(server, (Raider) entity));
                    return;
                }
                if (entity instanceof AbstractGolem) {
                    cir.setReturnValue(new CraftGolem(server, (AbstractGolem) entity));
                    return;
                }
                cir.setReturnValue(new MohistModsMob(server, (Mob) entity));
                return;
            }
            cir.setReturnValue(new CraftLivingEntity(server, (LivingEntity) entity));
            return;
        }
        if (entity instanceof AbstractMinecart) {
            if (entity instanceof AbstractMinecartContainer) {
                cir.setReturnValue(new MohistModsMinecartContainer(server, (AbstractMinecartContainer) entity));
                return;
            }
            cir.setReturnValue(new MohistModsMinecart(server, (AbstractMinecart) entity));
            return;
        }
        if (entity instanceof Projectile) {
            cir.setReturnValue(new MohistModsProjectileEntity(server, (Projectile) entity));
            return;
        }
        if (entity instanceof AbstractSkeleton) {
            cir.setReturnValue(new MohistModsSkeleton(server, (AbstractSkeleton) entity));
            return;
        }
        if (entity instanceof ThrowableItemProjectile) {
            cir.setReturnValue(new MohistModsThrowableProjectile(server, (ThrowableItemProjectile) entity));
            return;
        }

        if (entity instanceof ThrowableProjectile) {
            cir.setReturnValue(new MohistModsThrowableEntity(server, (ThrowableProjectile) entity));
            return;
        }
        if (entity instanceof TamableAnimal) {
            cir.setReturnValue(new MohistModsTameableEntity(server, (TamableAnimal) entity));
            return;
        }
        cir.setReturnValue(new MohistModsEntity(server, entity));
    }
}
