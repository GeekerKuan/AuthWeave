package dev.serverloginprofiles.ui;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.serverloginprofiles.ServerLoginProfiles;
import dev.serverloginprofiles.config.WarningPreference;
import dev.serverloginprofiles.profile.LoginKind;
import dev.serverloginprofiles.profile.LoginProfile;
import dev.serverloginprofiles.yggdrasil.YggdrasilLoginClient;
import dev.serverloginprofiles.yggdrasil.YggdrasilHttp;
import dev.serverloginprofiles.yggdrasil.YggdrasilLoginException;

/** Child screen that edits one server's pending login profile. */
public final class LoginProfileScreen extends Screen
{
    private final Screen returnTo;
    private final String serverAddress;
    private final Consumer<LoginProfile> completion;
    private LoginProfile editing;
    private EditBox offlineName;
    private EditBox apiRoot;
    private EditBox account;
    private EditBox password;
    private Button mode;
    private Button storePassword;
    private Button primaryAction;
    private StringWidget feedback;
    private boolean awaitingLogin;
    private String enteredPassword;

    /** Creates an editor around a detached profile. */
    public LoginProfileScreen(
        Screen returnTo,
        String serverAddress,
        LoginProfile profile,
        Consumer<LoginProfile> completion
    )
    {
        super(Component.translatable("serverloginprofiles.screen.title"));
        this.returnTo = returnTo;
        this.serverAddress = serverAddress;
        this.editing = profile.duplicate();
        this.enteredPassword = profile.storePassword ? profile.password : "";
        this.completion = completion;
    }

