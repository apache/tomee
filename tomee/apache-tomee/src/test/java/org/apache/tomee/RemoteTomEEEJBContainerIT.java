/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RemoteTomEEEJBContainerIT {
    @Test
    public void run() throws IOException {
        final File app = new File("target/mock/webapp");
        Files.mkdirs(app);

        final FileWriter writer = new FileWriter(new File(app, "index.html"));
        writer.write("Hello");
        writer.close();

        File work = new File("target/webprofile-work-dir/").getAbsoluteFile();
        if (!work.exists()) {
            //May be running from root
            work = new File("apache-tomee/target/webprofile-work-dir/").getAbsoluteFile();
        }

        final File[] files = work.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat-");
            }
        });

        final File tomee = (null != files ? files[0] : null);
        if (tomee == null) {
            fail("Failed to find Tomcat directory required for this test - Ensure you have run at least the maven phase: mvn process-resources");
        }

        final FileWriter serverXml = new FileWriter(new File(tomee, "conf/server.xml"));
        final int http = NetworkUtil.getNextAvailablePort();
        serverXml.write("<?xml version='1.0' encoding='utf-8'?>\n" +
            "<Server port=\"" + NetworkUtil.getNextAvailablePort() + "\" shutdown=\"SHUTDOWN\">\n" +
            "  <!-- TomEE plugin for Tomcat -->\n" +
            "  <Listener className=\"org.apache.tomee.catalina.ServerListener\" />\n" +
            "  <Service name=\"Catalina\">\n" +
            "    <Connector port=\"" + http + "\" protocol=\"HTTP/1.1\" />\n" +
            "    <Engine name=\"Catalina\" defaultHost=\"localhost\">\n" +
            "      <Host name=\"localhost\"  appBase=\"webapps\"\n" +
            "            unpackWARs=\"true\" autoDeploy=\"true\">\n" +
            "      </Host>\n" +
            "    </Engine>\n" +
            "  </Service>\n" +
            "</Server>\n");
        serverXml.close();

        EJBContainer container = null;
        try {
            container = EJBContainer.createEJBContainer(new HashMap<Object, Object>() {{
                put(EJBContainer.PROVIDER, "tomee-remote");
                put(EJBContainer.MODULES, app.getAbsolutePath());
                put("openejb.home", tomee.getAbsolutePath());
            }});
            final URL url = new URL("http://localhost:" + http + "/webapp/index.html");
            assertEquals("Hello", IO.slurp(url));
        } finally {
            if (container != null) {
                container.close();
            }
        }
    }
}
