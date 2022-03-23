/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@NamedQueries({
        @NamedQuery(name = Subject.FIND_ALL, query = "select s from Subject s"),
        @NamedQuery(name = Subject.FIND_BY_NAME_QUERY, query = "select s from Subject s where s.name = :name"),
        @NamedQuery(name = Subject.COUNT_VOTE, query = "select count(s) from Subject s left join s.votes v where v.value = :value and :name = s.name")
})
@XmlRootElement
public class Subject {

    public static final String FIND_BY_NAME_QUERY = "Subject.findByName";
    public static final String COUNT_VOTE = "Subject.countVoteNumber";
    public static final String FIND_ALL = "Subject.findAll";

    @Id
    @GeneratedValue
    private long id;

    private String name;

    private String question;

    @OneToMany(fetch = FetchType.EAGER)
    private Collection<Vote> votes = new ArrayList<Vote>();

    public Subject() {
        // no-op
    }

    public Subject(String name, String question) {
        this.name = name;
        this.question = question;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Collection<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Collection<Vote> votes) {
        this.votes = votes;
    }

    public int score() {
        int s = 0;
        for (Vote vote : votes) {
            if (vote.getValue().equals(Value.I_LIKE)) {
                s++;
            } else {
                s--;
            }
        }
        return s;
    }
}
