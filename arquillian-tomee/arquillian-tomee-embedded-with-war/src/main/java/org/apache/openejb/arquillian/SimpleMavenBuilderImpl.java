package org.apache.openejb.arquillian;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolutionFilter;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenBuilderImpl;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenConverter;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenRepositorySettings;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenRepositorySystem;
import org.jboss.shrinkwrap.resolver.impl.maven.Validate;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResult;

public class SimpleMavenBuilderImpl extends MavenBuilderImpl {

	private static final File[] FILE_CAST = new File[0];
	private final MavenRepositorySystem system;
	private final RepositorySystemSession session;

	public SimpleMavenBuilderImpl() {
		super();
		this.system = new MavenRepositorySystem(new MavenRepositorySettings());
		this.session = system.getSession();
	}

	public File[] resolveAsFiles(MavenResolutionFilter filter) throws ResolutionException {
		Validate.notEmpty(super.getDependencies(), "No dependencies were set for resolution");

		// configure filter
		filter.configure(Collections.unmodifiableList(super.getDependencies()));

		Collection<File> files = new ArrayList<File>(super.getDependencies().size());

		List<Dependency> dependencies = MavenConverter.asDependencies(super.getDependencies());
		for (Dependency dependency : dependencies) {
			try {
				ArtifactRequest artifactRequest = new ArtifactRequest(dependency.getArtifact(), system.getRemoteRepositories(), null);
				ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
				Artifact artifact = artifactResult.getArtifact();
				files.add(artifact.getFile());
			} catch (Exception e) {
				throw new ResolutionException("Unable to resolve an artifact", e);
			}
		}

		return files.toArray(FILE_CAST);
	}
}
