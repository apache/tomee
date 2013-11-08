/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.lib.conf;

import java.util.List;
import java.util.MissingResourceException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;
import org.apache.openjpa.lib.util.Options;

public class TestAnchorParsing extends TestCase {

    public void testFQAnchor() {
        String fqLoc = "META-INF/persistence.xml#test";
        Options opts = new Options();
        opts.setProperty("p", fqLoc);
        List<String> locs =
            Configurations.getFullyQualifiedAnchorsInPropertiesLocation(opts);
        assertNotNull(locs);
        assertEquals(1, locs.size());
        assertEquals(fqLoc, locs.get(0));
    }

    public void testNoResource() {
        allHelper(null);
    }

    public void testNoAnchor() {
        allHelper("META-INF/persistence.xml");
    }

    private void allHelper(String resource) {
        Options opts = new Options();
        if (resource != null)
            opts.setProperty("p", resource);
        List<String> locs =
            Configurations.getFullyQualifiedAnchorsInPropertiesLocation(opts);
        assertNotNull(locs);
        // approximate so that if someone adds more units, this doesn't break
        assertTrue(locs.size() >= 4);
        assertTrue(locs.contains("META-INF/persistence.xml#test"));
        assertTrue(locs.contains(
            "META-INF/persistence.xml#second-persistence-unit"));
        assertTrue(locs.contains(
            "META-INF/persistence.xml#third-persistence-unit"));
        assertTrue(locs.contains("META-INF/persistence.xml#invalid"));
    }

    public void testProductDerivationsLoadResource() {
        ProductDerivations.load(
            "org/apache/openjpa/lib/conf/product-derivations-load.xml",
            "foo", null);

        ProductDerivations.load(
            "org/apache/openjpa/lib/conf/product-derivations-load.xml",
            null, null);

        try {
            ProductDerivations.load(
                "org/apache/openjpa/lib/conf/product-derivations-load.xml",
                "nonexistant", null);
            fail("pu 'nonexistant' does not exist");
        } catch (MissingResourceException mre) {
            // expected
        }

        try {
            ProductDerivations.load(
                "org/apache/openjpa/lib/conf/product-derivations-load.xml",
                "", null);
            fail("pu '' does not exist");
        } catch (MissingResourceException mre) {
            // expected
        }
    }

    public void testNonexistantResourceLoad() {
        try {
            ProductDerivations.load("nonexistant-resource", null, null);
            fail("resource 'nonexistant-resource' should not exist");
        } catch (MissingResourceException e) {
            // expected
        }
    }

    public void testProductDerivationsLoadFile() throws IOException {
        File validFile = resourceToTemporaryFile(
            "org/apache/openjpa/lib/conf/product-derivations-load.xml");

        ProductDerivations.load(validFile, "foo", null);

        ProductDerivations.load(validFile, null, null);

        try {
            ProductDerivations.load(validFile, "nonexistant", null);
            fail("pu 'nonexistant' does not exist");
        } catch (MissingResourceException mre) {
            // expected
        }

        try {
            ProductDerivations.load(validFile, "", null);
            fail("pu '' does not exist");
        } catch (MissingResourceException mre) {
            // expected
        }
        validFile = null;
    }

    public void testNonexistantFileLoad() {
        File f = new File("this-should-not-exist");
        assertFalse(f.exists());
        try {
            ProductDerivations.load(f, null, null);
            fail(f.getName() + " does not exist");
        } catch (MissingResourceException e) {
            // expected
        }
    }

    private File resourceToTemporaryFile(String s) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(s);
        File f = File.createTempFile("TestAnchorParsing", ".xml");
        OutputStream out = new FileOutputStream(f);
        byte[] bytes = new byte[1024];
        while (true) {
            int count = in.read(bytes);
            if (count < 0)
                break;
            out.write(bytes, 0, count);
        }
        in.close();
        out.close();
        f.deleteOnExit();
        return f;
    }
}
