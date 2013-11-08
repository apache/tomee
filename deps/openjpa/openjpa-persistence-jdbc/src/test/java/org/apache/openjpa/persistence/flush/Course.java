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
package org.apache.openjpa.persistence.flush;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.openjpa.persistence.ElementDependent;

@Entity
@Table(name="FL_COURSE")
public class Course implements Serializable {

    private static final long serialVersionUID = -5351948190722744801L;

    @Id
    @Column(name="COURSE_ID")
    @SequenceGenerator(name="courseIdSeq", sequenceName="FL_COURSE_SEQ")
    @GeneratedValue(generator="courseIdSeq", strategy=GenerationType.SEQUENCE)
    protected Long courseId;
    
    @Column(name="COURSE_TEXT")
    protected String courseText;
    
    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @ElementDependent(true)
    protected Set<ClassPeriod> classPeriods;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseText() {
        return courseText;
    }

    public void setCourseText(String courseText) {
        this.courseText = courseText;
    }

    public Set<ClassPeriod> getClassPeriods() {
        return classPeriods;
    }

    public void setClassPeriods(Set<ClassPeriod> classPeriods) {
        this.classPeriods = classPeriods;
    }
}
