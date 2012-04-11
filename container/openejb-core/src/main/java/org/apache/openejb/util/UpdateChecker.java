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
package org.apache.openejb.util;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;

import java.net.URL;

public class UpdateChecker implements Runnable {
    private static final String SKIP_CHECK = "openejb.version.check";
    private static final String REPO_URL = SystemInstance.get().getOptions().get("openejb.version.check.repo.url", "http://repo1.maven.org/maven2/org/apache/openejb/");
    private static final String URL = SystemInstance.get().getOptions().get("openejb.version.check.url", REPO_URL + "openejb/maven-metadata.xml");
    private static final String TAG = "latest";
    private static final String UNDEFINED = "undefined";
    private static String LATEST = "undefined";

    @Override
    public void run() {
        if (isSkipped()) {
            return;
        }

        try {
            final URL url = new URL(URL);
            final String metaData = IO.readFileAsString(url.toURI());
            LATEST = extractLatest(metaData);
        } catch (Exception e) {
            // ignored
        }
    }

    private static String extractLatest(final String metaData) {
        if (metaData != null) {
            boolean found = false;
            for (String s : metaData.replace(">", ">\n").split("\n")) {
                if (found) {
                    return trim(s).replace("</" + TAG + ">", "");
                }
                if (!s.isEmpty() && trim(s).endsWith("<" + TAG + ">")) {
                    found = true;
                }
            }
        }
        return UNDEFINED;
    }

    private static String trim(final String s) {
        return s.replace("\t", "").replace(" ", "");
    }

    public static boolean usesLatest() {
        return OpenEjbVersion.get().getVersion().equals(LATEST);
    }

    public static String message() {
        if (isSkipped()) {
            return "version checking is skipped";
        }

        if (UNDEFINED.equals(LATEST)) {
            return "can't determine the latest version";
        }

        final String version = OpenEjbVersion.get().getVersion();
        if (version.equals(LATEST)) {
            return "running on the latest version";
        }
        return new StringBuilder("you are using the version ").append(version)
                .append(", our latest stable version ").append(LATEST)
                .append(" is available on ").append(REPO_URL).toString();
    }

    public static boolean isSkipped() {
        return System.getProperty(SKIP_CHECK) == null;
    }

    public static void main(String[] args) {
        UpdateChecker checker = new UpdateChecker();
        checker.run();
        System.out.println(UpdateChecker.message());
    }
}
