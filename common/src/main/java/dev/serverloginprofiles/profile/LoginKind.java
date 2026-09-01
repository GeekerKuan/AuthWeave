package dev.serverloginprofiles.profile;

/** Authentication strategy assigned to a multiplayer server. */
public enum LoginKind
{
    /** Use the account supplied by the launcher. */
    OFFICIAL,
    /** Construct an offline-mode identity. */
    OFFLINE,
    /** Use an authlib-injector-compatible Yggdrasil service. */
    YGGDRASIL;

    /** Advances to the next value for the editor's mode button. */
    public LoginKind following()
    {
        LoginKind[] choices = values();
        return choices[(ordinal() + 1) % choices.length];
    }
}
