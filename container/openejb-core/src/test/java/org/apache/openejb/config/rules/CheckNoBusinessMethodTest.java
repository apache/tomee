package org.apache.openejb.config.rules;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckNoBusinessMethodTest {
  // ===========START Region for testing key no.busines.method.args for local EJB's==========
  /**
   * Test success scenario: This test will succeed when an EJB class has a method with the same name as the one in the Local interface, but the types of arguments are different.
   */
  @Keys({ "no.busines.method.args" })
  public EjbJar noBusinessMethodArgsLocal() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    StatelessBean bean = new StatelessBean(FooSession.class);
    bean.setLocalHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooLocalHome");
    bean.setLocal("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooLocal");
    ejbJar.addEnterpriseBean(bean);
    return ejbJar;
  }

  private static interface FooLocalHome extends EJBLocalHome {
    FooLocal create() throws CreateException;
  }

  private static interface FooLocal extends EJBLocalObject {
    void foo(String x, String y);
  }

  private static class FooSession implements SessionBean {
    // method name is same as in the Local interface, except arguments are different
    public void foo(int x, String y) {
    }

    public void ejbCreate() {
    }

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
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
    }
  }

  // =========== END Region for testing key no.busines.method.args for local EJB's==========
  // ===========START Region for testing key no.busines.method.args for Remote EJB's==========
  /**
   * Test success scenario: This test will succeed when an EJB class has a method with the same name as the one in the Remote interface, but the types of arguments are different.
   */
  @Keys({ "no.busines.method.args" })
  public EjbJar noBusinessMethodArgsRemote() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    StatelessBean bean = new StatelessBean(FooSessionRemote.class);
    bean.setHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooRemoteHome");
    bean.setRemote("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$FooRemote");
    ejbJar.addEnterpriseBean(bean);
    return ejbJar;
  }

  private static interface FooRemoteHome extends EJBHome {
    FooRemote create() throws RemoteException, CreateException;
  }

  private static interface FooRemote extends EJBObject {
    void foo(String x, String y) throws RemoteException;
  }

  private static class FooSessionRemote implements SessionBean {
    // method name is same as in the Remote interface, except arguments are different
    public void foo(int x, String y) {
    }

    public void ejbCreate() {
    }

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
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
    }
  }

  // =========== END Region for testing key no.busines.method.args for remote EJB's==========
  // ===========START Region for testing key no.busines.method.case for local EJB's==========
  /**
   * Test success scenario: This test will succeed when an EJB class has a method with the same name as the one in the Local interface, but the method names have a different case.
   */
  @Keys({ "no.busines.method.case" })
  public EjbJar noBusinessMethodCaseLocal() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    StatelessBean bean = new StatelessBean(BarSession.class);
    bean.setLocalHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BarLocalHome");
    bean.setLocal("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BarLocal");
    ejbJar.addEnterpriseBean(bean);
    return ejbJar;
  }

  private static interface BarLocalHome extends EJBLocalHome {
    BarLocal create() throws CreateException;
  }

  private static interface BarLocal extends EJBLocalObject {
    void foo(String x, String y);
  }

  private static class BarSession implements SessionBean {
    // method name is same as in the Local interface, except the method name is in different case
    public void Foo(String x, String y) {
    }

    public void ejbCreate() {
    }

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
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
    }
  }

  // =========== END Region for testing key no.busines.method.case for local EJB's==========
  // ===========START Region for testing key no.busines.method.case for remote EJB's==========
  /**
   * Test success scenario: This test will succeed when an EJB class has a method with the same name as the one in the Remote interface, but the method names have a different case.
   */
  @Keys({ "no.busines.method.case" })
  public EjbJar noBusinessMethodCaseRemote() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    StatelessBean bean = new StatelessBean(BarSessionRemote.class);
    bean.setHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BarRemoteHome");
    bean.setRemote("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BarRemote");
    ejbJar.addEnterpriseBean(bean);
    return ejbJar;
  }

  private static interface BarRemoteHome extends EJBHome {
    BarRemote create() throws RemoteException, CreateException;
  }

  private static interface BarRemote extends EJBObject {
    void foo(String x, String y) throws RemoteException;
  }

  private static class BarSessionRemote implements SessionBean {
    // method name is same as in the Remote interface, except the method name is in different case
    public void Foo(String x, String y) {
    }

    public void ejbCreate() {
    }

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
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
    }
  }

  // =========== END Region for testing key no.busines.method.case for remote EJB's==========
  // ===========START Region for testing key no.busines.method for local EJB's==========
  /**
   * Test success scenario: This test will succeed when an EJB class does not have the method defined in its Local interface
   */
  @Keys({ "no.busines.method" })
  public EjbJar noBusinessMethodLocal() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    StatelessBean bean = new StatelessBean(BazSession.class);
    bean.setLocalHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BazLocalHome");
    bean.setLocal("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BazLocal");
    ejbJar.addEnterpriseBean(bean);
    return ejbJar;
  }

  private static interface BazLocalHome extends EJBLocalHome {
    BazLocal create() throws CreateException;
  }

  private static interface BazLocal extends EJBLocalObject {
    void foo(String x, String y);
  }

  private static class BazSession implements SessionBean {
    // method name is same as in the Local interface, except the method name is in different case
    public void ejbCreate() {
    }

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
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
    }
  }

  // =========== END Region for testing key no.busines.method for local EJB's==========
  // ===========START Region for testing key no.busines.method for remote EJB's==========
  /**
   * Test success scenario: This test will succeed when an EJB class does not have the method defined in its Remote interface
   */
  @Keys({ "no.busines.method" })
  public EjbJar noBusinessMethodRemote() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    StatelessBean bean = new StatelessBean(BazSessionRemote.class);
    bean.setHome("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BazRemoteHome");
    bean.setRemote("org.apache.openejb.config.rules.CheckNoBusinessMethodTest$BazRemote");
    ejbJar.addEnterpriseBean(bean);
    return ejbJar;
  }

  private static interface BazRemoteHome extends EJBHome {
    BazRemote create() throws RemoteException, CreateException;
  }

  private static interface BazRemote extends EJBObject {
    void foo(String x, String y) throws RemoteException;
  }

  private static class BazSessionRemote implements SessionBean {
    // method name is same as in the Local interface, except the method name is in different case
    public void ejbCreate() {
    }

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
    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
    }
  }
  // =========== END Region for testing key no.busines.method for remote EJB's==========
}
