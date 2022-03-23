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
package jug.dao;

import jug.domain.Subject;
import jug.domain.Value;
import jug.domain.Vote;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;

@Singleton
@Lock(LockType.READ)
public class SubjectDao {

    @PersistenceContext(unitName = "polling")
    private EntityManager em;

    @Inject
    private ReadSubjectDao readDao;

    public Subject create(final String name, final String question) {
        final Subject subject = new Subject();
        subject.setName(name);
        subject.setQuestion(question);

        em.persist(subject);
        return subject;
    }

    public Subject addVote(final Subject subject, final Vote vote) {
        final Vote foundVote = retrieve(vote, Vote.class, vote.getId());
        final Subject subjectToUpdate = retrieve(subject, Subject.class, subject.getId());

        subjectToUpdate.getVotes().add(foundVote);
        return subjectToUpdate;
    }

    public Subject findByName(final String name) {
        return readDao.findByName(name);
    }

    public Collection<Subject> findAll() {
        return readDao.findAll();
    }

    public int subjectLikeVoteNumber(final String subjectName) {
        return subjectVoteNumber(subjectName, Value.I_LIKE);
    }

    public int subjectNotLikeVoteNumber(final String subjectName) {
        return subjectVoteNumber(subjectName, Value.I_DONT_LIKE);
    }

    private int subjectVoteNumber(final String subjectName, final Value value) {
        return em.createNamedQuery(Subject.COUNT_VOTE, Number.class)
                .setParameter("name", subjectName)
                .setParameter("value", value)
                .getSingleResult().intValue();
    }

    private <T> T retrieve(final T object, final Class<T> clazz, long id) {
        if (em.contains(object)) {
            return object;
        }

        final T t = em.find(clazz, id);
        if (t == null) {
            throw new IllegalArgumentException(clazz.getSimpleName() + " not found");
        }
        return t;
    }

    public Subject bestSubject() {
        int bestScore = 0;
        Subject best = null;
        for (Subject subject : findAll()) {
            int currentScore = subject.score();
            if (best == null || bestScore < currentScore) {
                bestScore = currentScore;
                best = subject;
            }
        }
        return best;
    }

    @Singleton
    @Lock(LockType.READ)
    @PersistenceContext(name = "polling")
    public static interface ReadSubjectDao {

        Subject findByName(final String name);

        Collection<Subject> findAll();
    }
}
