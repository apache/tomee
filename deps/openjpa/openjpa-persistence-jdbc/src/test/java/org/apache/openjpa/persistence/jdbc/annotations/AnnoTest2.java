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
@Table(name = "ANNOTEST2")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ANNOCLS")
@DiscriminatorValue("ANNO2")
@IdClass(AnnoTest2.Oid.class)
public class AnnoTest2 {

    @Id
    @Column(name = "PK1")
    protected long pk1;

    @Id
    @Column(name = "PK2")
    protected String pk2;

    @Version
    @Column(name = "ANNOVER")
    protected Date version;

    @Basic
    protected String basic;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INVERSEONEONE_PK", referencedColumnName = "PK")
    protected AnnoTest1 inverseOneOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANYONEONE_PK", referencedColumnName = "PK")
    protected AnnoTest1 oneManyOwner;

    @ManyToMany
    @JoinTable(name = "ANNOTEST2_MANYMANY",
        joinColumns = {
        @JoinColumn(name = "MANY_PK1", referencedColumnName = "PK1"),
        @JoinColumn(name = "MANY_PK2", referencedColumnName = "PK2") },
        inverseJoinColumns =
        @JoinColumn(name = "MANYE_PK", referencedColumnName = "PK"))
    protected Set<AnnoTest1> manyMany = new HashSet();

    public AnnoTest2() {
    }

    public AnnoTest2(long pk1, String pk2) {
        this.pk1 = pk1;
        this.pk2 = pk2;
    }

    public void setPk1(long val) {
        pk1 = val;
    }

    public long getPk1() {
        return pk1;
    }

    public void setPk2(String str) {
        pk2 = str;
    }

    public String getPk2() {
        return pk2;
    }

    public Date getVersion() {
        return version;
    }

    public void setBasic(String s) {
        basic = s;
    }

    public String getBasic() {
        return basic;
    }

    public void setInverseOneOne(AnnoTest1 other) {
        inverseOneOne = other;
    }

    public AnnoTest1 getInverseOneOne() {
        return inverseOneOne;
    }

    public void setOneManyOwner(AnnoTest1 other) {
        oneManyOwner = other;
    }

    public AnnoTest1 getOneManyOwner() {
        return oneManyOwner;
    }

    public Set getManyMany() {
        return manyMany;
    }

    public static class Oid {

        public long pk1;
        public String pk2;

        public Oid() {
        }

        public Oid(long pk1, String pk2) {
            this.pk1 = pk1;
            this.pk2 = pk2;
        }

        public Oid(String str) {
            if (str != null) {
                int index = str.indexOf(",");
                pk1 = Long.parseLong(str.substring(0, index));
                pk2 = str.substring(index + 1);
                if ("null".equals(pk2))
                    pk2 = null;
            }
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Oid))
                return false;
            Oid other = (Oid) o;
            if (pk1 != other.pk1)
                return false;
            if (pk2 == null)
                return other.pk2 == null;
            return pk2.equals(other.pk2);
        }

        public int hashCode() {
            return ((int) pk1) + (pk2 == null ? 0 : pk2.hashCode());
        }

        public String toString() {
            return pk1 + "," + (pk2 == null ? "null" : pk2);
        }
    }
}
