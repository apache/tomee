package jug.routing;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@WebFilter(displayName = "routing-filter", urlPatterns = { "/*" })
public class RoutingFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(RoutingFilter.class.getName());
    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Resource(name = "ClientRouter", type = PollingRouter.class)
    private PollingRouter router;

    @Inject
    private DataSourceInitializer init;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        init.init();
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        String client = servletRequest.getParameter("client");
        if (client == null) {
            client = getRandomClient();
        }
        LOGGER.info("using client " + client);
        router.setDataSource(client);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            router.clear();
        }
    }

    private String getRandomClient() {
        return "client" +  (1 + COUNTER.getAndIncrement() % 2); // 2 clients
    }

    @Override
    public void destroy() {
        // no-op
    }
}
