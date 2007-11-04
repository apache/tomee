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
package org.apache.openejb.core;

import org.apache.openejb.Injection;

import javax.naming.Context;
import java.util.Collection;
import java.util.ArrayList;

public class CoreWebDeploymentInfo implements WebDeploymentInfo {
    private String id;
    private ClassLoader classLoader;
    private final Collection<Injection> injections = new ArrayList<Injection>();
    private Context jndiEnc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Collection<Injection> getInjections() {
        return injections;
    }

    public Context getJndiEnc() {
        return jndiEnc;
    }

    public void setJndiEnc(Context jndiEnc) {
        this.jndiEnc = jndiEnc;
    }
}
