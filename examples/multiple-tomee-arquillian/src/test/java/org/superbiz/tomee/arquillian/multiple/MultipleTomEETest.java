/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.tomee.arquillian.multiple;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class MultipleTomEETest {

    @Deployment(name = "war1", testable = false)
    @TargetsContainer("tomee-1")
    public static WebArchive createDep1() {
        return ShrinkWrap.create(WebArchive.class, "application1.war")
                .addAsWebResource(new StringAsset("Hello from TomEE 1"), "index.html");
    }

    @Deployment(name = "war2", testable = false)
    @TargetsContainer("tomee-2")
    public static WebArchive createDep2() {
        return ShrinkWrap.create(WebArchive.class, "application2.war")
                .addAsWebResource(new StringAsset("Hello from TomEE 2"), "index.html");
    }

    @Test
    @OperateOnDeployment("war1")
    public void testRunningInDep1(@ArquillianResource final URL url) throws IOException {
        final String content = IO.slurp(url);
        assertEquals("Hello from TomEE 1", content);
    }

    @Test
    @OperateOnDeployment("war2")
    public void testRunningInDep2(@ArquillianResource final URL url) throws IOException {
        final String content = IO.slurp(url);
        assertEquals("Hello from TomEE 2", content);
    }
}
