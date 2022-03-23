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
package org.superbiz.injection.jpa;

import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class MoviesTest {

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Test
    public void test() throws Exception {
        final Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        final EJBContainer container = EJBContainer.createEJBContainer(p);
        final Context context = container.getContext();
        context.bind("inject", this);

        assertTrue(((ReloadableEntityManagerFactory) emf).getManagedClasses().contains(Movie.class.getName()));

        container.close();
    }
}
