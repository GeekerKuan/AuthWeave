package dev.serverloginprofiles.profile;

import net.minecraft.nbt.CompoundTag;

/** NBT accessors used by 1.21 through 1.21.4. */
final class NbtProfileAccess
{
    private NbtProfileAccess() {}

    static CompoundTag child(CompoundTag parent, String key)
    {
        return parent.getCompound(key);
    }

    static String string(CompoundTag tag, String key, String fallback)
    {
        return tag.contains(key) ? tag.getString(key) : fallback;
    }

    static boolean bool(CompoundTag tag, String key, boolean fallback)
    {
        return tag.contains(key) ? tag.getBoolean(key) : fallback;
    }
}
