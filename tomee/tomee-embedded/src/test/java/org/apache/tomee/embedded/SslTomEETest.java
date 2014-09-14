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
package org.apache.tomee.embedded;

import org.apache.openejb.loader.Files;
import org.junit.Test;

import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SslTomEETest {
    @Test
    public void test() throws Exception {
        final File keystore = new File("target/keystore");

        {   // generate keystore/trustore
            if (keystore.exists()) {
                Files.delete(keystore);
            }

            Class<?> keyToolClass;
            try {
                keyToolClass = Class.forName("sun.security.tools.KeyTool");
            } catch (final ClassNotFoundException e) {
                try {
                    // in jdk8, the tool changed ...
                    keyToolClass = Class.forName("sun.security.tools.keytool.Main");
                } catch (final ClassNotFoundException cnfe) {
                    keyToolClass = Class.forName("com.ibm.crypto.tools.KeyTool");
                }
            }

            final String[] args = {
                    "-genkey",
                    "-alias", "serveralias",
                    "-keypass", "changeit",
                    "-keystore", keystore.getAbsolutePath(),
                    "-storepass", "changeit",
                    "-dname", "cn=serveralias",
                    "-keyalg", "RSA"
            };
            keyToolClass.getMethod("main", String[].class).invoke(null, new Object[]{args});
        }

        final Configuration configuration = new Configuration();
        configuration.setSsl(true);
        configuration.setKeystoreFile(keystore.getAbsolutePath());
        configuration.setKeystorePass("changeit");
        configuration.setKeyAlias("serveralias");

        final Container container = new Container();
        container.setup(configuration);
        container.start();
        try {
            assertEquals(8443, ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("Tomcat:type=ProtocolHandler,port=8443"), "port"));
        } finally {
            container.stop();
        }

        // ensure it is not always started
        configuration.setSsl(false);
        container.setup(configuration);
        container.start();
        try {
            assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("Tomcat:type=ProtocolHandler,port=8443")));
        } finally {
            container.stop();
        }

    }
}
