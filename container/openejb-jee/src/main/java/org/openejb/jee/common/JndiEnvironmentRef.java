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
package org.openejb.jee.common;

import org.openejb.jee.common.InjectionTarget;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class JndiEnvironmentRef {
    private String id;
    private List<String> description;
    private String mappedName;
    private List<InjectionTarget> injectionTargets;

    public JndiEnvironmentRef() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    public List<InjectionTarget> getInjectionTargets() {
        return injectionTargets;
    }

    public void setInjectionTargets(List<InjectionTarget> injectionTargets) {
        this.injectionTargets = injectionTargets;
    }
}
