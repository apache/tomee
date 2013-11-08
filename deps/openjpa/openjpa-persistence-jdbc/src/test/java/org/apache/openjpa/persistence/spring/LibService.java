/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.spring;

import org.apache.openjpa.persistence.models.library.*;

/**
 * The Spring library example is intended to mirror the behavior of an
 * application that uses the Spring Framework's JPA transaction manager and
 * annotations. In this case, the Spring Framework's transactional handling,
 * through its advice and proxies, causes the entity manager to be closed after
 * each call to the implementation of the service (a.k.a. DAO) interface. These
 * observations are based on Spring version 2.5.5.
 * <p>
 * It is likely that other containers behave in a similar manner.
 * <p>
 * In particular, it mirrors the behavior when there is a Spring annotated
 * service interface that looks like the following:
 * 
 * <pre>
 *    (at)Transactional(propagation=Propagation.SUPPORTS)
 *    public interface LibService
 *       {
 *       public Borrower findBorrowerByName(String name);
 *       public Book findBookByTitle(String title);
 *        
 *       (at)Transactional(propagation=Propagation.REQUIRED)
 *       public void borrowBook(Borrower borrower, Book book);
 *        
 *       (at)Transactional(propagation=Propagation.REQUIRED) 
 *       public void returnBook(Book book);
 *       }
 * </pre>
 * <p>
 * And there is a Spring configuration file with the following entries:
 * 
 * <pre>
 *    &lt;bean id=&quot;emf&quot; 
 *   class=&quot;org.springframework.orm.jpa.LocalEntityManagerFactoryBean&quot;
 *   &gt;
 *    &lt;property name=&quot;persistenceUnitName&quot; value=&quot;&quot; /&gt;
 *    &lt;/bean&gt;
 *  
 *    &lt;!--  enable Spring's support for JPA injection --&gt;
 *    &lt;bean class=&quot;
 *org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor
 *&quot;/&gt;
 *              
 *    &lt;bean id=&quot;transactionalService&quot; 
 *          class=&quot;sample.jpa.service.LibServiceImpl&quot; &gt;
 *    &lt;/bean&gt;
 *  
 *    &lt;bean id=&quot;transactionManager&quot; 
 *      class=&quot;org.springframework.orm.jpa.JpaTransactionManager&quot; &gt;
 *  &lt;property name=&quot;entityManagerFactory&quot; ref=&quot;emf&quot; /&gt;
 *    &lt;/bean&gt;
 *  
 *    &lt;tx:annotation-driven/&gt;
 * </pre>
 * <p>
 * And the declaration of the entity manager in the service implementation is
 * annotated with the <b>(at)PersistenceContext</b> annotation.
 * 
 * @author David Ezzio
 */

public interface LibService {
    public Borrower findBorrowerByName(String name);

    public Book findBookByTitle(String title);

    public void borrowBook(Borrower borrower, Book book);

    public void returnBook(Book book);

    public void setTransactionalEntityManagerFactory(
            TransactionalEntityManagerFactory txEMF);
}
