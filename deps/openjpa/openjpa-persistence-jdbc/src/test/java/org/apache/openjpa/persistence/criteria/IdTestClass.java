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
package org.apache.openjpa.persistence.criteria;

/**
 * A class is used as an identifier to {@link EntityWithIdClass another persistent entity}.
 * <br>
 * The type and name of the declared fields of this class and the identifier fields declared
 * in the persistent entity must be compatiable.
 * <br>
 * This class also must implement its <tt>equals()</tt> and <tt>hashCode()</tt> methods
 * properly i.e. involving the declared fields. Otherwise a JPA runtime will not work
 * properly.
 * 
 * @author Pinaki Poddar
 *
 */
public class IdTestClass {
        String name;
        long   ssn;
        
        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((name == null) ? 0 : name.hashCode());
                result = prime * result + (int) (ssn ^ (ssn >>> 32));
                return result;
        }
        
        @Override
        public boolean equals(Object obj) {
                if (this == obj)
                        return true;
                if (obj == null)
                        return false;
                if (getClass() != obj.getClass())
                        return false;
                IdTestClass other = (IdTestClass) obj;
                if (name == null) {
                        if (other.name != null)
                                return false;
                } else if (!name.equals(other.name))
                        return false;
                if (ssn != other.ssn)
                        return false;
                return true;
        }
}
