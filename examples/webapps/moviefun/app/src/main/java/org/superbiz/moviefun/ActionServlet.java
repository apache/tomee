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

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @version $Revision$ $Date$
 */
public class ActionServlet extends HttpServlet {

    @EJB(name = "movies")
    private Movies moviesBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        List<Movie> movies = null;
        ListIterator<Movie> listIterator = null;
        int display = 5;

        String action = request.getParameter("action");


        if ("Add".equals(action)) {

            String title = request.getParameter("title");
            String director = request.getParameter("director");
            String genre = request.getParameter("genre");
            int rating = Integer.parseInt(request.getParameter("rating"));
            int year = Integer.parseInt(request.getParameter("year"));

            Movie movie = new Movie(title, director, genre, rating, year);

            moviesBean.addMovie(movie);

        } else if ("Remove".equals(action)) {

            String[] ids = request.getParameterValues("id");
            for (String id : ids) {
                moviesBean.deleteMovieId(new Long(id));
            }

        } else if (">>".equals(action)) {

            movies = (List) session.getAttribute("movies.collection");
            listIterator = (ListIterator) session.getAttribute("movies.iterator");

        } else if ("<<".equals(action)) {

            movies = (List) session.getAttribute("movies.collection");
            listIterator = (ListIterator) session.getAttribute("movies.iterator");
            for (int i = display * 2; i > 0 && listIterator.hasPrevious(); i--) {
                listIterator.previous(); // backup
            }

        } else if ("findByTitle".equals(action)) {

            movies = moviesBean.findByTitle(request.getParameter("key"));

        } else if ("findByDirector".equals(action)) {

            movies = moviesBean.findByDirector(request.getParameter("key"));

        } else if ("findByGenre".equals(action)) {

            movies = moviesBean.findByGenre(request.getParameter("key"));
        }

        if (movies == null) {
            try {
                movies = moviesBean.getMovies();
            } catch (Throwable e) {
                // We must not have run setup yet
                response.sendRedirect("setup.jsp");
                return;
            }
        }

        if (listIterator == null) {
            listIterator = movies.listIterator();
        }

        session.setAttribute("movies.collection", movies);
        session.setAttribute("movies.iterator", listIterator);

        List<Movie> moviesToShow = new ArrayList<Movie>();

        boolean hasPrevious = listIterator.hasPrevious();

        int start = listIterator.nextIndex();

        for (int i = display; i > 0 && listIterator.hasNext(); i--) {
            Movie movie = (Movie) listIterator.next();
            moviesToShow.add(movie);
        }

        boolean hasNext = listIterator.hasNext();

        int end = listIterator.nextIndex();
        request.setAttribute("movies", moviesToShow);
        request.setAttribute("start", start);
        request.setAttribute("end", end);
        request.setAttribute("total", movies.size());
        request.setAttribute("display", display);
        request.setAttribute("hasNext", hasNext);
        request.setAttribute("hasPrev", hasPrevious);

        request.getRequestDispatcher("WEB-INF/moviefun.jsp").forward(request, response);
    }

}
