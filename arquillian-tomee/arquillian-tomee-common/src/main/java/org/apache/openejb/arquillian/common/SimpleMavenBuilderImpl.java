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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.arquillian.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolutionFilter;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenBuilderImpl;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenConverter;
import org.jboss.shrinkwrap.resolver.impl.maven.Validate;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;

public class SimpleMavenBuilderImpl extends MavenBuilderImpl {

	private static final File[] FILE_CAST = new File[0];
	private String altUrl = null;

	public SimpleMavenBuilderImpl() {
		super();
	}

	public File[] resolveAsFiles(MavenResolutionFilter filter) throws ResolutionException {
		Validate.notEmpty(super.getDependencies(), "No dependencies were set for resolution");

		// configure filter
		filter.configure(Collections.unmodifiableList(super.getDependencies()));

		Collection<File> files = new ArrayList<File>(super.getDependencies().size());

		List<Dependency> dependencies = MavenConverter.asDependencies(super.getDependencies());
		for (Dependency dependency : dependencies) {
			try {
				Artifact artifact = new MavenCache().getArtifact(dependency.getArtifact(), altUrl);
				files.add(artifact.getFile());
			} catch (Exception e) {
				throw new ResolutionException("Unable to resolve an artifact", e);
			}
		}

		return files.toArray(FILE_CAST);
	}

	public MavenBuilderImpl alternativeUrl(String altUrl) {
		this.altUrl = altUrl;
		return this;
	}
}
