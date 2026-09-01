package dev.serverloginprofiles.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Compile-time UI adapter for 1.21 through 1.21.8. */
public final class VersionUiBridge
{
    private VersionUiBridge() {}

    public static void show(Minecraft game, Screen screen)
    {
        game.setScreen(screen);
    }

    public static void maskPassword(EditBox box)
    {
        box.setFormatter((value, offset) -> FormattedCharSequence.forward(
            "•".repeat(value.length()), Style.EMPTY
        ));
    }
}
