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
package org.superbiz.moviefun;

import javax.ejb.CreateException;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import java.util.Date;

/**
 * Bean class for the Movie EJB
 *
 * @author David Blevins <dblevins@visi.com>
 * 
 * @version $Rev$ $Date$
 */
public class MovieBean implements javax.ejb.EntityBean, Movie {

    private EntityContext entityContext;
    public Integer movieId;
    public String title;
    public String director;
    public String genre;
    public int rating;
    public Date releaseDate;

    public Integer ejbCreate(String title, String director, String genre, int rating, Date releaseDate) throws CreateException {
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.rating = rating;
        this.releaseDate = releaseDate;
        return null;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void ejbPostCreate(String title, String director, String genre, int rating, Date releaseDate) throws CreateException {
    }

    public void ejbActivate() {
    }

    public void ejbLoad() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() throws RemoveException {
    }

    public void ejbStore() {
    }

    public void setEntityContext(EntityContext entityContext) {
        this.entityContext = entityContext;
    }

    public void unsetEntityContext() {
        this.entityContext = null;
    }
}
