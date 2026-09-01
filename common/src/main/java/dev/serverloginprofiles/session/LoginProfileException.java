package dev.serverloginprofiles.session;

/** User-facing profile error represented by a language-file key. */
public final class LoginProfileException extends IllegalStateException
{
    private final String translationKey;

    public LoginProfileException(String translationKey)
    {
        super(translationKey);
        this.translationKey = translationKey;
    }

    public String translationKey()
    {
        return translationKey;
    }
}
