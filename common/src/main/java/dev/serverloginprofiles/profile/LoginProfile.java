package dev.serverloginprofiles.profile;

import java.util.Objects;

/** Serializable settings for one server. */
public final class LoginProfile
{
    /** Selected authentication strategy. */
    public LoginKind kind = LoginKind.OFFICIAL;
    /** Name used for offline-mode connections. */
    public String offlineName = "";
    /** Root of an authlib-injector-compatible API. */
    public String yggdrasilRoot = "";
    /** Account identifier used when refreshing credentials manually. */
    public String account = "";
    /** Selected game profile name. */
    public String playerName = "";
    /** Selected game profile UUID. */
    public String playerId = "";
    /** Yggdrasil access token. */
    public String accessToken = "";
    /** Yggdrasil client token. */
    public String clientToken = "";
    /** Whether the password is deliberately stored without encryption. */
    public boolean storePassword;
    /** Optional clear-text password. */
    public String password = "";

    /** Returns a detached object safe for editing on a child screen. */
    public LoginProfile duplicate()
    {
        LoginProfile result = new LoginProfile();
        result.kind = kind;
        result.offlineName = offlineName;
        result.yggdrasilRoot = yggdrasilRoot;
        result.account = account;
        result.playerName = playerName;
        result.playerId = playerId;
        result.accessToken = accessToken;
        result.clientToken = clientToken;
        result.storePassword = storePassword;
        result.password = password;
        return result;
    }

    /** Stable value used to skip redundant identity installation on reconnect. */
    public int fingerprint()
    {
        return Objects.hash(kind, offlineName, yggdrasilRoot, playerName, playerId,
            accessToken, clientToken);
    }

    /** Whether this profile has any data worth adding to a server-list entry. */
    public boolean isDefault()
    {
        return kind == LoginKind.OFFICIAL && blank(offlineName) && blank(yggdrasilRoot)
            && blank(account) && blank(playerName) && blank(playerId) && blank(accessToken)
            && blank(clientToken) && !storePassword && blank(password);
    }

    private static boolean blank(String value)
    {
        return value == null || value.isEmpty();
    }
}
