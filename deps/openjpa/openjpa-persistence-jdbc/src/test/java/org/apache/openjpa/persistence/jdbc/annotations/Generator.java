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
package org.apache.openjpa.persistence.jdbc.annotations;


import java.util.*;

import javax.persistence.*;

@Entity
// non-psql/ora dbs cause issues with SequenceGenerator in auto-mapping
//@SequenceGenerator(name="seq", sequenceName="ejb_sequence")
@TableGenerator(name = "tab")
@IdClass(Generator.Oid.class)
public class Generator {

    @Id
    protected int pk;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tab")
    protected Integer pk2;

    @Id
    @GeneratedValue
    protected long pk3;

    @Basic
    protected String stringField;

    // for non-seq
    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPk() {
        return pk;
    }

    public Integer getPk2() {
        return pk2;
    }

    public long getPk3() {
        return pk3;
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String s) {
        stringField = s;
    }

    public static class Oid {

        public int pk;
        public Integer pk2;
        public long pk3;

        public Oid() {
        }

        public Oid(String str) {
            StringTokenizer tok = new StringTokenizer(str, ",");
            pk = Integer.parseInt(tok.nextToken());
            pk2 = Integer.valueOf(tok.nextToken());
            pk3 = Long.valueOf(tok.nextToken());
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof Oid))
                return false;
            Oid other = (Oid) o;
            if (pk != other.pk)
                return false;
            if (pk3 != other.pk3)
                return false;
            if (pk2 == null)
                return other.pk2 == null;
            return pk2.equals(other.pk2);
        }

        public int hashCode() {
            return pk + (pk2 == null ? 0 : pk2.hashCode())
                + (int) (pk3 % Integer.MAX_VALUE);
        }

        public String toString() {
            return pk + "," + pk2 + "," + pk3;
        }
    }
}
