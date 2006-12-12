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
package org.apache.openejb;

/**
 * @version $Rev$ $Date$
 */
public enum InterfaceType {
    EJB_HOME,
    EJB_OBJECT,
    EJB_LOCAL_HOME,
    EJB_LOCAL,
    BUSINESS_LOCAL,
    BUSINESS_LOCAL_HOME,
    BUSINESS_REMOTE,
    BUSINESS_REMOTE_HOME,
    SERVICE_ENDPOINT,
    UNKNOWN;

    public boolean isHome() {
        switch(this){
            case EJB_HOME: return true;
            case EJB_LOCAL_HOME: return true;
        }
        return false;
    }

    public boolean isComponent() {
        switch(this){
            case EJB_OBJECT: return true;
            case EJB_LOCAL: return true;
        }
        return false;
    }

    public boolean isBusiness() {
        switch(this){
            case BUSINESS_LOCAL: return true;
            case BUSINESS_REMOTE: return true;
        }
        return false;
    }

    public boolean isRemote() {
        switch(this){
            case EJB_HOME: return true;
            case EJB_OBJECT: return true;
            case BUSINESS_REMOTE: return true;
            case BUSINESS_REMOTE_HOME: return true;
        }
        return false;
    }

    public boolean isLocal() {
        switch(this){
            case EJB_LOCAL_HOME: return true;
            case EJB_LOCAL: return true;
            case BUSINESS_LOCAL: return true;
            case BUSINESS_LOCAL_HOME: return true;
        }
        return false;
    }
}
