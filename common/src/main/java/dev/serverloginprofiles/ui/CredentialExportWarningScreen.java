package dev.serverloginprofiles.ui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import dev.serverloginprofiles.config.WarningPreference;

/** Vanilla-style fullscreen warning shown before clear-text credential storage is enabled. */
public final class CredentialExportWarningScreen extends WarningScreen
{
    private static final Component TITLE = Component.translatable(
        "serverloginprofiles.warning.credentials.title"
    ).withStyle(ChatFormatting.BOLD);
    private static final Component MESSAGE = Component.translatable(
        "serverloginprofiles.warning.credentials.message"
    );
    private static final Component CHECK = Component.translatable(
        "serverloginprofiles.warning.credentials.check"
    ).withColor(0xE0E0E0);
    private static final Component NARRATION = Component.translatable(
        "serverloginprofiles.warning.credentials.narration"
    );

    private final Screen previous;
    private final Runnable accepted;

    public CredentialExportWarningScreen(Screen previous, Runnable accepted)
    {
        super(TITLE, MESSAGE, CHECK, NARRATION);
        this.previous = previous;
        this.accepted = accepted;
    }

    @Override
    protected Layout addFooterButtons()
    {
        LinearLayout buttons = LinearLayout.horizontal().spacing(8);
        buttons.addChild(Button.builder(CommonComponents.GUI_PROCEED, ignored -> proceed()).build());
        buttons.addChild(Button.builder(CommonComponents.GUI_BACK, ignored -> onClose()).build());
        return buttons;
    }

    private void proceed()
    {
        if (stopShowing.selected()) WarningPreference.suppressFutureWarnings();
        accepted.run();
        onClose();
    }

    @Override
    public void onClose()
    {
        if (minecraft != null) VersionUiBridge.show(minecraft, previous);
    }
}
