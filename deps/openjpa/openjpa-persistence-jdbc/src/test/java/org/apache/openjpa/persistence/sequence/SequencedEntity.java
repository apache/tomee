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
package org.apache.openjpa.persistence.sequence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="SEQENTITY_TBL")
@SequenceGenerator(name="SeqEntity", sequenceName="SEQENTITY_ntv_seq", allocationSize = 1)
public class SequencedEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SeqEntity")
    private int id;

    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SeqEntity2")
    @SequenceGenerator(name="SeqEntity2", sequenceName="SEQENTITY_TBL_gval1_seq", allocationSize = 1)
    private int gval1;
    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SeqEntity3")
    @SequenceGenerator(name="SeqEntity3", sequenceName="SEQENTITY_TBL_g_val2_seq", allocationSize = 1)
    @Column(name="g_val2")
    private int gval2;

    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SeqEntity4")
    @SequenceGenerator(name="SeqEntity4", sequenceName="SEQENTITY_gval3_seq", allocationSize = 1)
    private int gval3;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setGval2(int gval) {
        this.gval2 = gval;
    }

    public int getGval2() {
        return gval2;
    }

    public void setGval1(int gval1) {
        this.gval1 = gval1;
    }

    public int getGval1() {
        return gval1;
    }

    public void setGval3(int gval3) {
        this.gval3 = gval3;
    }

    public int getGval3() {
        return gval3;
    }
}
