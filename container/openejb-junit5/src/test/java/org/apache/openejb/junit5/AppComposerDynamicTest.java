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
package org.apache.openejb.junit5;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit5.RunWithApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWithApplicationComposer
public class AppComposerDynamicTest {

    private static final Map<String, String> domainMap = new HashMap<>();

    {
        domainMap.put("www.somedomain.com","154.174.10.56");
        domainMap.put("www.anotherdomain.com","211.152.104.132");
        domainMap.put("www.yetanotherdomain.com","78.144.120.15");
    }

    @Inject
    private DemoResolver resolver;

    @Module
    @Classes(innerClassesAsBean = true, cdi = true)
    public WebApp web() {
        return new WebApp();
    }

    @TestFactory
    public Stream<DynamicTest> dynamicTestsFromStream() {

        Set<String> input = domainMap.keySet();

        return input.stream()
                .map(dom -> DynamicTest.dynamicTest("Resolving: " + dom,
                        () -> {
                            assertEquals(domainMap.get(dom), resolver.resolve(dom));
                        }));
    }

    public static class DemoResolver {
        public String resolve(String domain) {
            return domainMap.get(domain);
        }

    }
}
