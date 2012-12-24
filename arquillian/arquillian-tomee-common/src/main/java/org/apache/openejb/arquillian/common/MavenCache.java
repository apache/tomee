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
package org.apache.openejb.arquillian.common;

import org.apache.openejb.loader.*;
import org.apache.openejb.resolver.Resolver;

import java.io.*;
import java.net.URI;
import java.util.logging.Logger;

public class MavenCache {
    private static final Logger LOGGER = Logger.getLogger(MavenCache.class.getName());

    public static File getArtifact(final String artifactInfo, final String altUrl) {
        LOGGER.info("Downloading " + artifactInfo + " please wait...");

        try {
            return new File(new Resolver().resolve(artifactInfo.startsWith("mvn") ? "" : "mvn:" + artifactInfo));
        } catch (Exception e) {
            // ignored
        }
        try {
            if (altUrl != null) {
                return download(altUrl);
                // don't cache the fallback
                // only main artifact should be cached
            }
        } catch (Exception e1) {
            throw new IllegalStateException(e1);
        }

        return null;
    }

    public static File download(final String source) throws DownloadException {
        File file = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            is = ProvisioningUtil.inputStreamTryingProxies(new URI(source));
            try {
                file = File.createTempFile("dload", ".fil");
            } catch (Throwable e) {
                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }
                file = File.createTempFile("dload", ".fil", tmp);
            }
            file.deleteOnExit();
            os = new FileOutputStream(file);

            int bytesRead;
            final byte[] buffer = new byte[8192];

            while ((bytesRead = is.read(buffer)) > -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new DownloadException("Unable to download " + source + " to " + file.getAbsolutePath(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // no-op
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    // no-op
                }
            }
        }

        return file;
    }
}
