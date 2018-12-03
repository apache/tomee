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
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MoviesBusinessBean implements SessionBean {

    private SessionContext ctx;

    @Override
    public void ejbActivate() throws EJBException, RemoteException {
    }

    @Override
    public void ejbPassivate() throws EJBException, RemoteException {
    }

    @Override
    public void ejbRemove() throws EJBException, RemoteException {
    }

    @Override
    public void setSessionContext(final SessionContext ctx) throws EJBException, RemoteException {

        this.ctx = ctx;
    }


    public int addMovie(final String title, final String director, int year) throws MovieException {
        try {
            final InitialContext context = new InitialContext();
            final MovieLocalHome home = (MovieLocalHome)
                    PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/MovieBean"), MovieLocalHome.class);

            final Movie movie = home.create(director, title, year);
            return movie.getId();

        } catch (NamingException | CreateException e) {
            throw new MovieException(e);
        }
    }

    public void addActor(final int movieId, final String firstName, final String lastName) throws MovieException {
        try {
            final InitialContext context = new InitialContext();
            final MovieLocalHome home = (MovieLocalHome)
                PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/MovieBean"), MovieLocalHome.class);

            final Movie movie = home.findByPrimaryKey(movieId);
            movie.addActor(firstName, lastName);
        } catch (NamingException | FinderException e) {
            throw new MovieException(e);
        }
    }

    public MovieVO findByPrimaryKey(final int id) throws MovieException {
        try {
            final InitialContext context = new InitialContext();
            final MovieLocalHome home = (MovieLocalHome)
                    PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/MovieBean"), MovieLocalHome.class);


            return MovieVO.from(home.findByPrimaryKey(id));
        } catch (NamingException | FinderException e) {
             throw new MovieException(e);
        }
    }

    public Collection findAll() throws MovieException {
        try {
            final InitialContext context = new InitialContext();
            final MovieLocalHome home = (MovieLocalHome)
                    PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/MovieBean"), MovieLocalHome.class);

            final Collection movies = home.findAll();

            final Collection result = new ArrayList();
            final Iterator iterator = movies.iterator();
            while (iterator.hasNext()) {
                Movie movie = (Movie) iterator.next();
                result.add(MovieVO.from(movie));
            }

            return result;
        } catch (NamingException | FinderException e) {
            throw new MovieException(e);
        }
    }

    public void delete(Integer id) throws MovieException {
        try {
            final InitialContext context = new InitialContext();
            final MovieLocalHome home = (MovieLocalHome)
                    PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/MovieBean"), MovieLocalHome.class);

            home.remove(id);
        } catch (NamingException | RemoveException e) {
            throw new MovieException(e);
        }
    }
}
