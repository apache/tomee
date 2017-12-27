package org.superbiz.model;

import javax.ejb.Local;
import java.util.List;

@Local
public interface Movies {
    Movie find(Long id);

    void addMovie(Movie movie);

    void editMovie(Movie movie);

    void deleteMovieId(long id);

    List<Movie> getMovies();

    List<Movie> findAll(int firstResult, int maxResults);

    int countAll();

    int count(String field, String searchTerm);

    List<Movie> findRange(String field, String searchTerm, int firstResult, int maxResults);

    void clean();
}
