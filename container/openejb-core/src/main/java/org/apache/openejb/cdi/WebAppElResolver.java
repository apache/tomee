package org.apache.openejb.cdi;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

public class WebAppElResolver extends ELResolver {
    private final ELResolver parent;
    private final ELResolver resolver;

    public WebAppElResolver(ELResolver elResolver, ELResolver elResolver1) {
        resolver = elResolver;
        parent = elResolver1;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Object value = resolver.getValue(context, base, property);
        if (value == null) {
            value = parent.getValue(context, base, property);
        }
        return value;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Class<?> value = resolver.getType(context, base, property);
        if (value == null) {
            value = parent.getType(context, base, property);
        }
        return value;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        // no-op
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }
}
