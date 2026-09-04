package dev.serverloginprofiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared constants for the client-only mod. */
public final class ServerLoginProfiles
{
    /** Stable mod identifier. */
    public static final String ID = "serverloginprofiles";
    /** Shared logger. */
    public static final Logger LOG = LoggerFactory.getLogger("AuthWeave");

    private ServerLoginProfiles() {}

    /** Called by each loader after mixins have been registered. */
    public static void initialize()
    {
        LOG.info("AuthWeave is ready");
    }
}
