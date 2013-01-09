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
package org.apache.openejb.util.classloader;

import javax.persistence.spi.ClassTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;

public interface EnhanceableClassLoader {
    void setTransformers(Collection<ClassTransformer> transformers, Collection<String> classes);

    public static final class Helper {
        private Helper() {
            // no-op
        }

        public static byte[] enhance(final Collection<ClassTransformer> transformers,
                                   final ClassLoader loader,
                                   final String className,
                                   final Class<?> classBeingRedefined,
                                   final ProtectionDomain protectionDomain,
                                   final byte[] classfileBuffer) throws IllegalClassFormatException {
            byte[] current = classfileBuffer;
            for (ClassTransformer transformer : transformers) {
                current = transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            }

            if (current != null) {
                return current;
            }
            return classfileBuffer;
        }
    }
}
