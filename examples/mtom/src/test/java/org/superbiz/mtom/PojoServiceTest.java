package org.superbiz.mtom;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;

@EnableServices("jaxws") // maybe this should be @Inherited like @RunWith
public class PojoServiceTest extends AbstractServiceTest {

    @Module
    public WebApp module() {
        return new WebApp().addServlet("ws", PojoService.class.getName(), "/ws");
    }
}
