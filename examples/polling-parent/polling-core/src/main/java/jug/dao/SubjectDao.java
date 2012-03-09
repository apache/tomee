package jug.dao;

import jug.domain.Subject;
import jug.domain.Value;
import jug.domain.Vote;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

@Singleton
@Lock(LockType.READ)
public class SubjectDao {
    @PersistenceContext(unitName = "polling")
    private EntityManager em;

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
        return em.createNamedQuery(Subject.FIND_BY_NAME_QUERY, Subject.class)
                .setParameter("name", name)
                .getSingleResult();
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

    public Collection<Subject> findAll() {
        return em.createNamedQuery(Subject.FIND_ALL, Subject.class).getResultList();
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
}
