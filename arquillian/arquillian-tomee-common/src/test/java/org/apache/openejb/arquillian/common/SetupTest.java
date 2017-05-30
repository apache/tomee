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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.common;

import org.apache.openejb.loader.Files;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SetupTest {
    @Test
    public void simpleReplace() throws IOException {
        replaceTest(8080, 8005, 8009, 1111, 2222, 3333);
    }

    private void replaceTest(int http1, int stop1, int ajp1, int http2, int stop2, int ajp2) throws IOException {
        final File target = write("target/setup1.xml", serverXml(http1, stop1, ajp1));
        Setup.replace(new PropertiesBuilder()
                .p(Integer.toString(http1), Integer.toString(http2))
                .p(Integer.toString(stop1), Integer.toString(stop2))
                .p(Integer.toString(ajp1), Integer.toString(ajp2))
                .asMap(), target, true);
        assertEquals(serverXml(http2, stop2, ajp2), IO.slurp(target));
    }

    @Test
    public void ambiguousReplace() throws IOException {
        replaceTest(111, 1111, 11111, 3333, 4444, 5555);
    }

    @Test
    public void synchronizeFolders() throws IOException {
        final File root = new File("target/SetupTest/synchronizeFolders/");
        Files.delete(root); // clean up
        final File source = org.apache.openejb.loader.Files.mkdirs(new File(root, "conf"));
        for (int i = 0; i < 10; i++) {
            final FileWriter w = new FileWriter(new File(source, "file-" + i));
            w.write(Integer.toString(i));
            w.close();
        }

        Setup.synchronizeFolder(root,new File(root, "conf").getAbsolutePath(), "conf-copy");
        final File target = new File(root, "conf-copy");
        assertEquals(10, target.listFiles().length);

        // sub folders
        final File subFolder = org.apache.openejb.loader.Files.mkdirs(new File(source, "conf.d"));
        for (int i = 0; i < 10; i++) {
            final FileWriter w = new FileWriter(new File(subFolder, "file-" + i));
            w.write(Integer.toString(i));
            w.close();
        }

        Setup.synchronizeFolder(root,new File(root, "conf").getAbsolutePath(), "conf-copy2");
        assertEquals(11, new File(root, "conf-copy2").listFiles().length);
        assertEquals(10, new File(root, "conf-copy2/conf.d").listFiles().length);
    }

    private File write(final String file, final String s1) throws IOException {
        final File target = new File(file);
        final FileWriter writer = new FileWriter(target);
        writer.write(s1);
        writer.close();
        return target;
    }

    private static String serverXml(final int http, final int stop, final int ajp) {
        return "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<Server port=\"" + stop + "\" shutdown=\"SHUTDOWN\">\n" +
                "  <Listener className=\"org.apache.tomee.catalina.ServerListener\" />\n" +
                "  <Service name=\"Catalina\">\n" +
                "    <Connector port=\"" + http + "\" protocol=\"HTTP/1.1\" connectionTimeout=\"20000\"/>\n" +
                "    <Connector port=\"" + ajp + "\" protocol=\"AJP/1.3\" redirectPort=\"8443\" />\n" +
                "    <Engine name=\"Catalina\" defaultHost=\"localhost\">\n" +
                "      <Host name=\"localhost\"  appBase=\"webapps\" unpackWARs=\"true\" autoDeploy=\"true\"/>\n" +
                "    </Engine>\n" +
                "  </Service>\n" +
                "</Server>\n";
    }
}
