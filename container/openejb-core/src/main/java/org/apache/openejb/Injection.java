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
public class Injection {
    private final Class target;
    private final String name;
    private final String jndiName;

    public Injection(String jndiName, String name, Class target) {
        this.jndiName = jndiName;
        this.name = name;
        this.target = target;
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

    @Override
    public String toString() {
        return "Injection{" +
                "target=" + ((target != null) ? target.getName() : null) +
                ", name='" + name + '\'' +
                ", jndiName='" + jndiName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Injection)) return false;

        Injection injection = (Injection) o;

        if (name != null ? !name.equals(injection.name) : injection.name != null) return false;
        if (target != null ? !target.equals(injection.target) : injection.target != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = target != null ? target.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
