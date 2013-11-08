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
package org.apache.openjpa.jdbc.persistence.classcriteria;

import java.util.Collection;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestQuerySubclass extends SingleEMFTestCase {

    private OpenJPAEntityManager em;

    public void setUp() {
        setUp("openjpa.Compatibility",
            "superclassDiscriminatorStrategyByDefault=true", CLEAR_TABLES,
            Artist.class, Book.class, Item.class, Movie.class);
        em = emf.createEntityManager();
        em.getTransaction().begin();
        Book b = new Book("book");
        Movie m = new Movie("movie");
        Artist a = new Artist("Herman Hess");
        b.setArtist(a);
        m.setArtist(a);
        em.persist(a);
        em.persist(b);
        em.persist(m);
        em.getTransaction().commit();

    }

    public void testQuery() {
        Collection<Book> books = null;
        Collection<Movie> movies = null;
        em = emf.createEntityManager();
        Artist artist = em.find(Artist.class, "Herman Hess");
        if (artist == null) {
            System.out.println("No artist found with ID Herman Hess");
        }
        else {
            books = artist.getBooks();
            movies = artist.getMovies();
        }
        assertEquals(1, books.size());
        assertEquals(1, movies.size());
    }
}
