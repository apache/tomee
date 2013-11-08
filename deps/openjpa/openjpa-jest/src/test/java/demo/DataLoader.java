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

package demo;

import java.util.Date;

import javax.persistence.EntityManager;

/**
 * Loads some example Actor-Movie data.
 *  
 * @author Pinaki Poddar
 *
 */
public class DataLoader {
    // Hand-tuned data for Testing
    @SuppressWarnings("deprecation")
    public static Object[][] ACTOR_DATA = {
        new Object[] {"m1", "Robert", "Redford",  Actor.Gender.Male, new Date(50, 1, 12)},
        new Object[] {"m2", "Robert", "De Niro",  Actor.Gender.Male, new Date(40, 4, 14)},
        new Object[] {"m3", "Al",     "Pacino",   Actor.Gender.Male, new Date(50, 1, 12)},
        new Object[] {"m4", "Jack",   "Nichelson",Actor.Gender.Male, new Date(40, 4, 14)},
        new Object[] {"m5", "Clint",  "Eastwood", Actor.Gender.Male, new Date(50, 1, 12)},
        
        new Object[] {"f1", "Meryl",   "Streep",    Actor.Gender.Female, new Date(40, 4, 14)},
        new Object[] {"f2", "Fay",     "Dunaway",   Actor.Gender.Female, new Date(50, 1, 12)},
        new Object[] {"f3", "Jodie",   "Foster",    Actor.Gender.Female, new Date(40, 4, 14)},
        new Object[] {"f4", "Diane",   "Keaton",    Actor.Gender.Female, new Date(50, 1, 12)},
        new Object[] {"f5", "Catherine", "Hepburn", Actor.Gender.Female, new Date(40, 4, 14)},
    };
    
    public static Object[][] MOVIE_DATA = {
        new Object[] {"1", "China Town", 1980},
        new Object[] {"2", "Taxi Driver", 1980},
        new Object[] {"3", "Where Eagles Dare", 1980},
        new Object[] {"4", "Godfather", 1980},
        new Object[] {"5", "Horse Whisperer", 1980},
    };
    
    public static int[][] MOVIE_ACTORS = {
        new int[] {3,6},
        new int[] {1,7},
        new int[] {4},
        new int[] {2,3,8},
        new int[] {0}
    };
    
    public static int[][] PARTNERS = {
        new int[] {3,6},
        new int[] {1,7},
        new int[] {3,8},
    };

    public void populate(EntityManager em) throws Exception {
        Long count = em.createQuery("select count(m) from Movie m", Long.class).getSingleResult();
        if (count != null && count.longValue() > 0) {
            System.err.println("Found " + count + " Movie records in the database");
            return;
        }
        
        
        Actor[] actors = createActors();
        Movie[] movies = createMovies();
        linkActorAndMovie(movies, actors);
        makePartner(actors);
        em.getTransaction().begin();
        for (Actor a : actors) {
            em.persist(a);
        }
        for (Movie m : movies) {
            em.persist(m);
        }
        em.getTransaction().commit();
    }
    
    Actor[] createActors() {
        Actor[] actors = new Actor[ACTOR_DATA.length];
        for (int i = 0; i < ACTOR_DATA.length; i++) {
            Object[] a = ACTOR_DATA[i];
            actors[i] = new Actor((String)a[0], (String)a[1], (String)a[2], (Actor.Gender)a[3], (Date)a[4]);
        }
        return actors;
    }
    
    Movie[] createMovies() {
        Movie[] movies = new Movie[MOVIE_DATA.length];
        for (int i = 0; i < MOVIE_DATA.length; i++) {
            Object[] m = MOVIE_DATA[i];
            movies[i] = new Movie((String)m[0], (String)m[1], (Integer)m[2]);
        }
        return movies;
    }
    
    void linkActorAndMovie(Movie[] movies, Actor[] actors) {
        for (int i = 0; i < MOVIE_ACTORS.length; i++) {
            int[] roles = MOVIE_ACTORS[i];
            Movie m = movies[i];
            for (int j = 0; j < roles.length; j++) {
                Actor a = actors[roles[j]];
                a.addMovie(m);
                m.addActor(a);
            }
        }
    }
    
    void makePartner(Actor[] actors) {
        for (int i = 0; i < PARTNERS.length; i++) {
            int[] partners = PARTNERS[i];
            Actor a1 = actors[partners[0]];
            Actor a2 = actors[partners[1]];
            a1.setPartner(a2);
            a2.setPartner(a1);
       }
    }
    
}
