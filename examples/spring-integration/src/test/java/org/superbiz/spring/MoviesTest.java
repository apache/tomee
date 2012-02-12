/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.spring;

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

//START SNIPPET: code
public class MoviesTest extends TestCase {

    public void test() throws Exception {

        //Uncomment for debug logging
        //org.apache.log4j.BasicConfigurator.configure();

        System.setProperty("openejb.deployments.classpath.include", "spring-integration");
        System.setProperty("openejb.exclude-include.order", "exclude-include");

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("movies.xml");

        // Can I lookup the Cineplex EJB via the Spring ApplicationContext
        Cineplex cineplex = (Cineplex) context.getBean("CineplexImplLocal");
        assertNotNull(cineplex);

        // Does the Cineplex EJB have a reference to the Theaters Spring bean?
        List<Theater> theaters = cineplex.getTheaters();
        assertNotNull(theaters);

        assertEquals(2, theaters.size());

        Theater theaterOne = theaters.get(0);
        Theater theaterTwo = theaters.get(1);


        // Were the Theater Spring beans able to use the
        // Movies EJB to get references to the Movie JPA objects?
        List<Movie> theaterOneMovies = theaterOne.getMovies();
        assertNotNull(theaterOneMovies);

        List<Movie> theaterTwoMovies = theaterTwo.getMovies();
        assertNotNull(theaterTwoMovies);

        // The first Theater should have used the Movies EJB
        // to get a reference to three Movie JPA objects
        assertEquals(3, theaterOneMovies.size());

        assertEquals("Fargo", theaterOneMovies.get(0).getTitle());
        assertEquals("Reservoir Dogs", theaterOneMovies.get(1).getTitle());
        assertEquals("The Big Lebowski", theaterOneMovies.get(2).getTitle());

        // The second Theater should have used the Movies EJB
        // to get a reference to four Movie JPA objects

        assertEquals(4, theaterTwoMovies.size());

        assertEquals("You, Me and Dupree", theaterTwoMovies.get(0).getTitle());
        assertEquals("Wedding Crashers", theaterTwoMovies.get(1).getTitle());
        assertEquals("Zoolander", theaterTwoMovies.get(2).getTitle());
        assertEquals("Shanghai Noon", theaterTwoMovies.get(3).getTitle());
    }
}
//END SNIPPET: code
