package org.apache.openejb.openjpa;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.LogFactoryAdapter;

public class JULOpenJPALogFactory extends LogFactoryAdapter {
    @Override
    protected Log newLogAdapter(final String channel) {
        return new JULOpenJPALog(channel);
    }
}
