/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugins;

public class LiveReload {
    private String watchedFolder;
    private String path = "/"; // then endpoint is bound to /livereload so to match default we need to set it to ROOT
    private int port = 35729;

    public String getWatchedFolder() {
        return watchedFolder;
    }

    public void setWatchedFolder(final String watchedFolder) {
        this.watchedFolder = watchedFolder;
    }

    public String getPath() {
        return path.startsWith("/") ? path : ('/' + path);
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
