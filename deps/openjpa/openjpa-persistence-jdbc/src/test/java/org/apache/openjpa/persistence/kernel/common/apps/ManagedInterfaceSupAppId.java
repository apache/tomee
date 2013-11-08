/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.StringTokenizer;

public interface ManagedInterfaceSupAppId {

    public int getId1();

    public void setId1(int i);

    public int getId2();

    public void setId2(int i);

    public int getIntFieldSup();

    public void setIntFieldSup(int i);

    public static class Id implements java.io.Serializable {

        public int id1;
        public int id2;

        public Id() {
        }

        public Id(String str) {
            StringTokenizer tok = new StringTokenizer(str, ",");
            id1 = Integer.parseInt(tok.nextToken());
            id2 = Integer.parseInt(tok.nextToken());
        }

        public String toString() {
            return id1 + "," + id2;
        }

        public int hashCode() {
            return id1 + id2;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Id))
                return false;
            Id other = (Id) o;
            return id1 == other.id1 && id2 == other.id2;
        }
    }
}
