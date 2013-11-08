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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

@Entity
@Table(name = "PER_JDBC_KERN_EMP")
public class MetaTest7 {

    public long id;
    private MetaTest7Status status;
    private MetaTest7IntLongStatus intLongStatus;
    private MetaTest7IntIntegerStatus intIntegerStatus;
    private MetaTest7IntegerIntegerStatus integerIntegerStatus;
    private MetaTest7IntegerIntStatus integerIntStatus;
    private MetaTest7IntegerLongStatus integerLongStatus;

    @Externalizer("getName")
    @Persistent(optional = false)
    @Column(name = "status")
    @Factory("valueOf")
    public MetaTest7Status getStatus() {
        return status;
    }

    public void setStatus(MetaTest7Status status) {
        this.status = status;
    }

    @Externalizer("getName")
    @Persistent(optional = false)
    @Column(name = "intLongStatus")
    @Factory("valueOf")
    public MetaTest7IntLongStatus getIntLongStatus() {
        return intLongStatus;
    }

    public void setIntLongStatus(MetaTest7IntLongStatus status) {
        this.intLongStatus = status;
    }

    @Externalizer("getName")
    @Persistent(optional = false)
    @Column(name = "intIntegerStatus")
    @Factory("valueOf")
    public MetaTest7IntIntegerStatus getIntIntegerStatus() {
        return intIntegerStatus;
    }

    public void setIntIntegerStatus(MetaTest7IntIntegerStatus status) {
        this.intIntegerStatus = status;
    }

    @Externalizer("getName")
    @Persistent(optional = false)
    @Column(name = "integerIntegerStatus")
    @Factory("valueOf")
    public MetaTest7IntegerIntegerStatus getIntegerIntegerStatus() {
        return integerIntegerStatus;
    }

    public void setIntegerIntegerStatus(MetaTest7IntegerIntegerStatus status) {
        this.integerIntegerStatus = status;
    }

    @Externalizer("getName")
    @Persistent(optional = false)
    @Column(name = "integerIntStatus")
    @Factory("valueOf")
    public MetaTest7IntegerIntStatus getIntegerIntStatus() {
        return integerIntStatus;
    }

    public void setIntegerIntStatus(MetaTest7IntegerIntStatus status) {
        this.integerIntStatus = status;
    }

    @Externalizer("getName")
    @Persistent(optional = false)
    @Column(name = "integerLongStatus")
    @Factory("valueOf")
    public MetaTest7IntegerLongStatus getIntegerLongStatus() {
        return integerLongStatus;
    }

    public void setIntegerLongStatus(MetaTest7IntegerLongStatus status) {
        this.integerLongStatus = status;
    }
    
    static class MetaTest7IntegerIntegerStatus {

        public Integer getName() {
            return 0;
        }

        public static MetaTest7IntegerIntegerStatus valueOf(final Integer ordinal) {
            return null;
        }

        public static MetaTest7IntegerIntegerStatus valueOf(final String name) {
            return null;
        }
    }

    static class MetaTest7IntegerIntStatus {

        public Integer getName() {
            return 0;
        }

        public static MetaTest7IntegerIntStatus valueOf(final int ordinal) {
            return null;
        }

        public static MetaTest7IntegerIntStatus valueOf(final String name) {
            return null;
        }
    }

    static class MetaTest7IntegerLongStatus {

        public Integer getName() {
            return 0;
        }

        public static MetaTest7IntegerLongStatus valueOf(final long ordinal) {
            return null;
        }

        public static MetaTest7IntegerLongStatus valueOf(final String name) {
            return null;
        }
    }

    static class MetaTest7IntIntegerStatus {

        public int getName() {
            return 0;
        }

        public static MetaTest7IntIntegerStatus valueOf(final Integer ordinal) {
            return null;
        }

        public static MetaTest7IntIntegerStatus valueOf(final String name) {
            return null;
        }
    }

    static class MetaTest7IntLongStatus {

        public int getName() {
            return 0;
        }

        public static MetaTest7IntLongStatus valueOf(final long ordinal) {
            return null;
        }

        public static MetaTest7IntLongStatus valueOf(final String name) {
            return null;
        }
    }

    static class MetaTest7Status {

        public String getName() {
            return null;
        }

        public static MetaTest7Status valueOf(final int ordinal) {
            return null;
        }

        public static MetaTest7Status valueOf(final String name) {
            return null;
        }
    }
}
