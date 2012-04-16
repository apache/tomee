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
package org.apache.xbean.finder.archive;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ClassesArchiveTest extends TestCase {
    private ClassesArchive archive;

    @Override
    protected void setUp() throws Exception {
        archive = new ClassesArchive(Red.class, Green.class, Blue.class);
    }

    public void testGetBytecode() throws Exception {

        assertNotNull(archive.getBytecode(Blue.class.getName()));
        assertNotNull(archive.getBytecode(Green.class.getName()));
        assertNotNull(archive.getBytecode(Red.class.getName()));

        try {
            archive.getBytecode("Fake");
            fail("ClassNotFoundException should have been thrown");
        } catch (ClassNotFoundException e) {
            // pass
        }
    }

    public void testLoadClass() throws Exception {
        assertEquals(Blue.class, archive.loadClass(Blue.class.getName()));
        assertEquals(Green.class, archive.loadClass(Green.class.getName()));
        assertEquals(Red.class, archive.loadClass(Red.class.getName()));

        try {
            archive.loadClass("Fake");
            fail("ClassNotFoundException should have been thrown");
        } catch (ClassNotFoundException e) {
            // pass
        }
    }

    public void testIterator() throws Exception {
        List<String> classes = new ArrayList<String>();
        for (Archive.Entry entry : archive) {
            classes.add(entry.getName());
        }

        assertFalse(0 == classes.size());
        assertTrue(classes.contains(Blue.class.getName()));
        assertTrue(classes.contains(Red.class.getName()));
        assertTrue(classes.contains(Green.class.getName()));
        assertEquals(3, classes.size());
    }

    public static class Red {
    }

    public static class Green {
    }

    public static class Blue {
    }

}
