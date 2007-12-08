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
package org.superbiz.injection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

@Stateful(name = "Movies")
public class MoviesImpl implements Movies {

    /**
     * The field name "movieDatabase" matches the DataSource we
     * configure in the TestCase via :
     * p.put("movieDatabase", "new://Resource?type=DataSource");
     *
     * This would also match an equivalent delcaration in an openejb.xml:
     * <Resource id="movieDatabase" type="DataSource"/>
     *
     * If you'd like the freedom to change the field name without
     * impact on your configuration you can set the "name" attribute
     * of the @Resource annotation to "movieDatabase" instead.
     */
    @Resource
    private DataSource movieDatabase;

    @PostConstruct
    private void construct() throws Exception {
        Connection connection = movieDatabase.getConnection();
        try {
            PreparedStatement stmt = connection.prepareStatement("CREATE TABLE movie ( director VARCHAR(255), title VARCHAR(255), year integer)");
            stmt.execute();
        } finally {
            connection.close();
        }
    }

    public void addMovie(Movie movie) throws Exception {
        Connection conn = movieDatabase.getConnection();
        try {
            PreparedStatement sql = conn.prepareStatement("INSERT into movie (director, title, year) values (?, ?, ?)");
            sql.setString(1, movie.getDirector());
            sql.setString(2, movie.getTitle());
            sql.setInt(3, movie.getYear());
            sql.execute();
        } finally {
            conn.close();
        }
    }

    public void deleteMovie(Movie movie) throws Exception {
        Connection conn = movieDatabase.getConnection();
        try {
            PreparedStatement sql = conn.prepareStatement("DELETE from movie where director = ? AND title = ? AND year = ?");
            sql.setString(1, movie.getDirector());
            sql.setString(2, movie.getTitle());
            sql.setInt(3, movie.getYear());
            sql.execute();
        } finally {
            conn.close();
        }
    }

    public List<Movie> getMovies() throws Exception {
        ArrayList<Movie> movies = new ArrayList<Movie>();
        Connection conn = movieDatabase.getConnection();
        try {
            PreparedStatement sql = conn.prepareStatement("SELECT director, title, year from movie");
            ResultSet set = sql.executeQuery();
            while ( set.next() ) {
                Movie movie = new Movie();
                movie.setDirector(set.getString("director"));
                movie.setTitle(set.getString("title"));
                movie.setYear(set.getInt("year"));
                movies.add( movie );
            }

        } finally {
            conn.close();
        }
        return movies;
    }

}
