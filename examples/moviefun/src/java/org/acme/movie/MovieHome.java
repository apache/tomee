package org.acme.movie;

import javax.ejb.FinderException;
import javax.ejb.CreateException;
import java.util.Collection;
import java.util.Date;

/**
 * EJB Local Home interface for the Movie EJB
 *
 * @author David Blevins <dblevins@visi.com>
 */
public interface MovieHome extends javax.ejb.EJBLocalHome {

    public MovieEntity create(String title, String director, String genre, int rating, Date releaseDate) throws CreateException;

    public Collection findByTitle(String title) throws FinderException;

    public Collection findByDirector(String director) throws FinderException;

    public Collection findByGenre(String genre) throws FinderException;

    public MovieEntity findByPrimaryKey(Integer key) throws FinderException;

    public Collection findAllMovies() throws FinderException;

}
