package dev.serverloginprofiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;

import dev.serverloginprofiles.profile.LoginProfile;
import dev.serverloginprofiles.profile.ServerDataProfileAccess;
import dev.serverloginprofiles.profile.ServerDataProfileCodec;

/** Persists a profile as private extension data in the vanilla saved-server entry. */
@Mixin(ServerData.class)
public abstract class ServerDataMixin implements ServerDataProfileAccess
{
    @Unique
    private LoginProfile slp$loginProfile = new LoginProfile();

    @Override
    public LoginProfile slp$getLoginProfile()
    {
        return slp$loginProfile;
    }

    @Override
    public void slp$setLoginProfile(LoginProfile profile)
    {
        slp$loginProfile = profile == null ? new LoginProfile() : profile.duplicate();
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void slp$readProfile(
        CompoundTag tag,
        CallbackInfoReturnable<ServerData> callback
    )
    {
        ((ServerDataProfileAccess) callback.getReturnValue())
            .slp$setLoginProfile(ServerDataProfileCodec.read(tag));
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void slp$writeProfile(CallbackInfoReturnable<CompoundTag> callback)
    {
        ServerDataProfileCodec.write(callback.getReturnValue(), slp$loginProfile);
    }

    @Inject(method = "copyNameIconFrom", at = @At("TAIL"))
    private void slp$copyProfile(ServerData source, CallbackInfo callback)
    {
        slp$setLoginProfile(((ServerDataProfileAccess) source).slp$getLoginProfile());
    }
}
