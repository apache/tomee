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
package org.superbiz.jpa.enums;

import junit.framework.TestCase;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.Properties;

public class MoviesTest extends TestCase {

    public void test() throws Exception {

        final Properties p = new Properties();
        p.put("movieDatabase", "new://Resource?type=DataSource");
        p.put("movieDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("movieDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        EJBContainer container = EJBContainer.createEJBContainer(p);
        final Context context = container.getContext();

        final Movies movies = (Movies) context.lookup("java:global/jpa-enumerated/Movies");

        movies.addMovie(new Movie("James Frawley", "The Muppet Movie", 1979, Rating.G));
        movies.addMovie(new Movie("Jim Henson", "The Great Muppet Caper", 1981, Rating.G));
        movies.addMovie(new Movie("Frank Oz", "The Muppets Take Manhattan", 1984, Rating.G));
        movies.addMovie(new Movie("James Bobin", "The Muppets", 2011, Rating.PG));

        assertEquals("List.size()", 4, movies.getMovies().size());

        assertEquals("List.size()", 3, movies.findByRating(Rating.G).size());

        assertEquals("List.size()", 1, movies.findByRating(Rating.PG).size());

        assertEquals("List.size()", 0, movies.findByRating(Rating.R).size());

        container.close();
    }
}
