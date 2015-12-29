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
package org.apache.openejb.resolver;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.provisining.MavenResolver;
import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.apache.openejb.resolver.maven.ShrinkwrapBridge;

import java.io.FileInputStream;
import java.io.InputStream;

public class Resolver extends MavenResolver {
    public InputStream resolve(final String rawLocation) {
        final boolean initialized = SystemInstance.isInitialized();
        if (!initialized) {
            SystemInstance.get().setComponent(ProvisioningResolver.class, new ProvisioningResolver());
        }

        try {
            if (rawLocation.startsWith("mvn:") && rawLocation.length() > "mvn:".length()) {
                try {
                    return new FileInputStream(new ShrinkwrapBridge().resolve(rawLocation));
                } catch (final Throwable th) {
                    // try aether if not in a mvn build
                    th.printStackTrace();
                }
            }
            return super.resolve(rawLocation);
        } finally {
            if (!initialized) {
                SystemInstance.reset();
            }
        }
    }
}
