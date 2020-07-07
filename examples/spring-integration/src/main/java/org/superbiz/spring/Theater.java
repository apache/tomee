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

import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

//START SNIPPET: code

/**
 * Spring bean that references the Movies EJB and the Movie JPA bean.
 * <p/>
 * This bean shows that Spring beans can have references to EJBs.
 */
public class Theater {

    /**
     * The Movies @Stateless EJB
     */
    private final Movies movies;

    private final List<Movie> nowPlaying = new ArrayList<Movie>();

    /**
     * The Movies EJB is passed in on the constructor which
     * guarantees we can use it in the setNowPlaying method.
     *
     * @param movies
     */
    @Inject @Named(value = "MoviesLocal")
    public Theater(Movies movies) {
        this.movies = movies;
    }

    /**
     * For every title in the list we will use the Movies EJB
     * to lookup the actual Movie JPA object.
     *
     * @param nowPlaying
     * @throws Exception
     */
    public void setNowPlaying(List<String> nowPlaying) throws Exception {
        for (String title : nowPlaying) {
            this.nowPlaying.add(movies.getMovieByTitle(title));
        }
    }

    public List<Movie> getMovies() throws Exception {
        return nowPlaying;
    }
}
//END SNIPPET: code

