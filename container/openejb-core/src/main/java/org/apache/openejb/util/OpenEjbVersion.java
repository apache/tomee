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

import org.apache.xbean.finder.ResourceFinder;

import java.net.URL;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbVersion {

    private final String copyright;//Copyright 1999-2006 (C) Apache OpenEJB Project, All Rights Reserved.
    private final String url; //http://incubator.apache.org/openejb
    private final String version;//${pom.version}
    private final String date;//@DATE-REPLACED-BY-MAVEN@
    private final String time;//@TIME-REPLACED-BY-MAVEN@
    private static OpenEjbVersion openEjbVersion;

    private OpenEjbVersion() {
        Properties info = new Properties();

        try {
            ResourceFinder finder = new ResourceFinder();
            info = finder.findProperties("openejb-version.properties");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        copyright = info.getProperty("copyright");
        url = info.getProperty("url");
        version = info.getProperty("version");
        date = info.getProperty("date");
        time = info.getProperty("time");
    }

    public static OpenEjbVersion get() {
        if (openEjbVersion == null) openEjbVersion = new OpenEjbVersion();
        return openEjbVersion;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }
}
