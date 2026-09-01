package dev.serverloginprofiles.yggdrasil;

import java.io.IOException;

/** A user-facing authentication failure represented by a localizable key. */
public final class YggdrasilLoginException extends IOException
{
    private final String translationKey;
    private final Object[] arguments;

    YggdrasilLoginException(String translationKey, Object... arguments)
    {
        super(translationKey);
        this.translationKey = translationKey;
        this.arguments = arguments.clone();
    }

    public String translationKey()
    {
        return translationKey;
    }

    public Object[] arguments()
    {
        return arguments.clone();
    }
}
