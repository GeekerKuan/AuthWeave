package dev.serverloginprofiles.profile;

import net.minecraft.nbt.CompoundTag;

/** NBT accessors used by 1.21.5 and newer. */
final class NbtProfileAccess
{
    private NbtProfileAccess() {}

    static CompoundTag child(CompoundTag parent, String key)
    {
        return parent.getCompoundOrEmpty(key);
    }

    static String string(CompoundTag tag, String key, String fallback)
    {
        return tag.getStringOr(key, fallback);
    }

    static boolean bool(CompoundTag tag, String key, boolean fallback)
    {
        return tag.getBooleanOr(key, fallback);
    }
}
