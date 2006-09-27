package org.apache.openejb;

import java.lang.reflect.Method;

public interface RpcContainer extends Container {

    public Object invoke(Object deployID, Method callMethod, Object [] args, Object primKey, Object securityIdentity) throws OpenEJBException;
}
