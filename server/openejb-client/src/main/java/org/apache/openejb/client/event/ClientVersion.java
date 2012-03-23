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
package org.apache.openejb.client.event;

import org.apache.openejb.client.ResourceFinder;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@Log(Log.Level.CONFIG)
public class ClientVersion {

    private final String version;
    private final String date;
    private final String time;

    public ClientVersion(String version, String date, String time) {
        this.version = version;
        this.date = date;
        this.time = time;
    }

    public ClientVersion() {
        Properties info = new Properties();

        try {
            ResourceFinder finder = new ResourceFinder();
            info = finder.findProperties("openejb-client-version.properties");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        version = info.getProperty("version");
        date = info.getProperty("date");
        time = info.getProperty("time");
    }

    public String getVersion() {
        return version;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "ClientVersion{" +
                "version='" + version + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
