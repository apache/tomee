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

import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@Entity
@Table(name="OCCourse")
public class Course {
    
    private int id;

    private String name;
    
    private Set<Student> students;
    private List<Student> waitList;
    
    public Course() {        
    }
    
    public Course(String name) {
        this.name = name;
    }
    
    @ManyToMany(cascade=CascadeType.PERSIST)
    @JoinTable(name="COURSE_ENROLLMENT")
    public Set<Student> getStudents() {
        return students;
    }
    
    public void setStudents(Set<Student> students) {
        this.students = students;
    }
    
    @ManyToMany(cascade=CascadeType.PERSIST)
    @JoinTable(name="WAIT_LIST")
    @OrderColumn(name="WAITLIST_ORDER")
    public List<Student> getWaitList() {
        return waitList;
    }
    
    public void setWaitList(List<Student> list) {
        waitList = list;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    public String getName() {
        return name;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Course) {
            Course course = (Course)obj;
            return getId() == course.getId() &&
                getName().equalsIgnoreCase(course.getName());
        }
        return false;
    }

}
