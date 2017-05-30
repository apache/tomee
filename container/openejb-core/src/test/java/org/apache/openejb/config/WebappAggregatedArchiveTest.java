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
package org.apache.openejb.config;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.apache.openejb.loader.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebappAggregatedArchiveTest {
    @Test
    public void allClassesDefined() throws MalformedURLException {
        final WebappAggregatedArchive aggregatedArchive = new WebappAggregatedArchive(
                new Module(false) {
                    @Override
                    public ClassLoader getClassLoader() {
                        return Thread.currentThread().getContextClassLoader();
                    }

                    @Override
                    public Map<String, Object> getAltDDs() {
                        return Map.class.cast(
                                singletonMap("scan.xml", Thread.currentThread().getContextClassLoader().getResource("WebappAggregatedArchiveTest.xml")));
                    }
                },
                asList(jarLocation(WebappAggregatedArchive.class).toURI().toURL(),
                        jarLocation(WebappAggregatedArchiveTest.class).toURI().toURL()));
        assertEquals(1, aggregatedArchive.getClassesMap().size());

        final URL key = jarLocation(WebappAggregatedArchiveTest.class).toURI().toURL();
        final List<String> classes = aggregatedArchive.getClassesMap().get(key);
        assertNotNull(classes);
        assertEquals(1, classes.size());
        assertEquals(WebappAggregatedArchiveTest.class.getName(), classes.iterator().next());
    }
}
