/**
 *
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
package org.apache.openejb.jee;

/**
 * TODO where is this from?
 *
 * @version $Revision$ $Date$
 */
public class ManagedBean extends SessionBean {
    protected boolean hidden;

    public ManagedBean(final String ejbName, final String ejbClass) {
        super(ejbName, ejbClass, SessionType.MANAGED);
    }

    public ManagedBean(final String ejbName, final String ejbClass, final boolean hidden) {
        super(ejbName, ejbClass, SessionType.MANAGED);
        this.hidden = hidden;
    }

    public ManagedBean(final Class<?> ejbClass) {
        this(ejbClass.getSimpleName(), ejbClass.getName());
    }

    public ManagedBean(final String name, final Class<?> ejbClass) {
        this(name, ejbClass.getName());
    }

    public ManagedBean() {
        this(null, (String) null);
    }

    public void setSessionType(final SessionType value) {
    }

    public boolean isHidden() {
        return hidden;
    }
}