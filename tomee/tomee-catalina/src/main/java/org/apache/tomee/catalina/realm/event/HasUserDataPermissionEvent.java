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

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

public class HasUserDataPermissionEvent {
    private final Request request;
    private final Response response;
    private final SecurityConstraint[] constraint;

    private boolean hasUserDataPermission;

    public HasUserDataPermissionEvent(final Request request, final Response response, final SecurityConstraint[] constraint) {
        this.request = request;
        this.response = response;
        this.constraint = constraint;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public SecurityConstraint[] getConstraint() {
        return constraint;
    }

    public boolean isHasUserDataPermission() {
        return hasUserDataPermission;
    }

    public void setHasUserDataPermission(boolean hasUserDataPermission) {
        this.hasUserDataPermission = hasUserDataPermission;
    }
}
