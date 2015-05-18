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
package org.apache.openejb.loader;

import org.apache.openejb.loader.provisining.MavenResolver;
import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// needs http to work
public class ProvisioningUtilTest {
    @Before
    public void init() throws Exception {
        SystemInstance.init(new Properties() {{
            setProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER, "target");
        }});
        new File("target/temp").mkdirs();
    }

    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void mvnUrl() throws MalformedURLException {
        final String url = new MavenResolver().quickMvnUrl("org.apache.tomee/apache-tomee/x.y.z-SNAPSHOT");
        assertEquals("https://repository.apache.org/snapshots/org/apache/tomee/apache-tomee/x.y.z-SNAPSHOT/apache-tomee-x.y.z-SNAPSHOT.jar", url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingShouldFail() throws Exception {
        ProvisioningUtil.realLocation("mvn:missing:artifact:c56dfhrvfjc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingShouldFailEvenWhenResolvedLocally() throws Exception {
        ProvisioningUtil.realLocation("file:///target/cigcrdyicexbeoeoc is not here.jar");
    }

    @Test
    public void wildcard() throws IOException {
        final File folder = new File("target/jars");
        folder.mkdirs();
        write(folder, "jar1.jar");
        write(folder, "jar2.jar");
        final Set<String> urls = ProvisioningUtil.realLocation(folder.getAbsolutePath() + "/*.jar");
        assertEquals(3, urls.size());
    }

    @Test
    public void localMaven() {
        final Set<String> urls = ProvisioningUtil.realLocation("mvn:junit:junit:4.11");
        assertEquals(1, urls.size());
    }

    @Test
    public void remoteMaven() { // not sure it is remote but at least not in tomee build
        final Set<String> urls = ProvisioningUtil.realLocation("mvn:com.google.guava:guava-io:r03");
        assertEquals(1, urls.size());
        assertTrue(urls.iterator().next().replace("\\", "/").endsWith("target/guava-io-r03.jar"));
    }

    @Test
    public void http() {
        final Set<String> urls = ProvisioningUtil.realLocation("http://repo1.maven.org/maven2/org/apache/batchee/batchee-test/0.2-incubating/batchee-test-0.2-incubating.jar");
        assertEquals(1, urls.size());
        assertTrue(urls.iterator().next().replace("\\", "/").endsWith("target/batchee-test-0.2-incubating.jar"));
    }

    private void write(final File folder, final String name) throws IOException {
        final FileWriter w = new FileWriter(new File(folder, name));
        w.write("empty");
        w.close();
    }
}
