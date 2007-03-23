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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.cmr.manytomany;

import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import org.apache.openejb.test.entity.cmr.CompoundPK;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public abstract class PlatformBean implements EntityBean {
    // CMP
    public abstract Integer getId();
    public abstract void setId(Integer id);

    public abstract String getName();
    public abstract void setName(String name);
    
    // CMR
    public abstract Set<GameLocal> getGames();
    public abstract void setGames(Set<GameLocal> gameSets);
    
    public Integer ejbCreate(Integer id)  throws CreateException {
        setId(id);
        return null;
    }

    public void ejbPostCreate(Integer id) {
    }

    public CompoundPK ejbCreate(PlatformPk primaryKey)  throws CreateException {
        setId(primaryKey.id);
        setName(primaryKey.name);
        return null;
    }

    public void ejbPostCreate(PlatformPk primaryKey) {
    }

    public void setEntityContext(EntityContext ctx) {
    }

    public void unsetEntityContext() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() throws RemoveException {
    }

    public String toString() {
        return "[Platform " + getId() + " name " + getName() + "]";
    }
}
