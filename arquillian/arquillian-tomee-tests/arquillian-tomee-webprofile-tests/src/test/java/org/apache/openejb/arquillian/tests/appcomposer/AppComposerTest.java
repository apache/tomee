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
package org.apache.openejb.arquillian.tests.appcomposer;

import org.apache.openejb.arquillian.common.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@Ignore
@RunWith(Arquillian.class)
public class AppComposerTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "AppComposerTest.war")
                .addClasses(AppComposerConfiguration.class, Bean1.class, Bean2.class, Endpoint.class);
    }

    @ArquillianResource
    private URL url;

    @Test
    public void get() throws IOException {
        assertEquals(
                "org.apache.openejb.arquillian.tests.appcomposer.Bean1_true_org.apache.openejb.persistence.JtaEntityManager",
                IO.slurp(new URL(url.toExternalForm() + "endpoint")));
    }
}
