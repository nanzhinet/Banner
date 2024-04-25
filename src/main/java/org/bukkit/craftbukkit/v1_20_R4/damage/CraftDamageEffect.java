package org.bukkit.craftbukkit.v1_20_R4.damage;

import net.minecraft.world.damagesource.DamageEffects;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R4.CraftSound;
import org.bukkit.damage.DamageEffect;

public class CraftDamageEffect implements DamageEffect {

    private final DamageEffects damageEffects;

    public CraftDamageEffect(DamageEffects damageEffects) {
        this.damageEffects = damageEffects;
    }

    public DamageEffects getHandle() {
        return this.damageEffects;
    }

    @Override
    public Sound getSound() {
        return CraftSound.minecraftToBukkit(this.getHandle().sound());
    }

    public static DamageEffect getById(String id) {
        for (DamageEffects damageEffects : DamageEffects.values()) {
            if (damageEffects.getSerializedName().equalsIgnoreCase(id)) {
                return CraftDamageEffect.toBukkit(damageEffects);
            }
        }
        return null;
    }

    public static DamageEffect toBukkit(DamageEffects damageEffects) {
        return new CraftDamageEffect(damageEffects);
    }
}
