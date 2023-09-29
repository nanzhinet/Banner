package com.mohistmc.banner.mixin.world.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BoneMealItem.class)
public abstract class MixinBoneMealItem extends Item {

    public MixinBoneMealItem(Properties properties) {
        super(properties);
    }

    private static InteractionResult applyBonemeal(UseOnContext itemactioncontext) {
        return Items.BONE_MEAL.useOn(itemactioncontext);
    }
}
