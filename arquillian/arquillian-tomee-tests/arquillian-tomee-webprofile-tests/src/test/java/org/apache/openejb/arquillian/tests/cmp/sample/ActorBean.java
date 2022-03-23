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

import java.util.Collection;
import jakarta.ejb.CreateException;
import jakarta.ejb.EntityBean;
import jakarta.ejb.EntityContext;


public abstract class ActorBean implements EntityBean {
    private EntityContext context;

    public abstract Integer getActorId();

    public abstract void setActorId(Integer id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Collection getMovies();

    public abstract void setMovies(Collection movies);

    public String ejbCreate(final String name) throws CreateException {
        setName(name);
        return null;
    }

    public void ejbPostCreate(final String name) throws CreateException {
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
