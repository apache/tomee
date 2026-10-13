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
package org.apache.openejb.core.stateful;

import org.apache.openejb.SystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class RAFPassivaterTest {

    private static final byte[] PLANTED = {1, 2, 3};

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private String originalTmpDir;
    private Path tmp;

    @Before
    public void useOwnTmpDir() throws IOException {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        tmp = folder.newFolder("tmp").toPath();
        System.setProperty("java.io.tmpdir", tmp.toString());
    }

    @After
    public void restoreTmpDir() {
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }

    @Test
    public void activateReturnsPassivatedState() throws Exception {
        final RAFPassivater passivater = new RAFPassivater();

        final Map<Object, Object> first = new HashMap<>();
        first.put("a", "x");
        first.put("b", new ArrayList<>(List.of("some", "longer", "state")));
        first.put("c", 42);
        passivater.passivate(first);

        final Map<Object, Object> second = new HashMap<>();
        second.put("d", "y".repeat(1000));
        second.put("e", new HashMap<>(Map.of("k", "v")));
        passivater.passivate(second);

        final Map<Object, Object> all = new HashMap<>(first);
        all.putAll(second);
        for (final Map.Entry<Object, Object> entry : all.entrySet()) {
            assertEquals(entry.getValue(), passivater.activate(entry.getKey()));
        }
        assertNull(passivater.activate("unknown"));
    }

    @Test
    public void passivatesIntoPrivateDirectory() throws Exception {
        final RAFPassivater passivater = new RAFPassivater();
        passivater.passivate(state("a"));
        passivater.passivate(state("b"));

        final Path directory = privateDirectory();
        assertTrue(Files.isRegularFile(directory.resolve("passivation0.ser")));
        assertTrue(Files.isRegularFile(directory.resolve("passivation1.ser")));
        if (directory.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            assertEquals(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE),
                Files.getPosixFilePermissions(directory));
        }
    }

    @Test
    public void ignoresFilesPlantedInTmpDir() throws Exception {
        final Path planted = Files.write(tmp.resolve("passivation0.ser"), PLANTED);
        final Path target = Files.write(tmp.resolve("target"), PLANTED);
        final boolean linked = link(tmp.resolve("passivation1.ser"), target);

        final RAFPassivater passivater = new RAFPassivater();
        passivater.passivate(state("a"));
        passivater.passivate(state("b"));

        assertEquals("a", passivater.activate("a"));
        assertEquals("b", passivater.activate("b"));
        assertArrayEquals(PLANTED, Files.readAllBytes(planted));
        if (linked) {
            assertArrayEquals(PLANTED, Files.readAllBytes(target));
        }
    }

    @Test
    public void refusesExistingFile() throws Exception {
        final RAFPassivater passivater = new RAFPassivater();
        passivater.passivate(state("a"));

        final Path planted = Files.write(privateDirectory().resolve("passivation1.ser"), PLANTED);
        assertRefused(passivater);
        assertArrayEquals(PLANTED, Files.readAllBytes(planted));
    }

    @Test
    public void refusesLink() throws Exception {
        final RAFPassivater passivater = new RAFPassivater();
        passivater.passivate(state("a"));

        final Path target = Files.write(tmp.resolve("target"), PLANTED);
        assumeTrue("symbolic links not supported", link(privateDirectory().resolve("passivation1.ser"), target));
        assertRefused(passivater);
        assertArrayEquals(PLANTED, Files.readAllBytes(target));
    }

    private Path privateDirectory() throws IOException {
        try (final Stream<Path> entries = Files.list(tmp)) {
            final List<Path> directories = entries.filter(Files::isDirectory).toList();
            assertEquals("expected a single passivation directory in " + tmp, 1, directories.size());
            return directories.get(0);
        }
    }

    private static void assertRefused(final RAFPassivater passivater) {
        try {
            passivater.passivate(state("b"));
            fail("an existing passivation file must not be reused");
        } catch (final SystemException expected) {
            // ok
        }
    }

    private static boolean link(final Path link, final Path target) {
        try {
            Files.createSymbolicLink(link, target);
            return true;
        } catch (final UnsupportedOperationException | IOException e) {
            return false;
        }
    }

    private static Map<Object, Object> state(final String value) {
        final Map<Object, Object> state = new HashMap<>();
        state.put(value, value);
        return state;
    }
}
