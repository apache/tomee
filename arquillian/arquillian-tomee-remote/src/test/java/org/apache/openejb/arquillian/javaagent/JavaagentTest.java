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
package org.apache.openejb.arquillian.javaagent;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.management.ManagementFactory;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class JavaagentTest {
    @Deployment
    public static Archive<?> empty() {
        return ShrinkWrap.create(WebArchive.class, "javaagent.war")
                .addAsResource(EmptyAsset.INSTANCE, "foo");
    }

    @Test
    public void checkAgent() {
        final List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (final String arg : inputArguments) {
            if (arg.startsWith("-javaagent") && arg.endsWith("cfgagent-1.0.0.jar")) {
                return;
            }
        }
        fail("didnt find sirona as javaagent: " + inputArguments.toString());
    }
}
