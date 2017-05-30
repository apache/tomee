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
package org.apache.openejb.arquillian.tests.tomcatscanning;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class TomcatScanningTest {
    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, "TomcatScanningTest.war")
                .addAsLibraries(
                        ShrinkWrap.create(JavaArchive.class, "ant.jar"/*excluded by tomcat only*/).addClass(E1.class),
                        ShrinkWrap.create(JavaArchive.class, "log4j-taglib-isincludedbytomcat.jar"/*excluded by tomee, included by tomcat*/).addClass(E2.class),
                        ShrinkWrap.create(JavaArchive.class, "neethi-whatever.jar"/*excluded by tomee only*/).addClass(E3.class)
                );
    }

    @ArquillianResource
    private URL base;

    @Test(expected = IOException.class)
    public void antIsNotVisible() throws IOException {
        IO.slurp(new URL(base.toExternalForm() + "e1"));
    }

    @Test
    public void log4jTaglibIsVisible() throws IOException {
        assertEquals("2", IO.slurp(new URL(base.toExternalForm() + "e2")).trim());
    }

    @Test(expected = IOException.class)
    public void neethiIsNotVisible() throws IOException {
        IO.slurp(new URL(base.toExternalForm() + "e3"));
    }
}
