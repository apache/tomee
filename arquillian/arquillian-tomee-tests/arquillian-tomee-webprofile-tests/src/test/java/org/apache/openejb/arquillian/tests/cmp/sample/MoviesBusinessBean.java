/*
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
package org.apache.openejb.arquillian.tests.cmp.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;

public class MoviesBusinessBean implements SessionBean {
    public void doLogic() {
        try {
            final Context initial = new InitialContext();

            final LocalActorHome actorHome = (LocalActorHome) initial.lookup("java:comp/env/ejb/Actor");
            final Context initial1 = new InitialContext();

            final LocalMovieHome movieHome = (LocalMovieHome) initial1.lookup("java:comp/env/ejb/Movie");

            final LocalMovie movie = movieHome.create("Bad Boys", "Action Comedy");

            final LocalActor actor1 = actorHome.create("Will Smith");
            final LocalActor actor2 = actorHome.create("Martin Lawrence");

            movie.addActor(actor1);
            movie.addActor(actor2);
        } catch (final Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void ejbCreate() throws CreateException {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    public void setSessionContext(final SessionContext sc) {
    }

    private ArrayList copyActorsToDetails(final Collection actors) {
        final ArrayList detailsList = new ArrayList();
        final Iterator i = actors.iterator();

        while (i.hasNext()) {
            final LocalActor player = (LocalActor) i.next();
            final ActorDetails details =
                new ActorDetails(player.getActorId(), player.getName());

            detailsList.add(details);
        }

        return detailsList;
    }
}
