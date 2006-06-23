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

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractEjbRef extends JndiEnvironmentRef {
    private String ejbRefName;
    private EjbRefType ejbRefType;
    private String ejbLink;

    public String getEjbRefName() {
        return ejbRefName;
    }

    public void setEjbRefName(String ejbRefName) {
        this.ejbRefName = ejbRefName;
    }

    public EjbRefType getEjbRefType() {
        return ejbRefType;
    }

    public void setEjbRefType(EjbRefType ejbRefType) {
        this.ejbRefType = ejbRefType;
    }

    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbLink(String ejbLink) {
        this.ejbLink = ejbLink;
    }
}
