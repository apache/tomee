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
package org.apache.openejb.client.corba;

public final class InstanceOf {
    private static final Class<?> STUB;
    private static final Class<?> PORTABLE_REMOTE_OBJECT;
    private static final Class<?> REMOTE;

    static {
        Class<?> stub = null;
        Class<?> pro = null;
        Class<?> remote = null;
        try {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            stub = loader.loadClass("javax.rmi.CORBA.Stub");
            pro = loader.loadClass("javax.rmi.PortableRemoteObject");
            remote = loader.loadClass("java.rmi.Remote");
        } catch (final ClassNotFoundException e) {
            // no-op
        }
        STUB = stub;
        PORTABLE_REMOTE_OBJECT = pro;
        REMOTE = remote;
    }

    private InstanceOf() {
        // no-op
    }

    public static boolean isStub(final Object instance) {
        return STUB != null && STUB.isInstance(instance);
    }

    public static boolean isRemote(final Object obj) {
        return REMOTE != null && PORTABLE_REMOTE_OBJECT != null && REMOTE.isInstance(obj) && PORTABLE_REMOTE_OBJECT.isInstance(obj);
    }
}
