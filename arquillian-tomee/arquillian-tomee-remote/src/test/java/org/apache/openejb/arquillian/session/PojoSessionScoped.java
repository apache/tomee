package org.apache.openejb.arquillian.session;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rmannibucau
 */
@SessionScoped
public class PojoSessionScoped implements Serializable {
    private static AtomicInteger ID = new AtomicInteger();

    private long ms;
    private int id;

    public PojoSessionScoped() {
        ms = System.currentTimeMillis();
    }

    @PostConstruct public void initId() {
        id = ID.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    public long getMs() {
        return ms;
    }
}
