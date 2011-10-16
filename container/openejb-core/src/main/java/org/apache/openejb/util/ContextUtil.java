package org.apache.openejb.util;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * @author rmannibucau
 */
public class ContextUtil {
    private ContextUtil() {
        // no-op
    }

    public static Context mkdirs(Context context, String key) {
        final String[] parts = key.split("/");

        int i = 0;
        Context lastContext = context;
        for (String part : parts) {
            if (++i == parts.length) {
                return lastContext;
            }

            try {
                lastContext = lastContext.createSubcontext(part);
            } catch (NamingException e) {
                try {
                    lastContext = (Context) lastContext.lookup(part);
                } catch (NamingException e1) {
                    return lastContext;
                }
            }
        }
        return lastContext;
    }
}
