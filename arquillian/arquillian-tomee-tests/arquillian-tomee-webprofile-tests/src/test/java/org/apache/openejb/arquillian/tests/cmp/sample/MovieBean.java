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
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;


public abstract class MovieBean implements EntityBean {
    private EntityContext context;

    public abstract Integer getMovieId();

    public abstract void setMovieId(Integer id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getGenre();

    public abstract void setGenre(String city);

    public abstract Collection getActors();

    public abstract void setActors(Collection actors);

    public ArrayList getCopyOfActors() {
        final ArrayList actorList = new ArrayList();
        final Collection actors = getActors();

        final Iterator i = actors.iterator();

        while (i.hasNext()) {
            final LocalActor actor = (LocalActor) i.next();
            final ActorDetails details =
                new ActorDetails(actor.getActorId(), actor.getName());

            actorList.add(details);
        }

        return actorList;
    }

    public void addActor(final LocalActor player) {
        try {
            final Collection actors = getActors();

            actors.add(player);
        } catch (final Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void removeActor(final LocalActor actor) {
        try {
            final Collection players = getActors();

            players.remove(actor);
        } catch (final Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public String ejbCreate(final String name, final String genre)
        throws CreateException {
        setName(name);
        setGenre(genre);

        return null;
    }

    public void ejbPostCreate(final String name, final String genre)
        throws CreateException {
    }

    public void setEntityContext(final EntityContext ctx) {
        context = ctx;
    }

    public void unsetEntityContext() {
        context = null;
    }

    public void ejbRemove() {
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbPassivate() {
    }

    public void ejbActivate() {
    }
}
