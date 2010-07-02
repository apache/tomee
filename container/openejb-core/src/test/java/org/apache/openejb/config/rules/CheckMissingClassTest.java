package org.apache.openejb.config.rules;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;


@RunWith(ValidationRunner.class)
public class CheckMissingClassTest {
  @Keys({ "missing.class", "missing.class", "missing.class", "missing.class", "missing.class", "missing.class", "missing.class", "missing.class", "missing.class", "missing.class",
      "missing.class", "missing.class", "missing.class", "missing.class", "missing.class", "missing.class" })
  public EjbJar wrongClassType() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    StatelessBean stateless = new StatelessBean(FooStateless.class);
    stateless.setHomeAndRemote("WrongHome", "WrongRemote");
    stateless.setLocal("WrongLocal");
    stateless.setLocalHome("WrongLocalHome");
    ejbJar.addEnterpriseBean(stateless);
    StatefulBean stateful = new StatefulBean(FooStateful.class);
    stateful.setHomeAndRemote("WrongHome", "WrongRemote");
    stateful.setLocal("WrongLocal");
    stateful.setLocalHome("WrongLocalHome");
    ejbJar.addEnterpriseBean(stateful);
    EntityBean bmpEntityBean = new EntityBean(FooEntityBMP.class, PersistenceType.BEAN);
    bmpEntityBean.setHome("WrongHome");
    bmpEntityBean.setLocalHome("WrongLocalHome");
    bmpEntityBean.setRemote("WrongRemote");
    bmpEntityBean.setLocal("WrongLocal");
    ejbJar.addEnterpriseBean(bmpEntityBean);
    EntityBean cmpEntityBean = new EntityBean(FooEntityCMP.class, PersistenceType.CONTAINER);
    cmpEntityBean.setHome("WrongHome");
    cmpEntityBean.setLocalHome("WrongLocalHome");
    cmpEntityBean.setRemote("WrongRemote");
    cmpEntityBean.setLocal("WrongLocal");
    ejbJar.addEnterpriseBean(cmpEntityBean);
    return ejbJar;
  }

  private static class FooStateless implements SessionBean {
    public void ejbCreate() {}

    @Override
    public void ejbActivate() throws EJBException, RemoteException {}

    @Override
    public void ejbPassivate() throws EJBException, RemoteException {}

    @Override
    public void ejbRemove() throws EJBException, RemoteException {}

    @Override
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {}
  }

  private static class FooStateful implements SessionBean {
    public void ejbCreate() {}

    @Override
    public void ejbActivate() throws EJBException, RemoteException {}

    @Override
    public void ejbPassivate() throws EJBException, RemoteException {}

    @Override
    public void ejbRemove() throws EJBException, RemoteException {}

    @Override
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {}
  }

  private static class FooEntityBMP implements javax.ejb.EntityBean {
    public String ejbCreate(String id) {
      return null;
    }

    public void ejbPostCreate(String id) {};

    @Override
    public void ejbActivate() throws EJBException, RemoteException {}

    @Override
    public void ejbLoad() throws EJBException, RemoteException {}

    @Override
    public void ejbPassivate() throws EJBException, RemoteException {}

    @Override
    public void ejbRemove() throws RemoveException, EJBException, RemoteException {}

    @Override
    public void ejbStore() throws EJBException, RemoteException {}

    @Override
    public void setEntityContext(EntityContext arg0) throws EJBException, RemoteException {}

    @Override
    public void unsetEntityContext() throws EJBException, RemoteException {}
  }

  private static class FooEntityCMP implements javax.ejb.EntityBean {
    public String ejbCreate(String id) {
      return null;
    }

    public void ejbPostCreate(String id) {};

    @Override
    public void ejbActivate() throws EJBException, RemoteException {}

    @Override
    public void ejbLoad() throws EJBException, RemoteException {}

    @Override
    public void ejbPassivate() throws EJBException, RemoteException {}

    @Override
    public void ejbRemove() throws RemoveException, EJBException, RemoteException {}

    @Override
    public void ejbStore() throws EJBException, RemoteException {}

    @Override
    public void setEntityContext(EntityContext arg0) throws EJBException, RemoteException {}

    @Override
    public void unsetEntityContext() throws EJBException, RemoteException {}
  }
}
