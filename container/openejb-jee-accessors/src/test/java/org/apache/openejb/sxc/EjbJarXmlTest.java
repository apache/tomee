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
package org.apache.openejb.sxc;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar$JAXB;
import org.apache.openejb.loader.IO;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarXmlTest extends TestCase {

    /**
     * TODO Doesn't seem there are any asserts here
     * @throws Exception
     */
    public void testEjbJar() throws Exception {
        String fileName = "ejb-jar-example1.xml";

        final Event test = Event.start("Test");

        final URL resource = this.getClass().getClassLoader().getResource(fileName);

        final String expected = IO.slurp(resource);

        final Event ejbJarJAXBCreate = Event.start("EjbJarJAXBCreate");
        ejbJarJAXBCreate.stop();

        final Event unmarshalEvent = Event.start("unmarshal");
        final Object value;

        final EjbJar$JAXB jaxbType = new EjbJar$JAXB();
        value = Sxc.unmarshalJavaee(resource, jaxbType);

        unmarshalEvent.stop();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final Event marshall = Event.start("marshall");
        Sxc.marshall(jaxbType, value, baos);
        marshall.stop();

        final String result = new String(baos.toByteArray());
        assertEquals(expected, result);
        test.stop();
    }

    private static class Event {
        protected final long start = System.nanoTime();
        private final String description;

        private Event(final String description) {
            this.description = description;
        }

        public static Event start(final String description) {
            return new Event(description);
        }

        public void stop() {
            final String format = String.format("JAXBContext.newInstance %s  %s", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.start), this.description);
            System.out.println(format);
        }
    }

}
