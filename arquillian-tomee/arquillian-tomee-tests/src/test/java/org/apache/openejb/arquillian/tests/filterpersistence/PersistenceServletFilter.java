package org.apache.openejb.arquillian.tests.filterpersistence;

import java.io.IOException;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.transaction.UserTransaction;

import org.apache.openejb.arquillian.tests.TestRun;
import org.junit.Assert;

public class PersistenceServletFilter implements Filter {

    @Resource
    private UserTransaction transaction;

    @PersistenceUnit
    private EntityManagerFactory entityMgrFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        TestRun.run(req, resp, this);
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