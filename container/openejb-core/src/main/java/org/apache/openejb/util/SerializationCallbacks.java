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
package org.apache.openejb.util;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectStreamException;

/**
 * This class exists primarily to document the serialization API.
 *
 * Don't subclass from this class, simply implement Serializable
 * and copy/paste the method signatures you wish implement.
 *
 * All of the methods are optional in the Serialization API 
 */
public class SerializationCallbacks implements Serializable {

    /**
     * ANY-ACCESS-MODIFIER  static final long serialVersionUID = VALUE;
     */
    private static final long serialVersionUID = 42L;

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // optional
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();  // optional
    }

    /**
     * ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException
     * @return
     * @throws java.io.ObjectStreamException
     */
    protected Object writeReplace() throws ObjectStreamException {
        return null;
    }

    /**
     * ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;
     * @return
     * @throws java.io.ObjectStreamException
     */
    protected Object readResolve() throws ObjectStreamException {
        return null;
    }

}
