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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.itest.failover;

import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.apache.openejb.util.Join;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public final class Repository {
    private static final ProvisioningResolver RESOLVER = new ProvisioningResolver();

    public static File getArtifact(final String groupId, final String artifactId, final String type) {
        final String oldCache = System.getProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER);
        final String property = System.getProperty("openejb.itest.failover.cache", "target/cache");
        new File(property).mkdirs(); // ensure cache folder exists otherwise copy will fail
        System.setProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER, property);
        final String path;
        try {
            path = RESOLVER.realLocation("mvn:" + groupId + ":" + artifactId + ":" + guessVersion(groupId, artifactId) + ":" + type).iterator().next();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (oldCache == null) {
                System.clearProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER);
            } else {
                System.setProperty(ProvisioningResolver.OPENEJB_DEPLOYER_CACHE_FOLDER, oldCache);
            }
        }

        return new File(path);
    }

    public static String guessVersion(final String groupId, final String artifactId) {
        final String[] keys = {artifactId + ".version", groupId + ".version", "version"};
        for (final String key : keys) {
            final String value = System.getProperty(key);
            if (value != null) {
                return value;
            }
        }

        final String message = String.format("Cannot find version for %s:%s. Checked the following system properties: %s", groupId, artifactId, Join.join(", ", keys));
        throw new IllegalStateException(message);
    }
}