    @Override
    protected void init()
    {
        int x = width / 2 - 105;
        addRenderableWidget(new StringWidget(x, 12, 210, 20, title, font));
        addRenderableWidget(new StringWidget(
            x, 32, 210, 16, Component.literal(serverAddress).withStyle(ChatFormatting.GRAY), font
        ));

        mode = addRenderableWidget(Button.builder(modeText(), ignored -> {
            editing.kind = editing.kind.following();
            refreshControls();
        }).bounds(x, 52, 210, 20).build());

        offlineName = input(x, 82, "serverloginprofiles.field.offline_name", editing.offlineName);
        apiRoot = input(x, 82, "serverloginprofiles.field.api_root", editing.yggdrasilRoot);
        account = input(x, 108, "serverloginprofiles.field.account", editing.account);
        password = input(x, 134, "serverloginprofiles.field.password", enteredPassword);
        VersionUiBridge.maskPassword(password);

        storePassword = addRenderableWidget(Button.builder(passwordStorageText(), ignored -> {
            if (editing.storePassword) {
                editing.storePassword = false;
                editing.password = "";
                storePassword.setMessage(passwordStorageText());
            } else {
                requestPasswordStorage();
            }
        }).bounds(x, 160, 210, 20).build());

        primaryAction = addRenderableWidget(Button.builder(actionText(), ignored -> accept())
            .bounds(x, 188, 102, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), ignored -> onClose())
            .bounds(x + 108, 188, 102, 20).build());
        feedback = addRenderableWidget(new StringWidget(x, 214, 210, 16, Component.empty(), font));
        refreshControls();
    }

    private EditBox input(int x, int y, String key, String value)
    {
        EditBox box = addRenderableWidget(new EditBox(font, x, y, 210, 20, Component.translatable(key)));
        box.setMaxLength(512);
        box.setValue(value == null ? "" : value);
        box.setHint(Component.translatable(key));
        return box;
    }

    private void refreshControls()
    {
        boolean offline = editing.kind == LoginKind.OFFLINE;
        boolean custom = editing.kind == LoginKind.YGGDRASIL;
        offlineName.setVisible(offline);
        apiRoot.setVisible(custom);
        account.setVisible(custom);
        password.setVisible(custom);
        storePassword.visible = custom;
        mode.setMessage(modeText());
        primaryAction.setMessage(actionText());
        feedback.setMessage(Component.empty());
    }

    private void accept()
    {
        if (awaitingLogin) return;
        if (editing.kind == LoginKind.OFFICIAL) {
            finish(editing);
            return;
        }
        if (editing.kind == LoginKind.OFFLINE) {
            if (offlineName.getValue().isBlank()) {
                showError("serverloginprofiles.error.offline_name");
                return;
            }
            editing.offlineName = offlineName.getValue().strip();
            finish(editing);
            return;
        }
        authenticate();
    }

    private void authenticate()
    {
        if (apiRoot.getValue().isBlank() || account.getValue().isBlank() || password.getValue().isBlank()) {
            showError("serverloginprofiles.error.required");
            return;
        }
        awaitingLogin = true;
        primaryAction.active = false;
        feedback.setMessage(Component.translatable("serverloginprofiles.status.login"));
        String root = apiRoot.getValue();
        String user = account.getValue();
        String secret = password.getValue();
        enteredPassword = secret;
        CompletableFuture.supplyAsync(() -> {
            try {
                return YggdrasilLoginClient.login(root, user, secret);
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
        }, YggdrasilHttp.executor()).whenComplete((loggedIn, failure) -> {
            assert minecraft != null;
            minecraft.execute(() -> completeAuthentication(loggedIn, failure, secret));
        });
    }

    private void completeAuthentication(LoginProfile loggedIn, Throwable failure, String secret)
    {
        awaitingLogin = false;
        primaryAction.active = true;
        if (failure != null) {
            ServerLoginProfiles.LOG.error("Third-party authentication failed for {}", serverAddress, failure);
            feedback.setMessage(Component.translatable(
                "serverloginprofiles.error.login", describeFailure(failure)
            ).withStyle(ChatFormatting.RED));
            return;
        }
        loggedIn.storePassword = editing.storePassword;
        loggedIn.password = editing.storePassword ? secret : "";
        editing = loggedIn;
        finish(loggedIn);
    }

    private void finish(LoginProfile profile)
    {
        if (!profile.storePassword) profile.password = "";
        completion.accept(profile.duplicate());
        assert minecraft != null;
        VersionUiBridge.show(minecraft, returnTo);
    }

    private void requestPasswordStorage()
    {
        captureInputs();
        if (WarningPreference.isSuppressed()) {
            editing.storePassword = true;
            storePassword.setMessage(passwordStorageText());
            return;
        }
        assert minecraft != null;
        VersionUiBridge.show(minecraft, new CredentialExportWarningScreen(this, () -> {
            editing.storePassword = true;
            editing.password = enteredPassword;
        }));
    }

    private void captureInputs()
    {
        editing.offlineName = offlineName.getValue();
        editing.yggdrasilRoot = apiRoot.getValue();
        editing.account = account.getValue();
        enteredPassword = password.getValue();
        if (editing.storePassword) editing.password = enteredPassword;
    }

    private void showError(String key)
    {
        feedback.setMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
    }

    private Component modeText()
    {
        String key = editing.kind.name().toLowerCase(Locale.ROOT);
        return Component.translatable("serverloginprofiles.mode.label",
            Component.translatable("serverloginprofiles.mode." + key));
    }

    private Component passwordStorageText()
    {
        return Component.translatable("serverloginprofiles.password.store", Component.translatable(
            editing.storePassword ? "serverloginprofiles.value.on" : "serverloginprofiles.value.off"
        ));
    }

    private Component actionText()
    {
        return Component.translatable(editing.kind == LoginKind.YGGDRASIL
            ? "serverloginprofiles.action.login" : "serverloginprofiles.action.save");
    }

    private static Component describeFailure(Throwable failure)
    {
        Throwable current = failure;
        while (current.getCause() != null) current = current.getCause();
        if (current instanceof YggdrasilLoginException localized) {
            return Component.translatable(localized.translationKey(), localized.arguments());
        }
        if (current instanceof HttpTimeoutException) {
            return Component.translatable("serverloginprofiles.error.timeout");
        }
        if (current instanceof UnknownHostException || current instanceof ConnectException) {
            return Component.translatable("serverloginprofiles.error.network");
        }
        if (current instanceof InterruptedException) {
            return Component.translatable("serverloginprofiles.error.interrupted");
        }
        return Component.translatable("serverloginprofiles.error.unknown");
    }

    @Override
    public void onClose()
    {
        if (!awaitingLogin && minecraft != null) VersionUiBridge.show(minecraft, returnTo);
    }
}
