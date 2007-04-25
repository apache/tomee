package org.acme.movie;

import javax.ejb.CreateException;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import java.util.Date;

/**
 * Bean class for the Movie EJB
 *
 * @author David Blevins <dblevins@visi.com>
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
