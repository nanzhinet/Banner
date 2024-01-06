package org.bukkit.craftbukkit.v1_20_R3.potion;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class CraftPotionUtil {

    public static Map<PotionType, PotionType> mods = new HashMap<>();

    private static final BiMap<PotionType, PotionType> regular = ImmutableBiMap.<PotionType, PotionType>builder()
            .put(PotionType.UNCRAFTABLE, PotionType.UNCRAFTABLE)
            .put(PotionType.WATER, PotionType.WATER)
            .put(PotionType.MUNDANE, PotionType.MUNDANE)
            .put(PotionType.THICK, PotionType.THICK)
            .put(PotionType.AWKWARD, PotionType.AWKWARD)
            .put(PotionType.NIGHT_VISION, PotionType.NIGHT_VISION)
            .put(PotionType.INVISIBILITY, PotionType.INVISIBILITY)
            .put(PotionType.JUMP, PotionType.JUMP)
            .put(PotionType.FIRE_RESISTANCE, PotionType.FIRE_RESISTANCE)
            .put(PotionType.SPEED, PotionType.SPEED)
            .put(PotionType.SLOWNESS, PotionType.SLOWNESS)
            .put(PotionType.WATER_BREATHING, PotionType.WATER_BREATHING)
            .put(PotionType.INSTANT_HEAL, PotionType.INSTANT_HEAL)
            .put(PotionType.INSTANT_DAMAGE, PotionType.INSTANT_DAMAGE)
            .put(PotionType.POISON, PotionType.POISON)
            .put(PotionType.REGEN, PotionType.REGEN)
            .put(PotionType.STRENGTH, PotionType.STRENGTH)
            .put(PotionType.WEAKNESS, PotionType.WEAKNESS)
            .put(PotionType.LUCK, PotionType.LUCK)
            .put(PotionType.TURTLE_MASTER, PotionType.TURTLE_MASTER)
            .put(PotionType.SLOW_FALLING, PotionType.SLOW_FALLING)
            .build();
    private static final BiMap<PotionType, PotionType> upgradeable = ImmutableBiMap.<PotionType, PotionType>builder()
            .put(PotionType.JUMP, PotionType.STRONG_LEAPING)
            .put(PotionType.SPEED, PotionType.STRONG_SWIFTNESS)
            .put(PotionType.INSTANT_HEAL, PotionType.STRONG_HEALING)
            .put(PotionType.INSTANT_DAMAGE, PotionType.STRONG_HARMING)
            .put(PotionType.POISON, PotionType.STRONG_POISON)
            .put(PotionType.REGEN, PotionType.STRONG_REGENERATION)
            .put(PotionType.STRENGTH, PotionType.STRONG_STRENGTH)
            .put(PotionType.SLOWNESS, PotionType.STRONG_SLOWNESS)
            .put(PotionType.TURTLE_MASTER, PotionType.STRONG_TURTLE_MASTER)
            .build();
    private static final BiMap<PotionType, PotionType> extendable = ImmutableBiMap.<PotionType, PotionType>builder()
            .put(PotionType.NIGHT_VISION, PotionType.LONG_NIGHT_VISION)
            .put(PotionType.INVISIBILITY, PotionType.LONG_INVISIBILITY)
            .put(PotionType.JUMP, PotionType.LONG_LEAPING)
            .put(PotionType.FIRE_RESISTANCE, PotionType.LONG_FIRE_RESISTANCE)
            .put(PotionType.SPEED, PotionType.LONG_SWIFTNESS)
            .put(PotionType.SLOWNESS, PotionType.LONG_SLOWNESS)
            .put(PotionType.WATER_BREATHING, PotionType.LONG_WATER_BREATHING)
            .put(PotionType.POISON, PotionType.LONG_POISON)
            .put(PotionType.REGEN, PotionType.LONG_REGENERATION)
            .put(PotionType.STRENGTH, PotionType.LONG_STRENGTH)
            .put(PotionType.WEAKNESS, PotionType.LONG_WEAKNESS)
            .put(PotionType.TURTLE_MASTER, PotionType.LONG_TURTLE_MASTER)
            .put(PotionType.SLOW_FALLING, PotionType.LONG_SLOW_FALLING)
            .build();

    public static PotionType fromBukkit(PotionData data) {
        if (mods.containsKey(data.getType())) {
            return mods.get(data.getType());
        }
        PotionType type;
        if (data.isUpgraded()) {
            type = upgradeable.get(data.getType());
        } else if (data.isExtended()) {
            type = extendable.get(data.getType());
        } else {
            type = regular.get(data.getType());
        }
        Preconditions.checkNotNull(type, "Unknown potion type from data " + data);

        return type;
    }

    public static PotionData toBukkit(PotionType type) {
        if (type == null) {
            return new PotionData(PotionType.UNCRAFTABLE, false, false);
        }

        PotionType potionType;
        potionType = extendable.inverse().get(type);
        if (potionType != null) {
            return new PotionData(potionType, true, false);
        }
        potionType = upgradeable.inverse().get(type);
        if (potionType != null) {
            return new PotionData(potionType, false, true);
        }

        return new PotionData(type, false, false);
    }

    public static MobEffectInstance fromBukkit(PotionEffect effect) {
        MobEffect type = CraftPotionEffectType.bukkitToMinecraft(effect.getType());
        return new MobEffectInstance(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles());
    }

    public static PotionEffect toBukkit(MobEffectInstance effect) {
        PotionEffectType type = CraftPotionEffectType.minecraftToBukkit(effect.getEffect());
        int amp = effect.getAmplifier();
        int duration = effect.getDuration();
        boolean ambient = effect.isAmbient();
        boolean particles = effect.isVisible();
        return new PotionEffect(type, duration, amp, ambient, particles);
    }

    public static boolean equals(MobEffect mobEffect, PotionEffectType type) {
        PotionEffectType typeV = CraftPotionEffectType.minecraftToBukkit(mobEffect);
        return typeV.equals(type);
    }
}
