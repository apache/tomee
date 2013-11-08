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

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "aimplb")
@IdClass(AImplB.Idkey.class)
public class AImplB implements AIntf {

    private String name;
    @Id
    private int pk1;
    @Id
    private String pk2;

    protected AImplB() {
    }

    public AImplB(String name, int pk1, String pk2) {
        setName(name);
        this.pk1 = pk1;
        this.pk2 = pk2;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setPK1(int pk1) {
        this.pk1 = pk1;
    }

    public int getPK1() {
        return this.pk1;
    }

    public void setPK2(String pk2) {
        this.pk2 = pk2;
    }

    public String getPK2() {
        return this.pk2;
    }

    public static class Idkey implements Serializable {

        public int pk1;
        public String pk2;

        public Idkey() {
        }

        public Idkey(String str) {
            int index = str.indexOf("/");
            if (index != -1) {
                pk1 = Integer.parseInt(str.substring(0, index));
                pk2 = str.substring(index + 1);
            }
        }

        public String toString() {
            return pk1 + "/" + pk2;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Idkey))
                return false;

            Idkey id = (Idkey) other;
            if (pk2 == null && id.pk2 != null)
                return false;
            if (pk2 != null && id.pk2 == null)
                return false;
            if (!(pk1 == id.pk1))
                return false;
            if (!(pk2.equals(id.pk2)))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (pk1 + pk2).hashCode();
        }
    }
}

