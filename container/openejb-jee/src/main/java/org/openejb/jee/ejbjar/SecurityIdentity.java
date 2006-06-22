/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import org.openejb.jee.javaee.RunAs;

/**
 * @version $Revision$ $Date$
 */
public class SecurityIdentity {
    private String id;
    private boolean useCallerIdentity;
    private RunAs runAs;

    public SecurityIdentity() {
    }

    public SecurityIdentity(boolean useCallerIdentity) {
        this.useCallerIdentity = useCallerIdentity;
    }

    public SecurityIdentity(RunAs runAs) {
        this.runAs = runAs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUseCallerIdentity() {
        return useCallerIdentity;
    }

    public void setUseCallerIdentity(boolean useCallerIdentity) {
        this.useCallerIdentity = useCallerIdentity;
    }

    public RunAs getRunAs() {
        return runAs;
    }

    public void setRunAs(RunAs runAs) {
        this.runAs = runAs;
    }
}
