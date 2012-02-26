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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jaxbagent;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @version $Rev$ $Date$
 */
public class Agent {
    private static String agentArgs;
    private static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (Agent.instrumentation != null) return;

        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
        final File output = new File(agentArgs);
        instrumentation.addTransformer(new WriteClassesTransformer(output));
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        if (Agent.instrumentation != null) return;

        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
    }

    private static class WriteClassesTransformer implements ClassFileTransformer {

        private final File output;

        public WriteClassesTransformer(File output) {
            this.output = output;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

            if (!className.contains("$JaxbAccessor")) return classfileBuffer;

            try {
                final File file = new File(output, className.replace('.', File.separatorChar) + ".class");
                file.getParentFile().mkdirs();
                final OutputStream write = new BufferedOutputStream(new FileOutputStream(file), classfileBuffer.length);
                try {
                    write.write(classfileBuffer);
                    write.flush();
                } finally {
                    close(write);
                }
            } catch (IOException e) {
            }
            return classfileBuffer;
        }

        public void close(Closeable closeable) {
            if (closeable == null) return;
            try {
                if (closeable instanceof Flushable) {
                    ((Flushable) closeable).flush();
                }
            } catch (IOException e) {
            }
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }

    }
}
