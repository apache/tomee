package org.openejb.loader;

import org.openejb.util.FileUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class LoaderServlet extends HttpServlet {
    private OpenEJBInstance openejb;

    public void init(ServletConfig config) throws ServletException {
        Properties p = new Properties();
        p.setProperty("openejb.loader", "tomcat");

        Enumeration enum = config.getInitParameterNames();
        System.out.println("OpenEJB init-params:");
        while (enum.hasMoreElements()) {
            String name = (String) enum.nextElement();
            String value = config.getInitParameter(name);
            p.put(name, value);
            System.out.println("\tparam-name: " + name + ", param-value: " + value);
        }

        String loader = p.getProperty("openejb.loader");
        if (loader.endsWith("tomcat-webapp")) {
            ServletContext ctx = config.getServletContext();
            File webInf = new File(ctx.getRealPath("WEB-INF"));
            File webapp = webInf.getParentFile();
            String webappPath = webapp.getAbsolutePath();

            setPropertyIfNUll(p, "openejb.base", webappPath);
            setPropertyIfNUll(p, "openejb.configuration", "META-INF/openejb.xml");
            setPropertyIfNUll(p, "openejb.container.decorators", "org.openejb.core.TomcatJndiSupport");
            setPropertyIfNUll(p, "log4j.configuration", "META-INF/log4j.properties");
        }

        try {
            init(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Properties properties) throws Exception {
        if (openejb != null) return;
        SystemInstance.init(properties);
        openejb = new OpenEJBInstance();
        if (openejb.isInitialized()) return;
        openejb.init(properties);
    }

    private Object setPropertyIfNUll(Properties properties, String key, String value) {
        String currentValue = properties.getProperty(key);
        if (currentValue == null) {
            properties.setProperty(key, value);
        }
        return currentValue;
    }
}
