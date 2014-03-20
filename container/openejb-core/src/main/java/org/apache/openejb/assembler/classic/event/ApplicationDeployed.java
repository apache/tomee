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
package org.apache.openejb.assembler.classic.event;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.observer.Event;

import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
@Event
public class ApplicationDeployed implements Serializable {

    private final AppInfo appInfo;
    private final String s;

    public ApplicationDeployed(final AppInfo appInfo) {
        this.appInfo = appInfo;
        this.s = ("ApplicationDeployed{appId=" + appInfo.appId + ",path=" + appInfo.path + "}");
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    @Override
    public String toString() {
        return s;
    }
}
