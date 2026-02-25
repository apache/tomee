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

package org.apache.openejb;

/**
 * @version $Rev$ $Date$
 */
public enum InterfaceType {
    EJB_HOME("Home"),
    EJB_OBJECT("Remote"),
    EJB_LOCAL_HOME("LocalHome"),
    EJB_LOCAL("Local"),
    BUSINESS_LOCAL("Local"),
    LOCALBEAN("LocalBean"),
    BUSINESS_LOCAL_HOME("LocalHome"),
    BUSINESS_REMOTE("Remote"),
    BUSINESS_REMOTE_HOME("Home"),
    SERVICE_ENDPOINT("ServiceEndpoint"),
    BUSINESS_LOCALBEAN_HOME("LocalBeanHome"),
    TIMEOUT("Timeout"),
    UNKNOWN("Unknown");

    private final String specName;

    InterfaceType(final String name) {
        this.specName = name;
    }

    public String getSpecName() {
        return specName;
    }

    public boolean isHome() {
        return switch (this) {
            case EJB_HOME -> true;
            case EJB_LOCAL_HOME -> true;
            case BUSINESS_LOCAL_HOME -> true;
            case BUSINESS_LOCALBEAN_HOME -> true;
            case BUSINESS_REMOTE_HOME -> true;
            default -> false;
        };
    }

    public boolean isComponent() {
        return switch (this) {
            case EJB_OBJECT -> true;
            case EJB_LOCAL -> true;
            default -> false;
        };
    }

    public boolean isBusiness() {
        return switch (this) {
            case BUSINESS_LOCAL -> true;
            case BUSINESS_REMOTE -> true;
            case LOCALBEAN -> true;
            default -> false;
        };
    }

    public boolean isRemote() {
        return switch (this) {
            case EJB_HOME -> true;
            case EJB_OBJECT -> true;
            case BUSINESS_REMOTE -> true;
            case BUSINESS_REMOTE_HOME -> true;
            default -> false;
        };
    }

    public boolean isLocal() {
        return switch (this) {
            case EJB_LOCAL_HOME -> true;
            case EJB_LOCAL -> true;
            case BUSINESS_LOCAL -> true;
            case BUSINESS_LOCAL_HOME -> true;
            case LOCALBEAN -> true;
            default -> false;
        };
    }

    public boolean isLocalBean() {
        return switch (this) {
            case LOCALBEAN -> true;
            case BUSINESS_LOCALBEAN_HOME -> true;
            default -> false;
        };

    }

    public InterfaceType getCounterpart() {
        return switch (this) {
            case EJB_HOME -> InterfaceType.EJB_OBJECT;
            case EJB_LOCAL_HOME -> InterfaceType.EJB_LOCAL;
            case BUSINESS_REMOTE_HOME -> InterfaceType.BUSINESS_REMOTE;
            case BUSINESS_LOCAL_HOME -> InterfaceType.BUSINESS_LOCAL;
            case EJB_OBJECT -> InterfaceType.EJB_HOME;
            case EJB_LOCAL -> InterfaceType.EJB_LOCAL_HOME;
            case BUSINESS_REMOTE -> InterfaceType.BUSINESS_REMOTE_HOME;
            case BUSINESS_LOCAL -> InterfaceType.BUSINESS_LOCAL_HOME;
            case BUSINESS_LOCALBEAN_HOME -> InterfaceType.LOCALBEAN;
            case LOCALBEAN -> InterfaceType.BUSINESS_LOCALBEAN_HOME;
            default -> throw new IllegalArgumentException("InterfaceType has no counterpart: " + this);
        };
    }
}
