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
package org.apache.openejb.spi;

import org.apache.openejb.core.ObjectInputStreamFiltered;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    public static Object deserialize(final byte[] bytes)
            throws IOException, ClassNotFoundException {

        ObjectInputStream ois = null;

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStreamFiltered(bais);
            return ois.readObject();
        } finally {
            if (ois != null) {
                ois.close();
            }
        }
    }

    public static byte[] serialize(final Object object) throws IOException {

        ObjectOutputStream oos = null;

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            return baos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
        }
    }

}
