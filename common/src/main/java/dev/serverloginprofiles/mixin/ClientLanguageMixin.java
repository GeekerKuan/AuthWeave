package dev.serverloginprofiles.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;

import dev.serverloginprofiles.ServerLoginProfiles;

/** Loads bundled language JSON when a loader does not expose dependency-free mod assets as a pack. */
@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin
{
    @Shadow @Final private Map<String, String> storage;

    @Inject(method = "loadFrom", at = @At("RETURN"), cancellable = true)
    private static void slp$appendBundledTranslations(
        ResourceManager resources,
        List<String> languageCodes,
        boolean rightToLeft,
        CallbackInfoReturnable<ClientLanguage> callback
    )
    {
        ClientLanguage original = callback.getReturnValue();
        ClientLanguageMixin language = (ClientLanguageMixin) (Object) original;
        Map<String, String> bundled = new LinkedHashMap<>();
        for (String code : languageCodes) {
            language.slp$loadLanguage(code, bundled);
        }
        bundled.putAll(language.storage);
        callback.setReturnValue(slp$create(bundled, rightToLeft));
    }

    private void slp$loadLanguage(String code, Map<String, String> destination)
    {
        String path = "assets/serverloginprofiles/lang/" + code + ".json";
        try (InputStream input = ClientLanguageMixin.class.getClassLoader().getResourceAsStream(path)) {
            if (input != null) Language.loadFromJson(input, destination::put);
        } catch (IOException | RuntimeException error) {
            ServerLoginProfiles.LOG.error("Unable to load bundled language resource {}", path, error);
        }
    }

    @Invoker("<init>")
    private static ClientLanguage slp$create(Map<String, String> storage, boolean rightToLeft)
    {
        throw new AssertionError();
    }
}
