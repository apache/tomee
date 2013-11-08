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
package org.apache.openjpa.persistence.meta.common.apps;

import javax.persistence.Entity;

@Entity
/**
 * <p>Persistent type used in the testing of the JDO metadata.</p>
 *
 * @author Abe White
 */
public abstract class MetaTest5 {

    private long id;

    public static class MetaTest5Id {

        public long id;

        public MetaTest5Id() {
        }

        public MetaTest5Id(String str) {
            id = Long.parseLong(str);
        }

        public String toString() {
            return String.valueOf(id);
        }

        public boolean equals(Object other) {
            return other instanceof MetaTest5Id
                && ((MetaTest5Id) other).id == id;
        }

        public int hashCode() {
            return (int) (id % Integer.MAX_VALUE);
        }
    }
}
