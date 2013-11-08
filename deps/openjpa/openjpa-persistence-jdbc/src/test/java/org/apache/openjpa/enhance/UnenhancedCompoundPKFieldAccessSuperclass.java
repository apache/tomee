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
package org.apache.openjpa.enhance;

import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@IdClass(UnenhancedCompoundPKFieldAccessSuperclass.PK.class)
public class UnenhancedCompoundPKFieldAccessSuperclass {

    @Id
    private int id0;
    @Id
    private int id1;

    protected UnenhancedCompoundPKFieldAccessSuperclass() {
    }

    public UnenhancedCompoundPKFieldAccessSuperclass(int i0, int i1) {
        id0 = i0;
        id1 = i1;
    }

    public static class PK {

        static {
            // register persistent class in JVM
            try {
                Class.forName(
                    UnenhancedCompoundPKFieldAccessSuperclass.class.getName());
            } catch (Exception e) {
                // ignore
            }
        }

        public int id0;
        public int id1;

        public PK() {
        }

        public PK(int i0, int i1) {
            id0 = i0;
            id1 = i1;
        }

        public String toString() {
            return String.valueOf(id0)
                + "::" + String.valueOf(id1);
        }

        public int hashCode() {
            int rs = 17;
            rs = rs * 37 + (int) (id0 ^ (id1 >>> 32));
            rs = rs * 37 + (int) (id0 ^ (id1 >>> 32));
            return rs;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || obj.getClass() != getClass())
                return false;

            UnenhancedCompoundPKFieldAccessSuperclass.PK other =
                (UnenhancedCompoundPKFieldAccessSuperclass.PK) obj;
            return (id0 == other.id0)
                && (id1 == other.id1);
        }
    }
}
