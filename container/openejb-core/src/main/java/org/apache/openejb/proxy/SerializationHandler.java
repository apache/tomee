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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.proxy;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.rmi.MarshalledObject;

import org.apache.openejb.util.ObjectInputStreamExt;
import org.omg.CORBA.ORB;


public class SerializationHandler {
    private static InheritableThreadLocal serializationState = new InheritableThreadLocal();

    /**
     * This method is public so it can be called by other parts of the
     * container during their serialization operations, namely session
     * passivation
     */
    public static void setStrategy(ReplacementStrategy strategy) {
        serializationState.set(strategy);
    }

    private static ReplacementStrategy getStrategy() {
        ReplacementStrategy replacementStrategy = (ReplacementStrategy) serializationState.get();
        if (replacementStrategy == null) {
            return ReplacementStrategy.REPLACE;
        }
        return replacementStrategy;
    }

    public static void copyArgs(Object[] objects) throws IOException, ClassNotFoundException {
        for (int i = 0; i < objects.length; i++) {
            Object originalObject = objects[i];
            Object copy = copyObj(originalObject);
            // connect a coppied stub to the same orb as the original stub
            if (copy instanceof javax.rmi.CORBA.Stub) {
                ORB orb = ((javax.rmi.CORBA.Stub)originalObject)._orb();
                if (orb != null) {
                    ((javax.rmi.CORBA.Stub)copy).connect(orb);
                }
            }
            objects[i] = copy;
        }
    }

    public static Object copyObj(Object object) throws IOException, ClassNotFoundException {
        MarshalledObject obj = new MarshalledObject(object);
        return obj.get();
    }

    public static Object writeReplace(Object object, ProxyInfo proxyInfo) throws ObjectStreamException {
        return getStrategy().writeReplace(object, proxyInfo);
    }

    public static void copyArgs(ClassLoader classLoader, Object[] objects) throws IOException, ClassNotFoundException {
        for (int i = 0; i < objects.length; i++) {
            objects[i] = copyObj(classLoader, objects[i]);
        }
    }

    public static Object copyObj(ClassLoader classLoader, Object object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStreamExt ois = new ObjectInputStreamExt(bais, classLoader);
        return ois.readObject();
    }
}

