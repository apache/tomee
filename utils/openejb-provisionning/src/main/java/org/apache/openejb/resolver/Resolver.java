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

import org.apache.openejb.assembler.LocationResolver;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resolver.http.Helper;
import org.apache.openejb.resolver.maven.Handler;
import org.apache.openejb.resolver.maven.Parser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;

public class Resolver implements LocationResolver {
    public static final String MVN_PREFIX = "mvn:";
    public static final String OPENEJB_DEPLOYER_CACHE_FOLDER = "openejb.deployer.cache.folder";
    public static final String HTTP_PREFIX = "http";

    public String resolve(final String rawLocation) throws Exception {
        final String cache = System.getProperty(OPENEJB_DEPLOYER_CACHE_FOLDER, "temp");
        if (rawLocation.startsWith(MVN_PREFIX) && rawLocation.length() > MVN_PREFIX.length()) {

            final String info = rawLocation.substring(MVN_PREFIX.length());
            final Parser parser = new Parser(info);
            final File file = new File(SystemInstance.get().getBase().getDirectory(),
                    cache + File.separator + parser.getArtifactPath());
            if (!file.exists()) {
                try {
                    final URL url = new URL(MVN_PREFIX.substring(MVN_PREFIX.length() - 1), "localhost", -1, info, new Handler());
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileUtils.copy(new FileOutputStream(file), url.openStream());
                } catch (Exception e) {
                    if (file.exists()) {
                        file.delete();
                    }
                    throw e;
                }
            }
            return file.getPath();
        } else if (rawLocation.startsWith(HTTP_PREFIX)) {
            final File file = new File(SystemInstance.get().getBase().getDirectory(),
                    cache + File.separator + lastPart(rawLocation));
            final URI uri = new URI(rawLocation);
            Helper.copyTryingProxies(uri, file);
            return file.getAbsolutePath();
        }
        return rawLocation;
    }

    /**
     * location looks like xxxx/X/x/X/Y.
     * we suppose location doesn't end with a /
     *
     * @param location input
     * @return Y
     */
    private String lastPart(final String location) {
        final int idx = location.lastIndexOf('/');
        if (idx <= 0) {
            return location;
        }
        return location.substring(idx + 1, location.length());
    }
}
