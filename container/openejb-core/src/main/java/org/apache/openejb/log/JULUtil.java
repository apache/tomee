package org.apache.openejb.log;

import java.util.logging.Level;
import org.apache.openejb.loader.SystemInstance;

public final class JULUtil {
    public static final String DEFAULT_LOG_LEVEL = "INFO";
    public static final String OPENEJB_LOG_LEVEL = "openejb.log.level";

    private JULUtil() {
        // no-op
    }

    public static Level level() {
        final String propLevel = SystemInstance.get().getProperty(OPENEJB_LOG_LEVEL, DEFAULT_LOG_LEVEL).toUpperCase();
        try {
            return (Level) Level.class.getDeclaredField(propLevel).get(null);
        } catch (IllegalAccessException e) {
            return Level.INFO;
        } catch (NoSuchFieldException e) {
            return Level.INFO;
        }
    }
}
