/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.enhance;


import org.apache.openjpa.persistence.test.SingleEMFTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * This unit test checks the enhancement of a superclass/subclass
 * constellation.
 */
public class TestClassHierarchyEnhancement extends SingleEMFTestCase {

    /**
     * This tests OPENJPA-1912.
     */
    public void testSerialize() throws Exception {
        // we don't even need an EntityManager for that ;)

        EnhancedSubClass entity = new EnhancedSubClass();
        entity.setValueInSubclass("sub");
        entity.setValueInSuperclass("super");

        byte[] serializedForm = serializeObject(entity);
        EnhancedSubClass newEntity = (EnhancedSubClass) deSerializeObject(serializedForm);
    }

    private byte[] serializeObject(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    private Object deSerializeObject(byte[] serial)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serial);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

}
