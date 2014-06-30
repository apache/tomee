package org.superbiz.mtom;

import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;

@EnableServices("jaxws") // maybe this should be @Inherited like @RunWith
public class EjbServiceTest extends AbstractServiceTest {

    @Module
    public Class<?>[] module() {
        return new Class<?>[]{EjbService.class};
    }
}
