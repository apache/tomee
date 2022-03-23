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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.openejb;

import java.net.URLClassLoader;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class LifecycleTest {
    private static int beforeClassNb = 0;
    private static int beforeNb = 0;
    private static int afterClassNb = 0;
    private static int afterNb = 0;

    @Inject
    private Foo foo;

    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class, LifecycleTest.class.getSimpleName() + ".jar")
                .addClass(Foo.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @BeforeClass
    public static void beforeClass() {
        if (beforeClassNb > 0) {
            fail();
        }
        checkCl();
        beforeClassNb++;
    }

    @Before
    public void before() {
        if (beforeNb > 0) {
            fail();
        }
        checkCl();
        assertNotNull(foo); // injections should be available
        beforeNb++;
    }

    @After
    public void after() {
        if (afterNb > 0) {
            fail();
        }
        checkCl();
        assertNotNull(foo); // injections should be available
        afterNb++;
    }

    @AfterClass
    public static void afterClass() {
        if (afterClassNb > 0) {
            fail();
        }
        checkCl();
        afterClassNb++;

        assertEquals(1, beforeClassNb);
        assertEquals(1, beforeNb);
        assertEquals(1, afterNb);
        assertEquals(1, afterClassNb);
    }

    @Test
    public void justToRunOthers() {
        // no-op
    }

    private static void checkCl() { // openejb classloader, not the app one
        assertThat(Thread.currentThread().getContextClassLoader().getParent(), instanceOf(URLClassLoader.class));
    }

    public static class Foo {

    }
}
