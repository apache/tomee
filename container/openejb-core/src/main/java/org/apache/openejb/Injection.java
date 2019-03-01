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

import java.util.Objects;

/**
 * @version $Rev$ $Date$
 */
public class Injection {
    private Class target;
    private final String classname;
    private final String name;
    private final String jndiName;

    public Injection(final String jndiName, final String name, final Class target) {
        this.jndiName = jndiName;
        this.name = name;
        this.target = target;
        this.classname = target.getName();
    }

    public Injection(final String jndiName, final String name, final String classname) {
        this.jndiName = jndiName;
        this.name = name;
        this.classname = classname;
        this.target = null;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getName() {
        return name;
    }

    public Class getTarget() {
        return target;
    }

    public void setTarget(final Class<?> target) {
        this.target = target;
    }

    public String getClassname() {
        return classname;
    }

    @Override
    public String toString() {
        return "Injection{" +
            "target=" + classname +
            ", name='" + name + '\'' +
            ", jndiName='" + jndiName + '\'' +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Injection)) {
            return false;
        }

        final Injection injection = (Injection) o;

        if (!Objects.equals(name, injection.name)) {
            return false;
        }
        if (!Objects.equals(classname, injection.classname)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = classname != null ? classname.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
