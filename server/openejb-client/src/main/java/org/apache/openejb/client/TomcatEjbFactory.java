package org.apache.openejb.client;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public final class TomcatEjbFactory implements ObjectFactory {
    private final static String OPENEJB_PREFIX = "openejb.";

    private final static String JAVA_PREFIX = "java.";

    private final static String OPENEJB_EJB_LINK = "openejb.ejb-link";

    private final static int OPENEJB_PREFIX_LENGTH = OPENEJB_PREFIX.length();

    public Object getObjectInstance(Object obj,
                                    Name name,
                                    Context nameCtx,
                                    Hashtable environment)
            throws Exception {
        Object beanObj = null;
        Class ejbRefClass = Class.forName("org.apache.naming.EjbRef");
        if (ejbRefClass.isAssignableFrom(obj.getClass())) {
            RefAddr refAddr = null;
            String addrType = null;
            Properties env = new Properties();
            String bean = null;

            Reference ref = (Reference) obj;

            Enumeration addresses = ref.getAll();
            while (addresses.hasMoreElements()) {
                refAddr = (RefAddr) addresses.nextElement();
                addrType = refAddr.getType();
                if (addrType.startsWith(OPENEJB_PREFIX)) {
                    String value = refAddr.getContent().toString();
                    if (addrType.equals(OPENEJB_EJB_LINK)) {
                        bean = value;
                        continue;
                    }
                    String key = addrType.substring(OPENEJB_PREFIX_LENGTH);
                    key = JAVA_PREFIX + key;
                    env.put(key, value);
                }
            }

            if (bean != null) {
                beanObj = (new InitialContext(env)).lookup(bean);
            }
        }
        return beanObj;
    }
}
