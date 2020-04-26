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
package org.superbiz.injection.tx;

import org.junit.Assert;
import org.junit.Test;

import jakarta.ejb.EJB;
import jakarta.ejb.embeddable.EJBContainer;
import java.util.Properties;

public class MoviesTest {

    @EJB
    private Movies movies;

    @Test
    public void testMe() throws Exception {
        final Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        EJBContainer.createEJBContainer(p).getContext().bind("inject", this);

        movies.addMovie(new Movie("Asif Kapadia", "Senna", 2010));
        movies.addMovie(new Movie("José Padilha", "Tropa de Elite", 2007));
        movies.addMovie(new Movie("Andy Wachowski/Lana Wachowski", "The Matrix", 1999));
        movies.addMovie(new Movie("Quentin Tarantino", "Reservoir Dogs", 1992));
        movies.addMovie(new Movie("Joel Coen", "Fargo", 1996));
        movies.addMovie(new Movie("Joel Coen", "The Big Lebowski", 1998));

        Assert.assertEquals(5L, movies.countMovies().longValue());
    }

}
