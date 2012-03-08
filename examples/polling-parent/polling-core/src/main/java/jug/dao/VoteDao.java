package jug.dao;

import jug.domain.Value;
import jug.domain.Vote;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
@Lock(LockType.READ)
public class VoteDao {
    @PersistenceContext
    private EntityManager em;

    public Vote create(final Value voteValue) {
        final Vote vote = new Vote();
        vote.setValue(voteValue);

        em.persist(vote);
        return vote;
    }
}
