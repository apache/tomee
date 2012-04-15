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
import org.acme.foo.Blue;
import org.acme.foo.Green;
import org.acme.foo.Red;
import org.apache.xbean.finder.UrlSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ClasspathArchiveTest extends TestCase {


    private ClasspathArchive archive;
    private final Class[] classes = {Blue.class, Blue.Navy.class, Blue.Sky.class, Green.class, Green.Emerald.class, Red.class, Red.CandyApple.class, Red.Pink.class};

    @Override
    protected void setUp() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        UrlSet urlSet = new UrlSet(classLoader);

        if (classLoader.getParent() != null) {
            urlSet = urlSet.exclude(classLoader.getParent());
        }

        urlSet = urlSet.excludeJavaHome();

        archive = new ClasspathArchive(classLoader, urlSet.getUrls());
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

    public void testArchives() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        UrlSet urlSet = new UrlSet(classLoader);

        if (classLoader.getParent() != null) {
            urlSet = urlSet.exclude(classLoader.getParent());
        }

        urlSet = urlSet.excludeJavaHome();

        List<Archive> list = ClasspathArchive.archives(classLoader, urlSet.getUrls());


        // At least classes, test-classes and junit.jar should be here
        assertTrue(list.size() >= 3);

        // target/classes/ and target/test-classes/
        assertTrue(sublist(list, FileArchive.class).size() >= 2);

        // junit
        assertTrue(sublist(list, JarArchive.class).size() >= 1);
    }

    private <T> List<T> sublist(List<Archive> list, Class<? extends T> type) {
        List<T> ts = new ArrayList<T>();
        for (Archive archive : list) {

            if (type.isAssignableFrom(archive.getClass())) {
                T t = (T) archive;
                ts.add(t);
            }
        }
        return ts;
    }

}
