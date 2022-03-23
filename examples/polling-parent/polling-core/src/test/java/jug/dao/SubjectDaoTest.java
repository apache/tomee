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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubjectDaoTest {

    private static EJBContainer container;

    @Inject
    private SubjectDao subjectDao;

    @Inject
    private VoteDao voteDao;

    @BeforeClass
    public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
    }

    @AfterClass
    public static void stop() {
        container.close();
    }

    @Test
    public void persistSimpleSubject() {
        final Subject subject = subjectDao.create("TOMEE_JUG", "What do you think about this JUG?");
        assertNotNull(subject);
        assertEquals("TOMEE_JUG", subject.getName());
    }

    @Test
    public void playWithVotes() {
        Subject subject = subjectDao.create("TOMEE_JUG_2", "What do you think about this JUG?");

        final Vote vote = voteDao.create(Value.I_LIKE);
        subject = subjectDao.addVote(subject, vote);
        assertEquals(1, subject.getVotes().size());

        final Vote moreVote = voteDao.create(Value.I_LIKE);
        subject = subjectDao.addVote(subject, moreVote);
        assertEquals(2, subject.getVotes().size());

        final Vote notLiked = voteDao.create(Value.I_DONT_LIKE);
        subject = subjectDao.addVote(subject, notLiked);
        assertEquals(3, subject.getVotes().size());

        final Subject retrievedSubject = subjectDao.findByName("TOMEE_JUG_2");
        assertNotNull(retrievedSubject);
        assertNotNull(retrievedSubject.getVotes());
        assertEquals(3, retrievedSubject.getVotes().size());
    }

    @Test
    public void voteNumber() {
        final Subject subject = subjectDao.create("TOMEE_JUG_3", "What do you think about this JUG?");

        subjectDao.addVote(subject, voteDao.create(Value.I_LIKE));
        subjectDao.addVote(subject, voteDao.create(Value.I_LIKE));
        subjectDao.addVote(subject, voteDao.create(Value.I_DONT_LIKE));

        assertEquals(2, subjectDao.subjectLikeVoteNumber("TOMEE_JUG_3"));
        assertEquals(1, subjectDao.subjectNotLikeVoteNumber("TOMEE_JUG_3"));
    }
}
