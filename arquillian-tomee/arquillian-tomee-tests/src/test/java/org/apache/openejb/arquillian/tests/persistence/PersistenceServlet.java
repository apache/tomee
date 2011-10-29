package org.apache.openejb.arquillian.tests.persistence;

import java.io.IOException;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.apache.openejb.arquillian.tests.Runner;
import org.junit.Assert;

public class PersistenceServlet extends HttpServlet {

    @Resource
    private UserTransaction transaction;

    @PersistenceUnit
    private EntityManagerFactory entityMgrFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Runner.run(req, resp, this);
    }

    public void testEntityManagerFactory() {
        Assert.assertNotNull(entityMgrFactory);

        Address a = new Address();
        EntityManager em = entityMgrFactory.createEntityManager();
        em.contains(a);
    }

    public void testEntityManager() {
        Assert.assertNotNull(entityManager);
        Address a = new Address();
        entityManager.contains(a);
    }

    public void testUserTransaction() throws Exception{
        Assert.assertNotNull(transaction);
        transaction.begin();
        transaction.commit();
    }

}