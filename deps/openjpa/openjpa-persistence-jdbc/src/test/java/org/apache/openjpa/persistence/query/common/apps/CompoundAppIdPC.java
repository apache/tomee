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
package org.apache.openjpa.persistence.query.common.apps;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * <p>Application identity type with a compound primary key.</p>
 *
 * @author Abe White
 */

@Entity
@IdClass(CompoundAppIdPC.Idkey.class)
public class CompoundAppIdPC {

    @Id
    private String pk1;

    @Id
    private int pk2;

    private int intField;

    public String getPk1() {
        return this.pk1;
    }

    public void setPk1(String pk1) {
        this.pk1 = pk1;
    }

    public int getPk2() {
        return this.pk2;
    }

    public void setPk2(int pk2) {
        this.pk2 = pk2;
    }

    public int getIntField() {
        return this.intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public static class Idkey implements Serializable {

        public String pk1;
        public int pk2;

        public Idkey() {
        }

        public Idkey(String str) {
            int index = str.indexOf("/");
            if (index != -1) {
                pk2 = Integer.parseInt(str.substring(0, index));
                pk1 = str.substring(index + 1);
            }
        }

        public String toString() {
            return pk2 + "/" + pk1;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Idkey))
                return false;

            Idkey id = (Idkey) other;
            if (pk1 == null && id.pk1 != null)
                return false;
            if (pk1 != null && id.pk1 == null)
                return false;
            if (!(pk1 == id.pk1))
                return false;
            if (!(pk1.equals(id.pk1)))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (pk2 + pk1).hashCode();
        }
    }
}
