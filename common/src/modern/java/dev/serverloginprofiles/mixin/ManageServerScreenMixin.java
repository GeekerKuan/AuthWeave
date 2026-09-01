package dev.serverloginprofiles.mixin;

import java.util.Locale;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

import dev.serverloginprofiles.profile.LoginProfile;
import dev.serverloginprofiles.profile.ServerDataProfileAccess;
import dev.serverloginprofiles.ui.LoginProfileScreen;
import dev.serverloginprofiles.ui.VersionUiBridge;

/** Places an independent login-profile control on the add/edit server screen. */
@Mixin(ManageServerScreen.class)
public abstract class ManageServerScreenMixin extends Screen
{
    @Shadow
    @Final
    private ServerData serverData;
    @Shadow
    private EditBox nameEdit;
    @Shadow
    private EditBox ipEdit;

    @Unique
    private LoginProfile slp$draft;
    @Unique
    private String slp$returnedName;
    @Unique
    private String slp$returnedAddress;

    private ManageServerScreenMixin(Component title)
    {
        super(title);
    }

    /** Creates the control after vanilla and other server-editor mods finish initialization. */
    @Inject(method = "init", at = @At("RETURN"))
    private void appendLoginControl(CallbackInfo callback)
    {
        assert minecraft != null;
        if (slp$draft == null) {
            slp$draft = ((ServerDataProfileAccess) serverData).slp$getLoginProfile().duplicate();
        }
        if (slp$returnedName != null) {
            nameEdit.setValue(slp$returnedName);
            ipEdit.setValue(slp$returnedAddress);
            slp$returnedName = null;
            slp$returnedAddress = null;
        }
        addRenderableWidget(Button.builder(slp$buttonText(), ignored -> {
            slp$returnedName = nameEdit.getValue();
            slp$returnedAddress = ipEdit.getValue();
            VersionUiBridge.show(minecraft, new LoginProfileScreen(
                this, ipEdit.getValue(), slp$draft, chosen -> slp$draft = chosen
            ));
        }).bounds(width - 103, 29, 98, 20).build());
    }

    /** Installs the draft before vanilla invokes its save callback. */
    @Inject(method = "onAdd", at = @At("HEAD"))
    private void persistDraft(CallbackInfo callback)
    {
        ((ServerDataProfileAccess) serverData).slp$setLoginProfile(slp$draft);
    }

    @Unique
    private Component slp$buttonText()
    {
        String mode = (slp$draft == null ? "official" : slp$draft.kind.name().toLowerCase(Locale.ROOT));
        return Component.translatable("serverloginprofiles.editor.button",
            Component.translatable("serverloginprofiles.mode." + mode));
    }
}
