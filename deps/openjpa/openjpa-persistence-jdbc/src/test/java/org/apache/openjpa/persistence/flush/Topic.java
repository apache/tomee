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
@Table(name="FL_TOPIC")
public class Topic implements Serializable {

    private static final long serialVersionUID = -2570150606711529060L;

    @Id
    @Column(name="TOPIC_ID")
    @SequenceGenerator(name="topicIdSeq", sequenceName="TOPIC_SEQ")
    @GeneratedValue(generator="topicIdSeq", strategy=GenerationType.SEQUENCE)
    protected Long topicId;
    
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch=FetchType.LAZY)
    @JoinColumn(name="CLP_ID")
    protected ClassPeriod clp;

    @Column(name="TOPIC_TEXT")
    protected String topicText;

    @OneToMany(mappedBy="topic",cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @ElementDependent(true)
    protected Set<Assignment> assignments;
    
    @OneToMany(mappedBy="topic",cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @ElementDependent(true)
    protected Set<SubTopic> subTopics;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public ClassPeriod getClassPeriod() {
        return clp;
    }

    public void setClassPeriod(ClassPeriod clp) {
        this.clp = clp;
    }

    public String getTopicText() {
        return topicText;
    }

    public void setTopicText(String topicText) {
        this.topicText = topicText;
    }

    public Set<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<Assignment> assignments) {
        this.assignments = assignments;
    }

    public Set<SubTopic> getSubTopics() {
        return subTopics;
    }

    public void setSubTopics(Set<SubTopic> subTopics) {
        this.subTopics = subTopics;
    }
}
