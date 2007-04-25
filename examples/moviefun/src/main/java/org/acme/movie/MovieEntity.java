package org.acme.movie;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Date;
import java.util.Collection;
import java.util.Properties;
import java.text.SimpleDateFormat;

/**
 * This is actually our EJBs Local interface.
 *
 * Since local interfaces don't declare crazy EJB specific Exceptions
 * in the throws clause we will choose to put our method declarations
 * in an interface called Movie and make both the Local interface and
 * Bean class implement it.
 *
 * @author David Blevins <dblevins@visi.com>
 */
public interface MovieEntity extends javax.ejb.EJBLocalObject, Movie {

    /** This EJB's JNDI name */
    public static final String NAME = "MovieEJBLocal";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");

    /**
     * This is another fun trick.  I prefer this to the standard service
     * locator pattern.  It makes for easy and neat looking entity creation
     * and location, such as:
     *
     *   MovieEntity movie = MovieEntity.Home.create(...);
     *
     * A home interface is essentially static, so why not really
     * make it static.
     *
     * The easiest way to implement the MovieHome interface for this class
     * declare the variable "private MovieHome home;" then use IntelliJ or
     * Eclipse's "Delegate Methods" support.
     */
    public static final MovieHome Home = new MovieHome(){
        private MovieHome home;
        private MovieHome home(){
            if (home == null){
                home = lookup();
            }
            return home;
        }
        private MovieHome lookup() {
            try {
                Properties p = new Properties(System.getProperties());
                p.put("java.naming.factory.initial", "org.openejb.client.LocalInitialContextFactory");
                InitialContext initialContext = new InitialContext(p);
                return (MovieHome) initialContext.lookup(NAME);
            } catch (NamingException e) {
                throw (IllegalStateException) new IllegalStateException(NAME + " cannot be retrieved from JNDI.").initCause(e);
            }
        }

        public MovieEntity create(String title, String director, String genre, int rating, Date releaseDate) throws CreateException {
            return home().create(title, director, genre, rating, releaseDate);
        }

        public Collection findByTitle(String title) throws FinderException {
            return home().findByTitle(title);
        }

        public Collection findByDirector(String director) throws FinderException {
            return home().findByDirector(director);
        }

        public Collection findByGenre(String genre) throws FinderException {
            return home().findByGenre(genre);
        }

        public MovieEntity findByPrimaryKey(Integer key) throws FinderException {
            return home().findByPrimaryKey(key);
        }

        public Collection findAllMovies() throws FinderException {
            return home().findAllMovies();
        }

        public void remove(Object o) throws RemoveException, EJBException {
            home().remove(o);
        }
    };

}
