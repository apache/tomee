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
package org.apache.tomee.catalina.realm.event;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

import java.util.List;

public class FindSecurityConstraintsEvent {

    private final Request request;
    private final Context context;
    private List<SecurityConstraint> securityConstraints;

    public FindSecurityConstraintsEvent(final Request request, final Context context) {
        this.request = request;
        this.context = context;
    }

    public Request getRequest() {
        return request;
    }

    public Context getContext() {
        return context;
    }

    public boolean addSecurityConstraint(final SecurityConstraint constraint) {
        return securityConstraints.add(constraint);
    }

    public SecurityConstraint[] getSecurityConstraints() {
        return securityConstraints.toArray(new SecurityConstraint[securityConstraints.size()]);
    }

}
