package org.apache.openejb.arquillian.tests.listenerpersistence;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.UserTransaction;


public class PersistenceServletContextListener implements ServletContextListener {

    @Resource
    private UserTransaction transaction;

    @PersistenceUnit
    private EntityManagerFactory entityMgrFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public void contextInitialized(ServletContextEvent event) {
        final ServletContext context = event.getServletContext();

        if (transaction != null) {
            try {
                transaction.begin();
                transaction.commit();
                context.setAttribute(ContextAttributeName.KEY_Transaction.name(), "Transaction injection successful");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (entityManager != null) {
            Address a = new Address();
            try {
                entityManager.contains(a);
                context.setAttribute(ContextAttributeName.KEY_EntityManager.name(), "Transaction manager injection successful");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (entityMgrFactory != null) {
            Address a = new Address();
            try {
                EntityManager em = entityMgrFactory.createEntityManager();
                em.contains(a);
                context.setAttribute(ContextAttributeName.KEY_EntityManagerFactory.name(), "Transaction manager factory injection successful");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void contextDestroyed(ServletContextEvent event) {
    }

}