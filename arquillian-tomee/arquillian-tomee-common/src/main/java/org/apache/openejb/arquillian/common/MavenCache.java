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

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenDependencyResolverSettings;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.ArtifactProperties;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * This class resolves artifacts in Maven. If an artifact (such as the Tomcat
 * zip) isn't available in Maven, this class can obtain it from an alternative
 * URL and store it in the local repository.
 * 
 * 
 */
public class MavenCache {

	private final MavenDependencyResolverSettings settings;
	private final RepositorySystem system;
	private final RepositorySystemSession session;

	public MavenCache() {
		this.settings = new MavenDependencyResolverSettings();
		this.system = getRepositorySystem();
		this.session = getSession();
	}

	public RepositorySystemSession getSession() {
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		LocalRepository localRepository = new LocalRepository(settings.getSettings().getLocalRepository());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepository));

		return session;
	}

	private RepositorySystem getRepositorySystem() {
		try {
			return new DefaultPlexusContainer().lookup(RepositorySystem.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to lookup component RepositorySystem, cannot establish Aether dependency resolver.",	e);
		} catch (PlexusContainerException e) {
			throw new RuntimeException("Unable to load RepositorySystem component by Plexus, cannot establish Aether dependency resolver.",	e);
		}
	}

	public Artifact getArtifact(String coords, String altUrl) {
		return getArtifact(getArtifact(coords), altUrl);
	}

	public Artifact getArtifact(Artifact art, String altUrl) {
		Artifact artifact = null;
		
		try {
			artifact = resolve(art);
		} catch (Exception e) {
			// so lets try and download and install it instead
			try {
				if (altUrl != null) {
					File file = download(altUrl);
					
					InstallRequest request = new InstallRequest();
					artifact = art.setFile(file);
					request.addArtifact(artifact);
					system.install(session, request);
					artifact = resolve(art);
				}
			} catch (InstallationException e1) {
				e1.printStackTrace();
			} catch (DownloadException e1) {
				e1.printStackTrace();
			} catch (ArtifactResolutionException e1) {
				e1.printStackTrace();
			}
		}
		
		return artifact;
	}

	public Artifact getArtifact(final String coords) {
        final Artifact artifact = new DefaultArtifact(coords); // just for the parsing
		return new DefaultArtifact(coords, new HashMap<String, String>() {{ // try to get faster
            put(ArtifactProperties.LOCAL_PATH, new File(session.getLocalRepository().getBasedir(), session.getLocalRepositoryManager().getPathForLocalArtifact(artifact)).getAbsolutePath());
        }});
	}

	public Artifact resolve(Artifact artifact) throws ArtifactResolutionException {
        ArtifactRequest artifactRequest = new ArtifactRequest(artifact, settings.getRemoteRepositories(), null);
		ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
		return artifactResult.getArtifact();
	}
	
	public File download(String source) throws DownloadException {
		File file = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new URL(source).openStream();
			file = File.createTempFile("dload", ".fil");
			file.deleteOnExit();
			os = new FileOutputStream(file);
			
			int bytesRead = -1;
			byte[] buffer = new byte[8192];
			
			while ((bytesRead = is.read(buffer)) > -1) {
				os.write(buffer, 0, bytesRead);
			}
			
			is.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DownloadException("Unable to download " + source + " to " + file.getAbsolutePath());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}

			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
		}
		
		return file;
	}

	public static void main(String[] args) {
		// File file = new MavenCache().getArtifact("org.apache.openejb:tomcat:zip:6.0.33", "http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.21/bin/apache-tomcat-7.0.21.zip").getFile();
		File file = new MavenCache().getArtifact("org.apache.openejb:apache-tomee:zip:plus:1.0.0-beta-2-SNAPSHOT", "http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.21/bin/apache-tomcat-7.0.21.zip").getFile();
		System.out.println(file.getAbsolutePath());
	}
}
