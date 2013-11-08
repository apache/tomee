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
package org.apache.openjpa.persistence.nullity;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * This Entity contains two date fields which are updated by the database. The
 * fields are non-insertable and non-updatable respectively making them read
 * only in many situations. The fields should be ignored when persisting or
 * updating an entity (again respectively).
 * 
 * <P>
 * <b>The syntax used for the database to generate the date column is specific
 * to Derby and DB2. Any testcase which uses this entity must ensure that one of
 * those databases is used, or use pre-existing tables</b>
 * </P>
 */
@Entity
public class TimestampedEntity {
    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;

    @Column(nullable = false, insertable = false, 
        columnDefinition = "DATE default '2008-01-01'")
    private Date nonInsertableNonNullableDate;

    @Column(nullable = false, updatable = false, 
        columnDefinition = "DATE default '2008-01-01'")
    private Date nonUpdatableNonNullableDate;

    public TimestampedEntity() { 
        setNonUpdatableNonNullableDate(new Date(52349606));
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getNonInsertableNonNullableDate() {
        return nonInsertableNonNullableDate;
    }

    public void setNonInsertableNonNullableDate(
        Date nonInsertableNonNullableDate) {
        this.nonInsertableNonNullableDate = nonInsertableNonNullableDate;
    }

    public Date getNonUpdatableNonNullableDate() {
        return nonUpdatableNonNullableDate;
    }

    public void setNonUpdatableNonNullableDate(
        Date nonUpdatableNonNullableDate) {
        this.nonUpdatableNonNullableDate = nonUpdatableNonNullableDate;
    }
}
