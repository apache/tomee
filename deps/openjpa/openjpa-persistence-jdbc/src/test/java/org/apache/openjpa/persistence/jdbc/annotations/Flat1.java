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


import javax.persistence.*;

@Entity(name="Flat1")
@Table(name="Flat1")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "EJB_CLS", length=100)
public class Flat1 {

    @Id
    @Column(name = "PK")
    protected int pk;

    @Version
    @Column(name = "EJB_VER")
    protected int version;

    @Basic
    protected int basic;

    public Flat1() {
    }

    public Flat1(int pk) {
        this.pk = pk;
    }

    public void setPk(int val) {
        pk = val;
    }

    public int getPk() {
        return pk;
    }

    public int getVersion() {
        return version;
    }

    public void setBasic(int i) {
        basic = i;
    }

    public int getBasic() {
        return basic;
    }
}

