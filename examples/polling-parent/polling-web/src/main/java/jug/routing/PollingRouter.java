package jug.routing;

import org.apache.openejb.resource.jdbc.AbstractRouter;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PollingRouter extends AbstractRouter {
    private static final Logger LOGGER = Logger.getLogger(PollingRouter.class.getName());

    private Map<String, DataSource> dataSources = null;
    private ThreadLocal<DataSource> currentDataSource = new ThreadLocal<DataSource>() {
        @Override
        public DataSource initialValue() {
            return dataSources.get("jdbc/client1");
        }
    };

    private void init() {
        dataSources = new HashMap<String, DataSource>();
        for (String ds : Arrays.asList("jdbc/client1", "jdbc/client2")) {
            try {
                final Object o = getOpenEJBResource(ds);
                if (o instanceof DataSource) {
                    dataSources.put(ds, DataSource.class.cast(o));
                }
            } catch (NamingException e) {
                // ignored
            }
        }
    }

    @Override
    public DataSource getDataSource() {
        if (dataSources == null) {
            init();
        }
        final DataSource ds = currentDataSource.get();
        LOGGER.info("using ds " + ds);
        return ds;
    }

    public void setDataSource(final String client) {
        if (dataSources == null) {
            init();
        }

        final String datasourceName = "jdbc/" + client;
        if (!dataSources.containsKey(datasourceName)) {
            throw new IllegalArgumentException("data source called " + datasourceName + " can't be found.");
        }
        final DataSource ds = dataSources.get(datasourceName);
        currentDataSource.set(ds);
    }

    public void clear() {
        currentDataSource.remove();
    }
}
