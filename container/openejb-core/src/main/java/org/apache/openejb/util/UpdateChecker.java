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

package org.apache.openejb.util;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.ObserverAdded;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class UpdateChecker {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP, UpdateChecker.class);

    private static final String TOMEE_ARTIFACT = "apache-tomee";

    // config
    private String repoUrl = SystemInstance.get().getOptions().get("openejb.version.check.repo.url", "http://repo1.maven.org/maven2/");
    private String groupId = "org/apache/openejb/";
    private String metadata = "/maven-metadata.xml";
    private String checkerProxy = SystemInstance.get().getOptions().get("openejb.version.check.proxy", (String) null);
    private String auto = "auto";
    private String url = SystemInstance.get().getOptions().get("openejb.version.check.url", auto);
    private String tag = "release";
    private String undefined = "undefined";
    private String latest = "undefined";

    // internal
    private String current;

    public void check(@Observes final ObserverAdded event) {
        if (event.getObserver() != this) {
            return;
        }

        String originalProxyHost = null;
        String originalProxyPort = null;
        String originalProxyUser = null;
        String originalProxyPwd = null;
        String proxyProtocol = null;


        if (checkerProxy != null) {
            try {
                final URL proxyUrl = new URL(checkerProxy);
                proxyProtocol = proxyUrl.getProtocol();

                originalProxyHost = JavaSecurityManagers.getSystemProperty(proxyProtocol + ".proxyHost");
                originalProxyPort = JavaSecurityManagers.getSystemProperty(proxyProtocol + ".proxyPort");
                originalProxyUser = JavaSecurityManagers.getSystemProperty(proxyProtocol + ".proxyUser");
                originalProxyPwd = JavaSecurityManagers.getSystemProperty(proxyProtocol + ".proxyPassword");

                JavaSecurityManagers.setSystemProperty(proxyProtocol + ".proxyHost", proxyUrl.getHost());
                JavaSecurityManagers.setSystemProperty(proxyProtocol + ".proxyPort", Integer.toString(proxyUrl.getPort()));

                final String userInfo = proxyUrl.getUserInfo();
                if (userInfo != null) {
                    final int sep = userInfo.indexOf(':');
                    if (sep >= 0) {
                        JavaSecurityManagers.setSystemProperty(proxyProtocol + ".proxyUser", userInfo.substring(0, sep));
                        JavaSecurityManagers.setSystemProperty(proxyProtocol + ".proxyPassword", userInfo.substring(sep + 1));
                    } else {
                        JavaSecurityManagers.setSystemProperty(proxyProtocol + ".proxyUser", userInfo);
                    }
                }
            } catch (final MalformedURLException e) {
                checkerProxy = null;
            }
        }

        String realUrl = url;
        if ("auto".equals(realUrl)) {
            realUrl = repoUrl + groupId + artifact() + metadata;
        }

        try {
            final URL url = new URL(realUrl);
            final String metaData = IO.slurp(url);
            latest = extractLatest(metaData);
            if (!usesLatest()) {
                LOGGER.warning(message());
            }
        } catch (final Exception e) {
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
            return TOMEE_ARTIFACT;
        } catch (final ClassNotFoundException e) {
            return "openejb";
        }
    }

    private static void resetSystemProp(final String key, final String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            JavaSecurityManagers.setSystemProperty(key, value);
        }
    }

    private String extractLatest(final String metaData) {
        if (metaData != null) {
            boolean found = false;
            for (final String s : metaData.replace(">", ">\n").split("\n")) {
                if (found) {
                    return trim(s).replace("</" + tag + ">", "");
                }
                if (!s.isEmpty() && trim(s).endsWith("<" + tag + ">")) {
                    found = true;
                }
            }
        }
        return undefined;
    }

    private String trim(final String s) {
        return s.replace("\t", "").replace(" ", "");
    }

    public boolean usesLatest() {
        if (artifact().contains(TOMEE_ARTIFACT)) {
            try (final InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/maven/org.apache.openejb/tomee-catalina/pom.properties")) {
                if (is != null) {
                    final Properties prop = new Properties();
                    prop.load(is);
                    current = prop.getProperty("version");
                }
            } catch (IOException e) {
                LOGGER.error("can't get tomee version, will use openejb one");
            }
        }

        if (current == null) {
            current = OpenEjbVersion.get().getVersion();
        }


        return current.equals(latest);
    }

    public String message() {
        if (undefined.equals(latest)) {
            return "can't determine the latest version";
        }

        if (current.equals(latest)) {
            return "running on the latest version";
        }
        return "you are using the version " + current +
                ", our latest stable version " + latest +
                " is available on " + repoUrl;
    }

    public void setRepoUrl(final String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public void setCheckerProxy(final String checkerProxy) {
        this.checkerProxy = checkerProxy;
    }

    public void setAuto(final String auto) {
        this.auto = auto;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public void setUndefined(final String undefined) {
        this.undefined = undefined;
    }

    public void setLatest(final String latest) {
        this.latest = latest;
    }
}
