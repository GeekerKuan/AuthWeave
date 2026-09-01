package dev.serverloginprofiles.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Compile-time UI adapter for 1.21.9 and newer. */
public final class VersionUiBridge
{
    private VersionUiBridge() {}

    public static void show(Minecraft game, Screen screen)
    {
        game.setScreenAndShow(screen);
    }

    public static void maskPassword(EditBox box)
    {
        box.addFormatter((value, offset) -> FormattedCharSequence.forward(
            "•".repeat(value.length()), Style.EMPTY
        ));
    }
}
