package org.apache.openejb.cdi;

import java.util.Map;
import java.util.Properties;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;

public class WebappWebBeansContext extends WebBeansContext {
    private WebBeansContext parent;
    private BeanManagerImpl bm;

    public WebappWebBeansContext(Map<Class<?>, Object> services, Properties properties, WebBeansContext webBeansContext) {
        super(services, properties);
        parent = webBeansContext;
    }

    @Override
    public BeanManagerImpl getBeanManagerImpl() {
        if (bm == null) { // should be done in the constructor
            bm = new WebappBeanManager(this);
        }
        return bm;
    }

    public WebBeansContext getParent() {
        return parent;
    }
}
