/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.cmp.sample;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

public class MovieServlet extends HttpServlet {


    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        final PrintWriter pw = resp.getWriter();

        try {
            final InitialContext context = new InitialContext();
            final MoviesBusinessLocalHome home = (MoviesBusinessLocalHome)
                    PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/MoviesBusiness"), MoviesBusinessLocalHome.class);

            final MoviesBusinessLocal bean = home.create();

            bean.addMovie("Bad Boys", "Michael Bay", 1995);

            pw.println("Movie added successfully");

            final Collection allMovies = bean.findAll();

            final Iterator iterator = allMovies.iterator();
            while (iterator.hasNext()) {
                final MovieVO movie = (MovieVO) iterator.next();
                pw.println(movie.toString());

                bean.delete(movie.getId());
                pw.println("Movie removed successfully");
            }

            bean.remove();
            pw.flush();

        } catch (NamingException | CreateException | RemoveException | MovieException e) {
            throw new ServletException(e);
        }
    }
}
