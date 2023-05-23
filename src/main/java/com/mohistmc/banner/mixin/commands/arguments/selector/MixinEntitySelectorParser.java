package com.mohistmc.banner.mixin.commands.arguments.selector;

import com.mohistmc.banner.injection.commands.arguments.selector.InjectionEntitySelectorParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.checkerframework.common.util.report.qual.ReportCall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(EntitySelectorParser.class)
public abstract class MixinEntitySelectorParser implements InjectionEntitySelectorParser {

    @Shadow protected abstract void parseSelector() throws CommandSyntaxException;

    @Shadow public abstract EntitySelector parse() throws CommandSyntaxException;

    @Shadow private boolean usesSelectors;

    private AtomicBoolean banner$overridePermissions = new AtomicBoolean(false);

    @Override
    public void parseSelector(boolean overridePermissions) throws CommandSyntaxException {
        banner$overridePermissions.set(overridePermissions);
        parseSelector();
    }

    @Redirect(method = "parseSelector", at = @At(value = "FIELD", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;usesSelectors:Z"))
    private void banner$resetUseSelectors(EntitySelectorParser instance, boolean value) {
        this.usesSelectors = !banner$overridePermissions.get();
    }

    @Redirect(method = "parse", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;parseSelector()V"))
    private void banner$resetParseSelectors(EntitySelectorParser instance) throws CommandSyntaxException {
        this.parseSelector(banner$overridePermissions.get());
    }

    @Override
    public EntitySelector parse(boolean overridePermissions) throws CommandSyntaxException {
        banner$overridePermissions.set(overridePermissions);
        return parse();
    }
}
