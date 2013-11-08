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
package org.apache.openjpa.persistence.jdbc.maps.spec_10_1_29_ex3;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name="T29x3Stud")
public class Student {
    @Id
    int id;

    String name;

    @ManyToMany
    @JoinTable(name="CENROLLS",
            joinColumns=@JoinColumn(name="STUDENT"),
            inverseJoinColumns=@JoinColumn(name="SEMESTER"))
            @MapKeyJoinColumn(name="COURSE")    
            Map<Course, Semester> enrollment =
                new HashMap<Course, Semester>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map getEnrollment() {
        return enrollment;
    }

    public void addToEnrollment(Course course, Semester semester) {
        enrollment.put(course, semester);
    }
}
