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

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.LocationResolver;
import org.apache.openejb.resolver.maven.Handler;
import org.apache.openejb.resolver.maven.Parser;

import java.io.File;
import java.net.URL;

import static org.apache.openejb.loader.ProvisioningUtil.cacheFile;

public class Resolver implements LocationResolver {
    public static final String MVN_PREFIX = "mvn:";

    @Override
    public String resolve(final String rawLocation) throws Exception {
        if (rawLocation.startsWith(MVN_PREFIX) && rawLocation.length() > MVN_PREFIX.length()) {

            final String info = rawLocation.substring(MVN_PREFIX.length());
            final Parser parser = new Parser(info);
            final File file = cacheFile(parser.getArtifactPath());
            if (!file.exists() || !file.canRead()) {
                try {
                    final URL url = new URL(MVN_PREFIX.substring(MVN_PREFIX.length() - 1), "localhost", -1, info, new Handler());
                    final File parentFile = file.getParentFile();
                    if (!parentFile.exists()) {
                        if (!parentFile.mkdirs()) {
                            throw new Exception("Failed to create: " + parentFile);
                        }
                    }
                    IO.copy(IO.read(url), file);
                } catch (Exception e) {
                    if (file.exists()) {
                        if (!file.delete()) {
                            file.deleteOnExit();
                        }
                    }
                    throw e;
                }
            }
            return file.getPath();
        }
        return rawLocation;
    }
}
