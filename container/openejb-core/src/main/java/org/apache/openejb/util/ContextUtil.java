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

    public static void mkdirs(Context context, String key) {
        final String[] parts = key.split("/");

        int i = 0;
        for (String part : parts) {
            if (++i == parts.length) return;

            try {
                context = context.createSubcontext(part);
            } catch (NamingException e) {
                try {
                    context = (Context) context.lookup(part);
                } catch (NamingException e1) {
                    return;
                }
            }
        }
    }
}
