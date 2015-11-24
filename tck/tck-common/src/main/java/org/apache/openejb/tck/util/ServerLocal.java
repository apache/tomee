/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
