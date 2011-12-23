package org.superbiz.osgi.moviefun;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class Bootstrap {
    @EJB
    private Movies movies;

    @PostConstruct
    public void init() {
        try {
            movies.addMovie(new Movie("OpenEJB", "OpenEJB in OSGi", 2011));
            System.out.println("found " + movies.getMovies().size() + " movies.");
        } catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
        }
    }
}
