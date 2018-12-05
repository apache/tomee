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
        ArrayList actorList = new ArrayList();
        Collection actors = getActors();

        Iterator i = actors.iterator();

        while (i.hasNext()) {
            LocalActor actor = (LocalActor) i.next();
            ActorDetails details =
                new ActorDetails(actor.getActorId(), actor.getName());

            actorList.add(details);
        }

        return actorList;
    }

    public void addActor(LocalActor player) {
        try {
            Collection actors = getActors();

            actors.add(player);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public void removeActor(LocalActor actor) {
        try {
            Collection players = getActors();

            players.remove(actor);
        } catch (Exception ex) {
            throw new EJBException(ex.getMessage());
        }
    }

    public String ejbCreate(String name, String genre)
        throws CreateException {
        setName(name);
        setGenre(genre);

        return null;
    }

    public void ejbPostCreate(String name, String genre)
        throws CreateException {
    }

    public void setEntityContext(EntityContext ctx) {
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
