package org.acme.movie;

import java.util.Date;

/**
 * Business interface for the Movie EJB
 *
 * @author David Blevins <dblevins@visi.com>
 */
public interface Movie {
    Integer getMovieId();

    String getTitle();

    void setTitle(String title);

    String getDirector();

    void setDirector(String director);

    String getGenre();

    void setGenre(String genre);

    int getRating();

    void setRating(int rating);

    Date getReleaseDate();

    void setReleaseDate(Date releaseDate);
}
