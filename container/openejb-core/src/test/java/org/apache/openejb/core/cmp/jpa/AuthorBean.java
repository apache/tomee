/**
 *
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
package org.apache.openejb.core.cmp.jpa;

import org.apache.openejb.core.cmp.cmp2.Cmp2Entity;
import org.apache.openejb.core.cmp.cmp2.SetValuedCmr;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.DeploymentContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.SystemException;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import java.util.Set;
import java.util.HashSet;

public class AuthorBean implements EntityBean, Cmp2Entity {
    public static Object deploymentInfo;
    static {
        try {
            deploymentInfo = new CoreDeploymentInfo(new DeploymentContext("author", Author.class.getClassLoader(), null),
                    AuthorBean.class,
                    null,
                    null,
                    AuthorHome.class,
                    Author.class,
                    null,
                    null,
                    String.class, 
                    BeanType.CMP_ENTITY);
            ((CoreDeploymentInfo)deploymentInfo).createMethodMap();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private transient boolean deleted;

    private String name;
    private Set<BookBean> books = new HashSet<BookBean>();
    private transient SetValuedCmr booksCmr = new SetValuedCmr(this, "books", BookBean.class, "authors");

    public AuthorBean() {
    }

    public AuthorBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set getBooks() {
        return booksCmr.get(books);
    }

    public void setBooks(Set books) {
        booksCmr.set(this.books, books);
    }

    public Object OpenEJB_getPrimaryKey() {
        return name;
    }

    public void OpenEJB_deleted() {
        if (deleted) return;
        deleted = true;

        booksCmr.deleted(books);
    }

    public Object OpenEJB_addCmr(String name, Object bean) {
        if (deleted) return null;

        if ("books".equals(name)) {
            books.add((BookBean) bean);
            return null;
        }

        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }

    public void OpenEJB_removeCmr(String name, Object value) {
        if (deleted) return;

        if ("books".equals(name)) {
            books.remove(value);
            return;
        }

        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }

    public void ejbActivate() {
    }

    public void ejbLoad() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    public void ejbStore() {
    }

    public void setEntityContext(EntityContext entityContext) {
    }

    public void unsetEntityContext() {
    }
}
