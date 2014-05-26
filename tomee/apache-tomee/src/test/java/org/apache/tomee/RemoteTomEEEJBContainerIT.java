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
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class RemoteTomEEEJBContainerIT {
    @Test
    public void run() throws IOException {
        final File app = new File("target/mock/webapp");
        Files.mkdirs(app);

        final FileWriter writer = new FileWriter(new File(app, "index.html"));
        writer.write("Hello");
        writer.close();

        EJBContainer container = null;
        try {
            container = EJBContainer.createEJBContainer(new HashMap<Object, Object>() {{
                put(EJBContainer.PROVIDER, "tomee-remote");
                put(EJBContainer.MODULES, app.getAbsolutePath());
                put("openejb.home", new File("target/webprofile-work-dir/").listFiles(new FileFilter() {
                    @Override
                    public boolean accept(final File pathname) {
                        return pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat-");
                    }
                })[0].getAbsolutePath());
            }});
            assertEquals("Hello", IO.slurp(new URL("http://localhost:8080/webapp/index.html")));
        } finally {
            if (container != null) {
                container.close();
            }
        }
    }
}
