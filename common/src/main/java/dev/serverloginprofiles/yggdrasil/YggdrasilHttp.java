package dev.serverloginprofiles.yggdrasil;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shared, low-overhead network resources for the selected Yggdrasil provider. */
public final class YggdrasilHttp
{
    private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(
        Thread.ofVirtual().name("server-login-profiles-network-", 0).factory()
    );
    static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .executor(EXECUTOR)
        // Authentication requests contain credentials and must never be redirected to another host.
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    private YggdrasilHttp() {}

    /** Executor shared by authentication and profile lookups; virtual threads do not retain workers. */
    public static Executor executor()
    {
        return EXECUTOR;
    }
}
