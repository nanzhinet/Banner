package org.bukkit.entity;

import com.mohistmc.banner.paper.addon.entity.monster.AddonSlime;

/**
 * Represents a Slime.
 */
public interface Slime extends Mob, Enemy, AddonSlime {

    /**
     * @return The size of the slime
     */
    public int getSize();

    /**
     * @param sz The new size of the slime.
     */
    public void setSize(int sz);
}
