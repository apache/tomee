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
package org.apache.openjpa.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.MultiClassLoader;

/**
 * Helper class to serialize and deserialize persistent objects,
 * subtituting oids into the serialized stream and subtituting the persistent
 * objects back during deserialization.
 *
 * @author Abe White
 * @since 0.3.3
 * @nojavadoc
 */
public class Serialization {

    private static final Localizer _loc = Localizer.forPackage
        (Serialization.class);

    /**
     * Serialize a value that might contain persistent objects. Replaces
     * persistent objects with their oids.
     */
    public static byte[] serialize(Object val, StoreContext ctx) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objs = new PersistentObjectOutputStream(bytes,
                ctx);
            objs.writeObject(val);
            objs.flush();
            return bytes.toByteArray();
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    /**
     * Deserialize an object value from the given bytes.
     */
    public static Object deserialize(byte[] bytes, StoreContext ctx) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return deserialize(in, ctx);
    }

    /**
     * Deserialize an object value from the given stream.
     */
    public static Object deserialize(InputStream in, StoreContext ctx) {
        try {
            if (ctx == null)
                return new ClassResolvingObjectInputStream(in).readObject();
            return new PersistentObjectInputStream(in, ctx).readObject();
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    /**
     * Object output stream that replaces persistent objects with their oids.
     */
    public static class PersistentObjectOutputStream
        extends ObjectOutputStream {

        private StoreContext _ctx;

        /**
         * Constructor; supply underlying stream.
         */
        public PersistentObjectOutputStream(OutputStream delegate,
            StoreContext ctx)
            throws IOException {
            super(delegate);
            _ctx = ctx;
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    enableReplaceObject(true);
                    return null;
                }
            });
        }

        protected Object replaceObject(Object obj) {
            Object oid = _ctx.getObjectId(obj);
            return (oid == null) ? obj : new ObjectIdMarker(oid);
        }
    }

    public static class ClassResolvingObjectInputStream
        extends ObjectInputStream {

        public ClassResolvingObjectInputStream(InputStream delegate)
            throws IOException {
            super(delegate);
        }

        protected Class resolveClass(ObjectStreamClass desc) 
            throws IOException, ClassNotFoundException {
            MultiClassLoader loader = AccessController
                .doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());
            addContextClassLoaders(loader);
            loader.addClassLoader(getClass().getClassLoader());
            loader.addClassLoader(MultiClassLoader.SYSTEM_LOADER);
            return Class.forName(desc.getName(), true, loader);
        }

        protected void addContextClassLoaders(MultiClassLoader loader) {
            loader.addClassLoader(AccessController.doPrivileged(
                J2DoPrivHelper.getContextClassLoaderAction()));
        }
    }

    /**
     * Object input stream that replaces oids with their objects.
     */
    public static class PersistentObjectInputStream
        extends ClassResolvingObjectInputStream {

        private final StoreContext _ctx;

        /**
         * Constructor; supply source stream and broker to
         * use for persistent object lookups.
         */
        public PersistentObjectInputStream(InputStream delegate,
            StoreContext ctx)
            throws IOException {
            super(delegate);
            _ctx = ctx;
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    enableResolveObject(true);
                    return null;
                }
            });
        }

        protected void addContextClassLoaders(MultiClassLoader loader) {
            super.addContextClassLoaders(loader);
            loader.addClassLoader(_ctx.getClassLoader());
        }

        protected Object resolveObject(Object obj) {
            if (!(obj instanceof ObjectIdMarker))
                return obj;

            Object oid = ((ObjectIdMarker) obj).oid;
            if (oid == null)
                return null;

            Object pc = _ctx.find(oid, null, null, null, 0);
            if (pc == null) {
                Log log = _ctx.getConfiguration().getLog
                    (OpenJPAConfiguration.LOG_RUNTIME);
                if (log.isWarnEnabled())
                    log.warn(_loc.get("bad-ser-oid", oid));
                if (log.isTraceEnabled())
                    log.trace(new ObjectNotFoundException(oid));
            }
            return pc;
        }
    }

    /**
     * Marker for oids.
     */
    private static class ObjectIdMarker
        implements Serializable {

        public Object oid;

        public ObjectIdMarker(Object oid) {
            this.oid = oid;
		}
	} 
}

