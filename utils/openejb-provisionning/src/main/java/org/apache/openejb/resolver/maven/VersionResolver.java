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
package org.apache.openejb.resolver.maven;

import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.version.Version;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

public final class VersionResolver {
    private VersionResolver() {
        // no-op
    }

    public static VersionRangeResult versions(final String info, final String defaultVersion) {
        final MavenConfigurationImpl config = ConfigHelper.createConfig();
        try {
            final Parser parser = new Parser(info);
            final AetherBasedResolver resolver = new AetherBasedResolver(config, parser.getRepositoryURL());
            return resolver.resolveVersions(parser.getGroup(), parser.getArtifact(), parser.getClassifier(), parser.getType(), parser.getVersion());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String highestVersion(final String info, final String prefix, final String defaultVersion) {
        final VersionRangeResult result = VersionResolver.versions(info, defaultVersion);
        if (result == null) {
            return defaultVersion;
        }
        final List<Version> versions = result.getVersions();
        Collections.sort(versions); // Version impl comparable so we just need to call it :)
        Version usedVersion = null;
        for (Version current : versions) {
            if (current.toString().startsWith(prefix)) {
                usedVersion = current;
            }
        }
        if (usedVersion != null) {
            return usedVersion.toString();
        }
        return defaultVersion;
    }
}
