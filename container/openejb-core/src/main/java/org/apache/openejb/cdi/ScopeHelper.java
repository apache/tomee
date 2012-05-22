package org.apache.openejb.cdi;

import javax.enterprise.context.spi.Context;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.type.ContextTypes;

// helper for embedded case
public final class ScopeHelper {
    private ScopeHelper() {
        // no-op
    }

    public static void startContexts(final ContextFactory contextFactory, final ServletContext servletContext, final HttpSession session) throws Exception {
        contextFactory.initSingletonContext(servletContext);
        contextFactory.initApplicationContext(servletContext);
        contextFactory.initSessionContext(session);
        contextFactory.initConversationContext(null);
        contextFactory.initRequestContext(null);
    }

    public static void stopContexts(final ContextFactory contextFactory, final ServletContext servletContext, final HttpSession session) throws Exception {
        if(isActive(contextFactory.getStandardContext(ContextTypes.SESSION))) {
            contextFactory.destroySessionContext(session);
        }
        if (isActive(contextFactory.getStandardContext(ContextTypes.CONVERSATION))) {
            contextFactory.destroyConversationContext();
        }
        if (isActive(contextFactory.getStandardContext(ContextTypes.REQUEST))) {
            contextFactory.destroyRequestContext(null);
        }
        if (isActive(contextFactory.getStandardContext(ContextTypes.APPLICATION))) {
            contextFactory.destroyApplicationContext(servletContext);
        }
        if (isActive(contextFactory.getStandardContext(ContextTypes.SINGLETON))) {
            contextFactory.destroySingletonContext(servletContext);
        }
    }

    private static boolean isActive(Context context) {
        return context != null && context.isActive();
    }
}
