package dev.serverloginprofiles.profile;

/** Added to Minecraft's ServerData at runtime without altering its public API. */
public interface ServerDataProfileAccess
{
    LoginProfile slp$getLoginProfile();

    void slp$setLoginProfile(LoginProfile profile);
}
