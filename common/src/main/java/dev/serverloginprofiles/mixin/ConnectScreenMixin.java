package dev.serverloginprofiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import dev.serverloginprofiles.ServerLoginProfiles;
import dev.serverloginprofiles.session.IdentityController;
import dev.serverloginprofiles.session.LoginProfileException;
import dev.serverloginprofiles.ui.VersionUiBridge;

/** Hooks identity selection before vanilla creates the connection worker. */
@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin
{
    private ConnectScreenMixin() {}

    /** Selects the server assignment or shows a normal connection error. */
    @Inject(method = "startConnecting", at = @At("HEAD"), cancellable = true)
    private static void selectIdentity(
        Screen parent,
        Minecraft game,
        ServerAddress address,
        ServerData data,
        boolean quickPlay,
        TransferState transfer,
        CallbackInfo callback
    )
    {
        try {
            IdentityController.selectFor(data);
        } catch (RuntimeException error) {
            ServerLoginProfiles.LOG.error("Unable to select a login profile for {}", data.ip, error);
            VersionUiBridge.show(game, new DisconnectedScreen(
                parent,
                Component.translatable("serverloginprofiles.error.connect.title"),
                error instanceof LoginProfileException profileError
                    ? Component.translatable(profileError.translationKey())
                    : Component.translatable("serverloginprofiles.error.connect.body")
            ));
            callback.cancel();
        }
    }
}
