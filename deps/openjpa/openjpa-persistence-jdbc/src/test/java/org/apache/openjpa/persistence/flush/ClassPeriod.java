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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.openjpa.persistence.ElementDependent;

@Entity
@Table(name="FL_CLP")
public class ClassPeriod implements Serializable {

    private static final long serialVersionUID = -5315185851562144594L;

    @Id
    @Column(name="CLP_ID")
    @SequenceGenerator(name="clpIdSeq", sequenceName="FL_CLP_SEQ")
    @GeneratedValue(generator="clpIdSeq", strategy=GenerationType.SEQUENCE)
    protected Long clpId;
    
    @Column(name="CLP_TEXT")
    protected String clpText;
    
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch=FetchType.LAZY)
    @JoinColumn(name="COURSE_ID")
    protected Course course;
    
    @OneToMany(mappedBy="clp",cascade=CascadeType.ALL,fetch=FetchType.EAGER)
    @ElementDependent(true)
    protected Set<Topic> topics;
    
    public Set<Topic> getTopics() {
        return topics;
    }
    
    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }
    
    public Long getClassPeriodId() {
        return clpId;
    }
    
    public void setClassPeriodId(Long clpId) {
        this.clpId = clpId;
    }
    
    public Course getCourse() {
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;   
    }
    
    public String getClassPeriodText() {
        return clpText;
    }
    
    public void setClassPeriodText(String clpText) {
        this.clpText = clpText;
    }
}
