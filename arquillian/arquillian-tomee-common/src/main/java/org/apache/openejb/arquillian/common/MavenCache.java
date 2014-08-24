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
package org.apache.openejb.arquillian.common;

import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.provisining.HttpResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class MavenCache {
    private static final Logger LOGGER = Logger.getLogger(MavenCache.class.getName());

    public static File getArtifact(final String artifactInfo, final String altUrl) {
        LOGGER.info("Downloading " + artifactInfo + " please wait...");

        // initializing the SystemInstance because we'll need it for configuration
        try {
            if (!SystemInstance.isInitialized()) {
                SystemInstance.init(new Properties());
            }
        } catch (final Exception e) {
            // no-op
        }

        try {
            return new File(ProvisioningUtil.realLocation(artifactInfo.startsWith("mvn") ? "" : "mvn:" + artifactInfo).iterator().next());
        } catch (final Exception e) {
            // ignored
        }
        try {
            if (altUrl != null) {
                return download(altUrl);
                // don't cache the fallback
                // only main artifact should be cached
            }
        } catch (final Exception e1) {
            throw new IllegalStateException(e1);
        }

        return null;
    }

    public static File download(final String source) throws DownloadException {
        File file = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new HttpResolver().resolve(source);
            try {
                file = File.createTempFile("dload", ".fil");
            } catch (final Throwable e) {
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
        } catch (final Exception e) {
            throw new DownloadException("Unable to download " + source + " to "
                    + (file == null ? "null" : file.getAbsolutePath()), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final Exception e) {
                    // no-op
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (final Exception e) {
                    // no-op
                }
            }
        }

        return file;
    }
}
