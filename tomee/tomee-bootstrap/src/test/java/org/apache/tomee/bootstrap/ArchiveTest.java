/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.bootstrap;

import org.apache.openejb.loader.IO;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copied with permission from tomitribe-util
 */
public class ArchiveTest {

    @Test
    public void addString() throws IOException {
        final Archive archive = new Archive()
                .add("colors/red.txt", "crimson")
                .add("colors/green/emerald.txt", "#50c878");

        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("colors", dir.listFiles()[0].getName());
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    @Test
    public void addArchive() throws IOException {

        final Archive nested = new Archive()
                .add("red.txt", "crimson")
                .add("green/emerald.txt", "#50c878");
        final Archive archive = new Archive();
        archive.add("colors", nested);
        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("colors", dir.listFiles()[0].getName());
        assertFile(dir, "colors/red.txt", "crimson");
        assertFile(dir, "colors/green/emerald.txt", "#50c878");
    }

    /**
     * When adding an inner class we must also add the parent as the child
     * cannot be loaded without the parent class
     */
    @Test
    public void addInnerClass() throws IOException {

        final Archive archive = new Archive().add(MomINeedYou.class);
        final File dir = archive.toDir();

        assertTrue(dir.isDirectory());
        assertEquals(1, dir.listFiles().length);
        assertEquals("org", dir.listFiles()[0].getName());

        final File parent = new File(dir, "org/apache/tomee/bootstrap/ArchiveTest.class");
        final File child = new File(dir, "org/apache/tomee/bootstrap/ArchiveTest$MomINeedYou.class");
        assertTrue(parent.exists());
        assertTrue(child.exists());
    }

    private static void assertFile(final File dir, final String name, final String expected) throws IOException {
        final File file = new File(dir, name);
        assertTrue(name, file.exists());
        assertEquals(expected, IO.slurp(file));
    }

    /**
     * Inner classes cannot be loaded without their parent,
     * so we must always include the parent by default
     */
    public static class MomINeedYou {

    }
}