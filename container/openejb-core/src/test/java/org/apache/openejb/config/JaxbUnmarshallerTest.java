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
package org.apache.openejb.config;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.openejb.jee.Application;

/**
 * @version $Rev$ $Date$
 */
public class JaxbUnmarshallerTest extends TestCase {

    public final void testUnmarshalURL() {
        try {
            final URL applicationXmlUrl = new File("target/test-classes/META-INF/application.xml").toURL();
            JaxbUnmarshaller unmarshaller = new JaxbUnmarshaller(Application.class, "META-INF/application.xml");
            unmarshaller.unmarshal(applicationXmlUrl);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

}
