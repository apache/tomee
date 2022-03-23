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
package org.superbiz.injection.enventry;

import junit.framework.TestCase;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.Date;

//START SNIPPET: code
public class ConfigurationTest extends TestCase {

    public void test() throws Exception {
        final Context context = EJBContainer.createEJBContainer().getContext();

        final Configuration configuration = (Configuration) context.lookup("java:global/injection-of-env-entry/Configuration");

        assertEquals("orange", configuration.getColor());

        assertEquals(Shape.TRIANGLE, configuration.getShape());

        assertEquals(Widget.class, configuration.getStrategy());

        assertEquals(new Date(123456789), configuration.getDate());
    }
}
//END SNIPPET: code
 


