/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.hessian;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.util.reflection.Reflections;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

// done by relfection to let hessian be in the app
public class HessianServer {
    public static final String CONTENT_TYPE_HESSIAN = "application/x-hessian";

    private static final Class<?>[] BOOLEAN_PARAM = new Class<?>[]{boolean.class};
    private static final Object[] TRUE_PARAM = new Object[]{true};

    private final ClassLoader loader;
    private final Class<?> serializerFactoryClass;

    private Object serializerFactory;
    private Object skeleton;
    private Logger debugLogger = null;

    public HessianServer(final ClassLoader classLoader) throws HessianIsMissingException {
        this.loader = classLoader;

        try {
            serializerFactoryClass = classLoader.loadClass("com.caucho.hessian.io.SerializerFactory");
            serializerFactory = serializerFactoryClass.getConstructor(ClassLoader.class).newInstance(loader);
        } catch (final Exception e) {
            throw new HessianIsMissingException(e);
        }
        Reflections.invokeByReflection(serializerFactory, "setAllowNonSerializable", BOOLEAN_PARAM, TRUE_PARAM);
    }

    public HessianServer serializerFactory(final Object serializerFactory) {
        this.serializerFactory = serializerFactory;
        return this;
    }

    public HessianServer sendCollectionType(final boolean sendCollectionType) {
        Reflections.invokeByReflection(serializerFactory, "setSendCollectionType", BOOLEAN_PARAM, new Object[]{sendCollectionType});
        return this;
    }

    public HessianServer debug(boolean debug) {
        this.debugLogger = (debug ? Logger.getLogger("OpenEJB.hessian.DEBUG") : null);
        return this;
    }

    public HessianServer createSkeleton(final Object instance, final Class<?> itf) {
        try {
            skeleton = loader.loadClass("com.caucho.hessian.server.HessianSkeleton").getConstructor(Object.class, Class.class).newInstance(instance, itf);
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
        return this;
    }

    public Object getSerializerFactory() {
        return serializerFactory;
    }

    public void invoke(final InputStream inputStream, final OutputStream outputStream) throws Throwable {
        InputStream isToUse = inputStream;
        OutputStream osToUse = outputStream;

        if (debugLogger != null && debugLogger.isLoggable(Level.FINE)) {
            isToUse = InputStream.class.cast(loader.loadClass("com.caucho.hessian.io.HessianDebugInputStream").getConstructor(InputStream.class, Logger.class, Level.class).newInstance(inputStream, debugLogger, Level.FINE));
            Reflections.invokeByReflection(isToUse, "startTop2", new Class<?>[0], null);

            osToUse = OutputStream.class.cast(loader.loadClass("com.caucho.hessian.io.HessianDebugOutputStream").getConstructor(OutputStream.class, Logger.class, Level.class).newInstance(outputStream, debugLogger, Level.FINE));
            Reflections.invokeByReflection(osToUse, "startTop2", new Class<?>[0], null);
        }

        if (!isToUse.markSupported()) {
            isToUse = new BufferedInputStream(isToUse);
            isToUse.mark(1);
        }

        int code = isToUse.read();
        int major;
        int minor;

        Object in;
        Object out;

        if (code == 'H' || code == 'C') { // Hessian 2.0 stream
            major = isToUse.read();
            minor = isToUse.read();
            if (major != 0x02) {
                throw new IOException("Version " + major + "." + minor + " is not understood");
            }
            in = loader.loadClass("com.caucho.hessian.io.Hessian2Input").getConstructor(InputStream.class).newInstance(isToUse);
            out = loader.loadClass("com.caucho.hessian.io.Hessian2Output").getConstructor(OutputStream.class).newInstance(osToUse);
            Reflections.invokeByReflection(in, "readCall", new Class<?>[0], null);
        } else if (code == 'c') { // Hessian 1.0 call
            major = isToUse.read();
            minor = isToUse.read();
            in = loader.loadClass("com.caucho.hessian.io.HessianInput").getConstructor(InputStream.class).newInstance(isToUse);
            if (major >= 2) {
                out = loader.loadClass("com.caucho.hessian.io.Hessian2Output").getConstructor(OutputStream.class).newInstance(osToUse);
            } else {
                out = loader.loadClass("com.caucho.hessian.io.HessianOutput").getConstructor(OutputStream.class).newInstance(osToUse);
            }
        } else {
            throw new IOException("Expected 'H'/'C' (Hessian 2.0) or 'c' (Hessian 1.0) in hessian input at " + code);
        }

        Reflections.invokeByReflection(in, "setSerializerFactory", new Class<?>[]{serializerFactoryClass}, new Object[]{serializerFactory});
        Reflections.invokeByReflection(out, "setSerializerFactory", new Class<?>[]{serializerFactoryClass}, new Object[]{serializerFactory});

        try {
            Reflections.invokeByReflection(skeleton, "invoke", new Class<?>[]{loader.loadClass("com.caucho.hessian.io.AbstractHessianInput"), loader.loadClass("com.caucho.hessian.io.AbstractHessianOutput")}, new Object[]{in, out});
        } finally {
            try {
                Reflections.invokeByReflection(in, "close", new Class<?>[0], null);
                isToUse.close();
            } catch (final IOException ex) {
                // ignore
            }
            try {
                Reflections.invokeByReflection(out, "close", new Class<?>[0], null);
                osToUse.close();
            } catch (final IOException ex) {
                // ignore
            }
        }
    }

    public static class HessianIsMissingException extends Exception {
        public HessianIsMissingException(final Exception e) {
            super(e);
        }
    }
}
