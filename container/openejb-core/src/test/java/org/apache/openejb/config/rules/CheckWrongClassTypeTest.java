package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.StatelessBean;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckWrongClassTypeTest {
  @Keys({ "wrong.class.type", "noInterfaceDeclared.entity" })
  public EjbJar wrongClassType() throws OpenEJBException {
    System.setProperty("openejb.validation.output.level", "VERBOSE");
    EjbJar ejbJar = new EjbJar();
    EntityBean entityBean = new EntityBean();
    entityBean.setEjbClass(FooEntity.class);
    entityBean.setEjbName("fooEntity");
    entityBean.setPersistenceType(PersistenceType.BEAN);
    ejbJar.addEnterpriseBean(entityBean);
    return ejbJar;
  }

  private static class FooEntity {
  }
}
