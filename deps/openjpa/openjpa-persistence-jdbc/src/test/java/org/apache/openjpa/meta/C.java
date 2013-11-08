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
package org.apache.openjpa.meta;

import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.openjpa.meta.C.Identity;

@Entity
@IdClass(Identity.class)
@Table(name="meta_C")
public class C {
    private A a;
    private B b;
    private long num;

    @ManyToOne(optional = false)
    @Column(nullable = false)
    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    @Id
    @ManyToOne(optional = false)
    @Column(nullable = false)
    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    @Id
    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public static class Identity {
        private String b;
        private long num;

        @Override
        public int hashCode() {
            return b.hashCode() * 17 + (int) num;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && (obj instanceof Identity)
                && b.equals(((Identity) obj).b) && num == ((Identity) obj).num;
        }

        public String getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b.getId();
        }

        public void setB(String b) {
            this.b = b;
        }

        public long getNum() {
            return num;
        }

        public void setNum(long num) {
            this.num = num;
        }
    }
}
