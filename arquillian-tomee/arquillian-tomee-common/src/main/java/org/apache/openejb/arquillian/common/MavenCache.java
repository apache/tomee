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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class MavenCache {
	public File getArtifact(String artifactInfo, String altUrl) {
        try {
            return new File(new Resolver().resolve(artifactInfo.startsWith("mvn")? "" : "mvn:" + artifactInfo));
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

	public File download(String source) throws DownloadException {
		File file = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			is = ProvisioningUtil.inputStreamTryingProxies(new URI(source));
			file = File.createTempFile("dload", ".fil");
			file.deleteOnExit();
			os = new FileOutputStream(file);
			
			int bytesRead;
			byte[] buffer = new byte[8192];
			
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

	public static void main(String[] args) {
		// File file = new MavenCache().getArtifact("org.apache.openejb:tomcat:zip:6.0.33", "http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.27/bin/apache-tomcat-7.0.27.zip").getFile();
		File file = new MavenCache().getArtifact("org.apache.openejb:apache-tomee:1.0.0-beta-2-SNAPSHOT:zip:plus", "http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.27/bin/apache-tomcat-7.0.27.zip");
		System.out.println(file.getAbsolutePath());
	}
}
