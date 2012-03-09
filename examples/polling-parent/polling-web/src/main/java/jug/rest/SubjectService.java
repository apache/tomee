package jug.rest;

import jug.dao.SubjectDao;
import jug.dao.VoteDao;
import jug.domain.Result;
import jug.domain.Subject;
import jug.domain.Value;
import jug.domain.Vote;
import jug.monitoring.VoteCounter;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;

@Path("subject")
@Singleton // an ejb just to be able to test in standalone
@Lock(LockType.READ)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Produces({ MediaType.APPLICATION_JSON })
public class SubjectService {
    @Inject
    private SubjectDao dao;

    @Inject
    private VoteDao voteDao;

    @Inject
    private VoteCounter counter;

    @Resource(name = "poll.blacklist")
    private List<String> blackList;

    @POST
    @Path("create")
    public Subject create(final String question, @QueryParam("name") final String name) {
        if (blackList.contains(name)) {
            throw new IllegalArgumentException("name blacklisted");
        }

        final Subject subject = dao.create(name, question);

        counter.putSubject(subject);

        return subject;
    }

    @GET
    @Path("list")
    public Collection<Subject> list() {
        return dao.findAll();
    }

    @GET
    @Path("find/{name}")
    public Subject findByName(@PathParam("name") final String name) {
        return dao.findByName(name);
    }

    @GET
    @Path("best")
    public Subject best() {
        return dao.bestSubject();
    }

    @GET
    @Path("result/{name}")
    public Result result(@PathParam("name") final String name) {
        return new Result(dao.subjectLikeVoteNumber(name), -dao.subjectNotLikeVoteNumber(name));
    }

    @POST
    @Path("vote")
    public Vote vote(final String input, @QueryParam("subject") final String subjectName) {
        final Vote vote = voteDao.create(Value.valueOf(input));
        final Subject subject = dao.findByName(subjectName);
        dao.addVote(subject, vote);

        counter.putSubject(subject); // update

        return vote;
    }
}
