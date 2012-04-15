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
public class CompositeArchiveTest extends TestCase {
    private CompositeArchive archive;
    Class[] classes = {Blue.class, Green.class, Red.class};

    @Override
    protected void setUp() throws Exception {
        archive = new CompositeArchive(
                new ClassesArchive(Red.class, Green.class),
                new ClassesArchive(Blue.class)
        );
    }

    public void testGetBytecode() throws Exception {

        for (Class clazz : classes) {
            assertNotNull(clazz.getName(), archive.getBytecode(clazz.getName()));
        }

        try {
            archive.getBytecode("Fake");
            fail("ClassNotFoundException should have been thrown");
        } catch (ClassNotFoundException e) {
            // pass
        }
    }

    public void testLoadClass() throws Exception {
        for (Class clazz : classes) {
            assertEquals(clazz.getName(), clazz, archive.loadClass(clazz.getName()));
        }

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
