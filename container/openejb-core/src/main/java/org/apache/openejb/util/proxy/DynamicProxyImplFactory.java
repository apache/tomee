package org.apache.openejb.util.proxy;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author rmannibucau
 */
public class DynamicProxyImplFactory {
    public static Object newProxy(BeanContext context) {
        List<Injection> injection = context.getInjections(); // the entity manager
        if (injection.size() < 1) {
            throw new RuntimeException("a dynamic bean should have at least one PersistenceContext annotation");
        }

        String emLookupName = injection.get(injection.size() - 1).getJndiName();
        EntityManager em;
        try {
            em = (EntityManager) context.getJndiEnc().lookup(emLookupName);
        } catch (NamingException e) {
            throw new RuntimeException("a dynamic bean should reference at least one correct PersistenceContext", e);
        }

        try {
            return ProxyManager.newProxyInstance(context.getLocalInterface(), new QueryProxy(em));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("illegal access", e);
        }
    }
}
