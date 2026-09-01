package dev.serverloginprofiles.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

final class ServerDataProfileCodecTest
{
    @Test
    void defaultProfileDoesNotAddCustomData()
    {
        CompoundTag server = new CompoundTag();
        ServerDataProfileCodec.write(server, new LoginProfile());
        assertFalse(server.contains(ServerDataProfileCodec.ROOT_KEY));
    }

    @Test
    void passwordIsOmittedUnlessExplicitlyEnabled()
    {
        LoginProfile profile = thirdPartyProfile();
        profile.password = "must-not-be-written";
        profile.storePassword = false;
        CompoundTag server = new CompoundTag();

        ServerDataProfileCodec.write(server, profile);

        CompoundTag stored = NbtProfileAccess.child(server, ServerDataProfileCodec.ROOT_KEY);
        assertFalse(stored.contains("password"));
        assertEquals("", ServerDataProfileCodec.read(server).password);
    }

    @Test
    void optedInPasswordAndTokensRoundTrip()
    {
        LoginProfile profile = thirdPartyProfile();
        profile.storePassword = true;
        profile.password = "clear-text-by-choice";
        CompoundTag server = new CompoundTag();

        ServerDataProfileCodec.write(server, profile);
        LoginProfile restored = ServerDataProfileCodec.read(server);

        assertTrue(restored.storePassword);
        assertEquals(profile.password, restored.password);
        assertEquals(profile.accessToken, restored.accessToken);
        assertEquals(profile.clientToken, restored.clientToken);
        assertEquals(LoginKind.YGGDRASIL, restored.kind);
    }

    private static LoginProfile thirdPartyProfile()
    {
        LoginProfile profile = new LoginProfile();
        profile.kind = LoginKind.YGGDRASIL;
        profile.yggdrasilRoot = "https://example.invalid/api/yggdrasil";
        profile.account = "account";
        profile.playerName = "Player";
        profile.playerId = "12345678123456781234567812345678";
        profile.accessToken = "access";
        profile.clientToken = "client";
        return profile;
    }
}
