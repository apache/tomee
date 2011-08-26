package org.apache.openejb.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author rmannibucau
 */
public final class DatasourceDefinitionHelper {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, DatasourceDefinitionHelper.class.getPackage().getName());

    private static final Collection<String> MANUALLY_SET_PROPERTIES = Arrays.asList("serName", "portNumber", "url");

    private DatasourceDefinitionHelper() {
        // no-op
    }

    /**
     * datasource has to be created here since resources deployment was already done at start up time.
     *
     *
     * @param resourceInfo the datasource definition
     * @param classLoader the classloader to use
     * @return the datasource defined by dsDef
     */
    public static DataSource newInstance(ResourceInfo resourceInfo, ClassLoader classLoader) {
        String className = resourceInfo.properties.getProperty("className");
        String url = resourceInfo.properties.getProperty("url");
        String server = resourceInfo.properties.getProperty("serverName");
        int port = (Integer) resourceInfo.properties.get("portNumber");

        DataSource ds;

        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }

        if (!BasicDataSource.class.isAssignableFrom(clazz)) {
            ObjectRecipe objectRecipe = new ObjectRecipe(className);
            objectRecipe.allow(Option.FIELD_INJECTION);
            objectRecipe.allow(Option.PRIVATE_PROPERTIES);
            objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            objectRecipe.allow(Option.NAMED_PARAMETERS);

            if (port <= 0) {
                objectRecipe.setProperty("url", url);
            } else {
                objectRecipe.setProperty("serverName", server);
                objectRecipe.setProperty("portNumber", port);
            }

            for (Map.Entry<Object, Object> prop : resourceInfo.properties.entrySet()) {
                String name = (String) prop.getKey();
                if (!MANUALLY_SET_PROPERTIES.contains(name)) {
                    Object value = prop.getValue();
                    if (value != null
                            && ((value instanceof Number && ((Number) value).intValue() > 0)
                                || !(value instanceof Number))) {
                        objectRecipe.setProperty(name, value);
                    }
                    if (name.endsWith("Name")) {
                        // depending of implementations...
                        objectRecipe.setProperty(name.substring(0, name.length() - 4), value);
                    }
                }
            }

            ds = (DataSource) objectRecipe.create(classLoader);
        } else {
            ds = DataSourceFactory.create(true);
            BasicDataSource bd = (BasicDataSource) ds;
            if (server != null && port > 0) {
                if (url != null) { // try to use the pattern provided
                    int startHost = url.indexOf("//");
                    int endHost = url.indexOf("/", startHost + 2);
                    url = url.replace(url.substring(startHost + 2, endHost), server + ":" + port);
                } else {
                    url = "jdbc:derby://" + server + ":" + port
                            + "/" + resourceInfo.properties.getProperty("databaseName");
                }
            }
            bd.setUrl(url);

            bd.setInitialSize((Integer) resourceInfo.properties.get("initialPoolSize"));
            try {
                if ((Integer) resourceInfo.properties.get("loginTimeout") > 0) {
                    bd.setLoginTimeout((Integer) resourceInfo.properties.get("loginTimeout"));
                }
            } catch (SQLException e) {
                // ignored
            }
            if ((Integer) resourceInfo.properties.get("maxIdleTime") > 0) {
                bd.setMaxIdle((Integer) resourceInfo.properties.get("maxIdleTime"));
            }
            if ((Integer) resourceInfo.properties.get("maxStatements") > 0) {
                bd.setMaxOpenPreparedStatements((Integer) resourceInfo.properties.get("maxStatements"));
            }
            if ((Integer) resourceInfo.properties.get("minPoolSize") > 0) {
                bd.setMinIdle((Integer) resourceInfo.properties.get("minPoolSize"));
            }
            if ((Integer) resourceInfo.properties.get("maxPoolSize") > 0) {
                bd.setMaxIdle((Integer) resourceInfo.properties.get("maxPoolSize"));
            }
            bd.setPassword(resourceInfo.properties.getProperty("password"));
            bd.setUsername(resourceInfo.properties.getProperty("user"));
        }

        return ds;
    }
}
