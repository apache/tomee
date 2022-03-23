/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.moviefun.setup;

import org.superbiz.moviefun.Movie;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ExampleDataProducer {
    @Produces
    @Examples
    public List<Movie> createSampleMovies() {
        final List<Movie> sampleMovies = new ArrayList<Movie>();
        sampleMovies.add(new Movie("Wedding Crashers", "David Dobkin", "Comedy", 7, 2005));
        sampleMovies.add(new Movie("Starsky & Hutch", "Todd Phillips", "Action", 6, 2004));
        sampleMovies.add(new Movie("Shanghai Knights", "David Dobkin", "Action", 6, 2003));
        sampleMovies.add(new Movie("I-Spy", "Betty Thomas", "Adventure", 5, 2002));
        sampleMovies.add(new Movie("The Royal Tenenbaums", "Wes Anderson", "Comedy", 8, 2001));
        sampleMovies.add(new Movie("Zoolander", "Ben Stiller", "Comedy", 6, 2001));
        sampleMovies.add(new Movie("Shanghai Noon", "Tom Dey", "Comedy", 7, 2000));
        return sampleMovies;
    }
}
