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
package org.apache.tomee.livereload;

import org.apache.johnzon.mapper.JohnzonIgnore;

import java.util.Collection;
import java.util.Map;

public class Command {
    private String command;

    // hello
    private String serverName;
    private Collection<String> protocols;

    // update client-server
    private String url;

    // update server-client
    private String path;
    private Boolean liveCss;

    // info
    private Map<String, Object> plugins;

    // alert: not used
    // String message


    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public Map<String, Object> getPlugins() {
        return plugins;
    }

    public void setPlugins(final Map<String, Object> plugins) {
        this.plugins = plugins;
    }

    public Collection<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(final Collection<String> protocols) {
        this.protocols = protocols;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Boolean getLiveCss() {
        return liveCss;
    }

    public void setLiveCss(final Boolean liveCss) {
        this.liveCss = liveCss;
    }

    @JohnzonIgnore
    public boolean isHello() {
        return "hello".equals(command);
    }

    @JohnzonIgnore
    public boolean isClientUpdate() {
        return "url".equals(command);
    }

    @JohnzonIgnore
    public boolean isInfo() {
        return "info".equals(command);
    }
}
