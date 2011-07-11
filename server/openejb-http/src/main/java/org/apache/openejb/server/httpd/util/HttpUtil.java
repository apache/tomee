package org.apache.openejb.server.httpd.util;

import java.util.List;

/**
 * @author Romain Manni-Bucau
 */
public final class HttpUtil {
    private HttpUtil() {
        // no-op
    }

    public static String selectSingleAddress(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        // return the first http address
        for (String address : addresses) {
            if (address.startsWith("http:")) {
                return address;
            }
        }
        // return the first https address
        for (String address : addresses) {
            if (address.startsWith("https:")) {
                return address;
            }
        }
        // just return the first address
        String address = addresses.iterator().next();
        return address;
    }
}
