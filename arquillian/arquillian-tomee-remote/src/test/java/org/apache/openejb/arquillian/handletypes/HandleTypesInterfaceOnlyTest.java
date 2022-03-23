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
package org.apache.openejb.arquillian.handletypes;

import org.apache.openejb.arquillian.common.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.servlet.ServletContainerInitializer;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class HandleTypesInterfaceOnlyTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "interfaceonly.war")
            .addClasses(API.class, Impl.class, InitInterfaceOnly.class, AnotherChild.class, Decorated.class, Decoration.class)
            .addAsServiceProvider(ServletContainerInitializer.class, InitInterfaceOnly.class);
    }

    @ArquillianResource
    private URL url;

    @Test
    public void check() throws IOException {
        final String content = IO.slurp(new URL(url.toExternalForm() + "list"));
        assertThat(content, containsString(Impl.class.getName()));
        assertThat(content, containsString(AnotherChild.class.getName()));
        assertThat(content, not(containsString(API.class.getName())));
        assertThat(content, not(containsString(Decorated.class.getName())));
        assertThat(content, not(containsString(Decoration.class.getName())));
    }
}
