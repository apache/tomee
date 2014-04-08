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
package org.apache.openejb.core.cmp.jpa;

public class ComplexId {
    public String firstId;
    public String secondId;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplexId complexId = (ComplexId) o;

        if (secondId != null ? !secondId.equals(complexId.secondId) : complexId.secondId != null) return false;
        if (firstId != null ? !firstId.equals(complexId.firstId) : complexId.firstId != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (firstId != null ? firstId.hashCode() : 0);
        result = 31 * result + (secondId != null ? secondId.hashCode() : 0);
        return result;
    }
}
