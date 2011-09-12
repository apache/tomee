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
package org.apache.openejb.tck.cdi.tomee.embedded;

import org.apache.openejb.core.ivm.IntraVmProxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
* @version $Rev$ $Date$
*/
public class BeansImpl implements org.jboss.jsr299.tck.spi.Beans {

    public boolean isProxy(Object instance) {
        System.out.println("isProxy: " + instance);
        return instance instanceof IntraVmProxy || instance.getClass().getName().contains("$$");
    }

    @Override
    public byte[] serialize(Object instance) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(baos);
        os.writeObject(instance);
        os.flush();
        return baos.toByteArray();
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(bais);
        return is.readObject();
    }
}
