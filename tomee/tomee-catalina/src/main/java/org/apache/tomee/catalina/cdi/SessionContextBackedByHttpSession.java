package org.apache.tomee.catalina.cdi;

import org.apache.tomee.catalina.OpenEJBValve;
import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.context.creational.BeanInstanceBag;

import javax.enterprise.context.spi.Contextual;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SessionContextBackedByHttpSession extends SessionContext {
    @Override
    public void setComponentInstanceMap() {
        componentInstanceMap = new HttpSessionMap();
    }

    protected static HttpSession session() {
        return OpenEJBValve.request().getSession(true);
    }

    private static class HttpSessionMap implements Map<Contextual<?>,BeanInstanceBag<?>> {
        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            return session().getAttribute(key.toString()) != null;
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Contextual<?>> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<BeanInstanceBag<?>> values() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            // no-op
        }

        @Override
        public Set<Entry<Contextual<?>, BeanInstanceBag<?>>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public BeanInstanceBag<?> get(Object key) {
            return (BeanInstanceBag<?>) session().getAttribute(key.toString());
        }

        @Override
        public BeanInstanceBag<?> put(Contextual<?> key, BeanInstanceBag<?> value) {
            session().setAttribute(key.toString(), value);
            return value;
        }

        @Override
        public BeanInstanceBag<?> remove(Object key) {
            final BeanInstanceBag<?> bag = get(key);
            session().removeAttribute(key.toString());
            return bag;
        }

        @Override
        public void putAll(Map<? extends Contextual<?>, ? extends BeanInstanceBag<?>> m) {
            for (Map.Entry<? extends Contextual<?>, ? extends BeanInstanceBag<?>> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }
}
