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
package org.apache.openejb.client;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @version $Rev$ $Date$
 */
public class JndiRequestTest extends TestCase {

    public void testExternalize() throws Exception {
        JNDIRequest expected = new JNDIRequest(RequestMethodConstants.JNDI_LOOKUP, "this/is/a/jndi/name");
        JNDIRequest actual = new JNDIRequest();

        externalize(expected, actual);

        assertEquals("Request method not the same", expected.getRequestMethod(), actual.getRequestMethod());
        assertEquals("ModuleId not the same", expected.getModuleId(), actual.getModuleId());
        assertEquals("JNDI Name not the same", expected.getRequestString(), actual.getRequestString());
    }


    public void testExternalize2() throws Exception {
        JNDIRequest expected = new JNDIRequest(RequestMethodConstants.JNDI_LOOKUP, "this/is/a/jndi/name");
        expected.setModuleId("foobar");
        JNDIRequest actual = new JNDIRequest();

        externalize(expected, actual);

        assertEquals("Request method not the same", expected.getRequestMethod(), actual.getRequestMethod());
        assertEquals("ModuleId not the same", expected.getModuleId(), actual.getModuleId());
        assertEquals("JNDI Name not the same", expected.getRequestString(), actual.getRequestString());
    }


    private void externalize(Externalizable original, Externalizable copy) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        original.writeExternal(out);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);

        copy.readExternal(in);
    }
}