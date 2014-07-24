/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
 */
package org.apache.openejb.tck.util;

import org.apache.tomee.util.QuickServerXmlParser;

import java.io.File;

public class ServerLocal {

    private ServerLocal() {
    }

    /**
     * If the TCK is running against a local server extracted to the target dir
     * then the server.xml will have the port defined already.
     *
     * @param def Default port to use if none is found
     * @return The determined port, the value of 'server.http.port' or the provided default
     */
    public static int getPort(final int def) {
        final String home = System.getProperty("openejb.home", "empty");

        if (!"empty".equals(home)) {
            final File serverXml = new File(home, "conf/server.xml");

            if (serverXml.exists() && serverXml.isFile()) {
                final QuickServerXmlParser parser = QuickServerXmlParser.parse(serverXml);

                return Integer.parseInt(parser.http());
            }
        }

        return Integer.getInteger("server.http.port", def);
    }
}
