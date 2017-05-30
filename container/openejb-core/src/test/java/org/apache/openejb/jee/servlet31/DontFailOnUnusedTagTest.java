/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.servlet31;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebApp$JAXB;
import org.apache.openejb.sxc.Sxc;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class DontFailOnUnusedTagTest {
    @Test
    public void run() throws Exception { // this test just validates we passthrough on unknown elements (tomcat will fail/warn)
        try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("webxml31.xml")) {
            final WebApp web = Sxc.unmarshalJavaee(new WebApp$JAXB(), is);
            assertNotNull(web.getAbsoluteOrdering());
        }
    }
}
