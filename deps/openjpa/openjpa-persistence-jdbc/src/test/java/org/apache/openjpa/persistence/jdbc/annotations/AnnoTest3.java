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

@Entity
@DiscriminatorValue("ANNO3")
@Table(name = "ANNOTEST3")
@PrimaryKeyJoinColumns(@PrimaryKeyJoinColumn(name = "SUB_PK",
    referencedColumnName = "PK"))
public class AnnoTest3 extends AnnoTest1 {

    @Basic
    @Column(name = "SUBBASIC")
    protected int basic2;

    @OneToOne(fetch = FetchType.LAZY)
    protected AnnoTest2 subOneOne;

    public AnnoTest3() {
    }

    public AnnoTest3(long pk) {
        super(pk);
    }

    public AnnoTest3(Long pk) {
        super(pk);
    }

    public void setBasic2(int i) {
        basic2 = i;
    }

    public int getBasic2() {
        return basic2;
    }

    public AnnoTest2 getSubOneOne() {
        return subOneOne;
    }

    public void setSubOneOne(AnnoTest2 anno2) {
        subOneOne = anno2;
    }
}
