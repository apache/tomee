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
package org.apache.openjpa.lib.util;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests the {@link MultiClassLoader}.
 *
 * @author Abe White
 */
public class TestMultiClassLoader extends TestCase {

    private ClassLoader SYSTEM_LOADER = MultiClassLoader.class.getClassLoader();

    private MultiClassLoader _loader = new MultiClassLoader();

    public TestMultiClassLoader(String test) {
        super(test);
    }

    public void setUp() {
        _loader.addClassLoader(MultiClassLoader.THREAD_LOADER);
        _loader.addClassLoader(SYSTEM_LOADER);
    }

    /**
     * Tests basic add/remove functionality.
     */
    public void testBasic() {
        assertEquals(2, _loader.size());
        assertTrue(!_loader.isEmpty());
        assertTrue(_loader.getClassLoaders()[0] == SYSTEM_LOADER);
        assertEquals(Thread.currentThread().getContextClassLoader(),
            _loader.getClassLoaders()[1]);
        assertTrue(_loader.getClassLoader(0) == SYSTEM_LOADER);
        assertTrue(!_loader.addClassLoader(_loader.THREAD_LOADER));

        FooLoader foo = new FooLoader();
        assertTrue(_loader.addClassLoader(foo));
        assertTrue(!_loader.addClassLoader(foo));
        assertEquals(3, _loader.size());
        assertEquals(foo, _loader.getClassLoaders()[2]);

        assertTrue(_loader.removeClassLoader(_loader.THREAD_LOADER));
        assertTrue(!_loader.removeClassLoader(_loader.THREAD_LOADER));
        assertEquals(2, _loader.size());
        assertTrue(_loader.getClassLoaders()[0] == SYSTEM_LOADER);
        assertEquals(foo, _loader.getClassLoaders()[1]);

        assertTrue(_loader.removeClassLoader(foo));
        assertTrue(_loader.removeClassLoader(SYSTEM_LOADER));
        assertTrue(_loader.isEmpty());

        MultiClassLoader loader2 = new MultiClassLoader();
        loader2.addClassLoader(MultiClassLoader.THREAD_LOADER);
        loader2.addClassLoader(SYSTEM_LOADER);
        assertTrue(_loader.addClassLoaders(loader2));
        assertTrue(!_loader.addClassLoaders(loader2));
        FooLoader foo2 = new FooLoader();
        loader2.addClassLoader(foo);
        loader2.addClassLoader(foo2);
        assertTrue(_loader.addClassLoaders(1, loader2));
        ClassLoader[] loaders = _loader.getClassLoaders();
        assertTrue(loaders[0] == SYSTEM_LOADER);
        assertEquals(Thread.currentThread().getContextClassLoader(),
            loaders[3]);
        assertEquals(foo, loaders[1]);
        assertEquals(foo2, loaders[2]);
    }

    /**
     * Test finding classes.
     */
/*
 public void testClassForName()
 throws Exception
 {
 assertEquals(MultiClassLoader.class,
 Class.forName(MultiClassLoader.class.getName(), true, _loader));
 assertTrue(_loader.removeClassLoader(SYSTEM_LOADER));
 assertTrue(_loader.removeClassLoader(MultiClassLoader.THREAD_LOADER));
 try
 {
 // have to switch classes here b/c other is now cached
 assertEquals(TestMultiClassLoader.class, Class.forName
 (TestMultiClassLoader.class.getName(), true, _loader));
 fail("System class laoder still working.");
 } catch (ClassNotFoundException cnfe)
 {
 }
 try
 {
 Class.forName("foo", true, _loader);
 fail("Somehow found 'foo'???");
 } catch (ClassNotFoundException cnfe)
 {
 }
 _loader.addClassLoader(new FooLoader());
 assertEquals(Integer.class, Class.forName("foo", true, _loader));
 }
*/

    /**
     * Test finding resources.
     */
    public void testGetResource() {
        assertNull(_loader.getResource("foo"));
        _loader.addClassLoader(new FooLoader());
        assertNotNull(_loader.getResource("foo"));
    }

    public static Test suite() {
        return new TestSuite(TestMultiClassLoader.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    private static final class FooLoader extends ClassLoader {

        protected Class findClass(String name) throws ClassNotFoundException {
            if ("foo".equals(name))
                return Integer.class;
            throw new ClassNotFoundException(name);
        }

        protected URL findResource(String name) {
            try {
                if ("foo".equals(name))
                    return new URL("file:///dev/null");
            } catch (Exception e) {
            }
            return null;
        }
    }
}
