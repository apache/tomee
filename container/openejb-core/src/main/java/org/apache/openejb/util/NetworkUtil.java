package org.apache.openejb.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Romain Manni-Bucau
 */
public final class NetworkUtil {
    private NetworkUtil() {
        // no-op
    }

    /**
     * @return a opened port or -1 if an IOException occurs.
     */
    public static int getAnAvailablePort() {
        int port = -1;
        try {
            ServerSocket server = new ServerSocket(0);
            port = server.getLocalPort();
            server.close();
        } catch (IOException e) {
            // no-op: -1 will be returned
        }
        return port;
    }

    public static String getLocalAddress(String start, String end) {
        return start + "localhost:" + getAnAvailablePort() + end;
    }
}
