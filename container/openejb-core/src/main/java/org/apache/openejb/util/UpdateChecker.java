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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.openejb.assembler.classic.event.AssemblerCreated;
import org.apache.openejb.assembler.classic.event.ConfigurationLoaded;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.ObserverAdded;

public class UpdateChecker {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP, UpdateChecker.class);

    private static final String REPO_URL = SystemInstance.get().getOptions().get("openejb.version.check.repo.url", "http://repo1.maven.org/maven2/");
    private static final String OPENEJB_GROUPID = "org/apache/openejb/";
    private static final String METADATA = "/maven-metadata.xml";
    private static String CHECKER_PROXY = SystemInstance.get().getOptions().get("openejb.version.check.proxy", (String) null);
    private static final String AUTO = "auto";
    private static final String URL = SystemInstance.get().getOptions().get("openejb.version.check.url", AUTO);
    private static final String TAG = "latest";
    private static final String UNDEFINED = "undefined";
    private static String LATEST = "undefined";

    public void check(@Observes ObserverAdded event) {
        if (event.getObserver() != this) {
            return;
        }

        String originalProxyHost = null;
        String originalProxyPort = null;
        String originalProxyUser = null;
        String originalProxyPwd = null;
        String proxyProtocol = null;


        if (CHECKER_PROXY != null) {
            try {
                final URL proxyUrl = new URL(CHECKER_PROXY);
                proxyProtocol = proxyUrl.getProtocol();

                originalProxyHost = System.getProperty(proxyProtocol + ".proxyHost");
                originalProxyPort = System.getProperty(proxyProtocol + ".proxyPort");
                originalProxyUser = System.getProperty(proxyProtocol + ".proxyUser");
                originalProxyPwd = System.getProperty(proxyProtocol + ".proxyPassword");

                System.setProperty(proxyProtocol + ".proxyHost", proxyUrl.getHost());
                System.setProperty(proxyProtocol + ".proxyPort", Integer.toString(proxyUrl.getPort()));

                final String userInfo = proxyUrl.getUserInfo();
                if (userInfo != null) {
                    int sep = userInfo.indexOf(":");
                    if (sep >= 0) {
                        System.setProperty(proxyProtocol + ".proxyUser", userInfo.substring(0, sep));
                        System.setProperty(proxyProtocol + ".proxyPassword", userInfo.substring(sep + 1));
                    } else {
                        System.setProperty(proxyProtocol + ".proxyUser", userInfo);
                    }
                }
            } catch (MalformedURLException e) {
                CHECKER_PROXY = null;
            }
        }

        String realUrl = URL;
        if ("auto".equals(realUrl)) {
            realUrl = REPO_URL + OPENEJB_GROUPID + artifact() + METADATA;
        }

        try {
            final URL url = new URL(realUrl);
            final String metaData = IO.readFileAsString(url.toURI());
            LATEST = extractLatest(metaData);
            if (!usesLatest()) {
                LOGGER.warning(message());
            }
        } catch (Exception e) {
            LOGGER.warning("can't check the version: " + e.getMessage()); // don't be too verbose here
        } finally {
            if (proxyProtocol != null) {
                resetSystemProp(proxyProtocol + ".proxyHost", originalProxyHost);
                resetSystemProp(proxyProtocol + ".proxyPort", originalProxyPort);
                resetSystemProp(proxyProtocol + ".proxyUser", originalProxyUser);
                resetSystemProp(proxyProtocol + ".proxyPassword", originalProxyPwd);
            }
        }
    }

    private static String artifact() {
        try {
            UpdateChecker.class.getClassLoader().loadClass("org.apache.tomee.catalina.TomcatWebAppBuilder");
            return "apache-tomee";
        } catch (ClassNotFoundException e) {
            return "openejb";
        }
    }

    private static void resetSystemProp(final String key, final String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
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
}
