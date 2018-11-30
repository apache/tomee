package org.apache.openejb.arquillian.tests.cmp;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.LocalHome;
import javax.ejb.RemoteHome;
import javax.ejb.RemoveException;

@LocalHome(MyLocalHome.class)
@RemoteHome(MyRemoteHome.class)
public abstract class MyCmpBean implements EntityBean {

    // CMP
    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract String getName();

    public abstract void setName(String number);

    public void doit() {
    }

    public Integer ejbCreateObject(final String id) throws CreateException {
        return null;
    }

    public void ejbPostCreateObject(final String id) {
    }

    public void setEntityContext(final EntityContext ctx) {
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
}