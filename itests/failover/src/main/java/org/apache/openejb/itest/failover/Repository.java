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

import org.apache.openejb.loader.ProvisioningUtil;
import org.apache.openejb.resolver.Resolver;
import org.apache.openejb.util.Join;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public final class Repository {
    private static final Resolver RESOLVER = new Resolver();

    public static File getArtifact(final String groupId, final String artifactId, final String type) {
        final String oldCache = System.getProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER);
        System.setProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, System.getProperty("openejb.itest.failover.cache", "target/cache"));
        final String path;
        try {
            path = RESOLVER.resolve("mvn:" + groupId + ":" + artifactId + ":" + guessVersion(groupId, artifactId) + ":" + type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (oldCache == null) {
                System.clearProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER);
            } else {
                System.setProperty(ProvisioningUtil.OPENEJB_DEPLOYER_CACHE_FOLDER, oldCache);
            }
        }

        return new File(path);
    }

    private static String guessVersion(final String groupId, final String artifactId) {
        String[] keys = { artifactId + ".version", groupId + ".version", "version" };
        for (String key : keys) {
            final String value = System.getProperty(key);
            if (value != null) {
                return value;
            }
        }

        String message = String.format("Cannot find version for %s:%s. Checked the following system properties: %s", groupId, artifactId, Join.join(", ", keys));
        throw new IllegalStateException(message);
    }
}
