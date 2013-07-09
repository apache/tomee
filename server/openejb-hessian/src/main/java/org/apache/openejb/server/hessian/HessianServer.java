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

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianDebugInputStream;
import com.caucho.hessian.io.HessianDebugOutputStream;
import com.caucho.hessian.io.HessianFactory;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HessianServer {
    public static final String CONTENT_TYPE_HESSIAN = "application/x-hessian";

    private final ClassLoader loader;

    private SerializerFactory serializerFactory;
    private HessianSkeleton skeleton;
    private Logger debugLogger = null;

    public HessianServer(final ClassLoader classLoader) {
        this.loader = classLoader;

        serializerFactory = new SerializerFactory(loader);
        serializerFactory.setAllowNonSerializable(true);
    }

    public HessianServer serializerFactory(final SerializerFactory serializerFactory) {
        this.serializerFactory = (serializerFactory != null ? serializerFactory : new SerializerFactory(loader));
        return this;
    }

    public HessianServer sendCollectionType(final boolean sendCollectionType) {
        this.serializerFactory.setSendCollectionType(sendCollectionType);
        return this;
    }

    public HessianServer debug(boolean debug) {
        this.debugLogger = (debug ? Logger.getLogger("OpenEJB.hessian.DEBUG") : null);
        return this;
    }

    public HessianServer createSkeleton(final Object instance, final Class<?> itf) {
        skeleton = new HessianSkeleton(instance, itf);
        return this;
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    public void invoke(final InputStream inputStream, final OutputStream outputStream) throws Throwable {
        InputStream isToUse = inputStream;
        OutputStream osToUse = outputStream;

        if (debugLogger != null && debugLogger.isLoggable(Level.FINE)) {
            final HessianDebugInputStream dis = new HessianDebugInputStream(inputStream, debugLogger, Level.FINE);
            dis.startTop2();
            final HessianDebugOutputStream dos = new HessianDebugOutputStream(outputStream, debugLogger, Level.FINE);
            dos.startTop2();
            isToUse = dis;
            osToUse = dos;
        }

        if (!isToUse.markSupported()) {
            isToUse = new BufferedInputStream(isToUse);
            isToUse.mark(1);
        }

        int code = isToUse.read();
        int major;
        int minor;

        AbstractHessianInput in;
        AbstractHessianOutput out;

        if (code == 'H') { // Hessian 2.0 stream
            major = isToUse.read();
            minor = isToUse.read();
            if (major != 0x02) {
                throw new IOException("Version " + major + "." + minor + " is not understood");
            }
            in = new Hessian2Input(isToUse);
            out = new Hessian2Output(osToUse);
            in.readCall();
        } else if (code == 'C') { // Hessian 2.0 call... for some reason not handled in HessianServlet!
            isToUse.reset();
            in = new Hessian2Input(isToUse);
            out = new Hessian2Output(osToUse);
            in.readCall();
        } else if (code == 'c') { // Hessian 1.0 call
            major = isToUse.read();
            minor = isToUse.read();
            in = new HessianInput(isToUse);
            if (major >= 2) {
                out = new Hessian2Output(osToUse);
            } else {
                out = new HessianOutput(osToUse);
            }
        } else {
            throw new IOException("Expected 'H'/'C' (Hessian 2.0) or 'c' (Hessian 1.0) in hessian input at " + code);
        }

        if (serializerFactory != null) {
            in.setSerializerFactory(serializerFactory);
            out.setSerializerFactory(serializerFactory);
        }

        try {
            skeleton.invoke(in, out);
        } finally {
            try {
                in.close();
                isToUse.close();
            } catch (final IOException ex) {
                // ignore
            }
            try {
                out.close();
                osToUse.close();
            } catch (final IOException ex) {
                // ignore
            }
        }
    }
}
