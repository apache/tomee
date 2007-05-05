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

import org.apache.openejb.core.cmp.cmp2.SetValuedCmr;
import org.apache.openejb.core.cmp.cmp2.Cmp2Entity;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.DeploymentContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.SystemException;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import java.util.HashSet;
import java.util.Set;

public class BookBean implements EntityBean, Cmp2Entity {
    public static Object deploymentInfo;
    static {
        try {
            deploymentInfo = new CoreDeploymentInfo(new DeploymentContext("book", Book.class.getClassLoader(), null),
                    BookBean.class,
                    null,
                    null,
                    BookHome.class,
                    Book.class,
                    null, null,
                    null,
                    String.class,
                    BeanType.CMP_ENTITY);
            ((CoreDeploymentInfo)deploymentInfo).createMethodMap();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private transient boolean deleted;

    private String title;
    private Set<AuthorBean> authors = new HashSet<AuthorBean>();
    private transient SetValuedCmr authorsCmr = new SetValuedCmr(this, "authors", AuthorBean.class, "books");

    public BookBean() {
    }

    public BookBean(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set getAuthors() {
        return authorsCmr.get(authors);
    }

    public void setAuthors(Set authors) {
        authorsCmr.set(this.authors, authors);
    }

    public Object OpenEJB_getPrimaryKey() {
        return title;
    }

    public void OpenEJB_deleted() {
        if (deleted) return;
        deleted = true;

        authorsCmr.deleted(authors);
    }

    public Object OpenEJB_addCmr(String name, Object bean) {
        if (deleted) return null;

        if ("authors".equals(name)) {
            authors.add((AuthorBean) bean);
            return null;
        }

        throw new IllegalArgumentException("Unknown cmr field " + name + " on entity bean of type " + getClass().getName());
    }

    public void OpenEJB_removeCmr(String name, Object value) {
        if (deleted) return;

        if ("authors".equals(name)) {
            authors.remove(value);
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
