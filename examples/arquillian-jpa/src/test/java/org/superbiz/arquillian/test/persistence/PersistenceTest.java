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
package org.superbiz.arquillian.test.persistence;

import java.util.concurrent.Callable;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import static jakarta.ejb.TransactionAttributeType.REQUIRES_NEW;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.arquillian.persistence.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class PersistenceTest {
    @Deployment
    public static Archive<?> createDeploymentPackage() {
        return ShrinkWrap.create(WebArchive.class, "UserPersistenceTest.war")
                .addPackage(User.class.getPackage())
                .addAsManifestResource(new ClassLoaderAsset("META-INF/persistence.xml"), "persistence.xml");
    }

    @PersistenceContext
    private EntityManager em;
    
    @EJB
    private Caller transactionalCaller;

    
    public void seriouslyYouAlreadyForgotOpenEJB_questionMark() throws Exception {
        
        final User user = em.find(User.class, 2L);
        assertNotNull(user);
        
        user.setName("OpenEJB"); // @Transactional(TransactionMode.COMMIT) will commit it and datasets/expected-users.yml will check it
    }
    
    @Test
    @Transactional(TransactionMode.COMMIT) // default with persistence extension
    @UsingDataSet("datasets/users.yml")
    @ShouldMatchDataSet("datasets/expected-users.yml")
    public void testWithTransaction() throws Exception {
        assertEquals(2, em.createQuery("select count(e) from User e", Number.class).getSingleResult().intValue());

        transactionalCaller.call(new Callable() {
            public Object call() throws Exception {
                seriouslyYouAlreadyForgotOpenEJB_questionMark();
                return null;
            }
        });
    }
    
    public static interface Caller {
        public <V> V call(Callable<V> callable) throws Exception;
    }
    
    @Stateless
    @TransactionAttribute(REQUIRES_NEW)
    public static class TransactionBean implements Caller {

        public <V> V call(Callable<V> callable) throws Exception {
            return callable.call();
        }
    }
}
