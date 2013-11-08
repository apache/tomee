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
package org.apache.openjpa.persistence.jdbc.order;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Home {

    @Id
    @GeneratedValue
    private int id;

    @Basic
    private int buildYear;
    
    public Home() {        
    }

    public Home(int year) {
        this.buildYear = year;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setBuildYear(int buildYear) {
        this.buildYear = buildYear;
    }

    public int getBuildYear() {
        return buildYear;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Home) {
            Home home = (Home)obj;
            return getBuildYear() == home.getBuildYear() &&
              getId() == home.getId();
        }
        return false;
    }
}
