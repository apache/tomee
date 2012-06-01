package org.superbiz.init;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.transaction.TransactionManager;
import org.eclipse.persistence.transaction.JTATransactionController;

@Startup
@Singleton // tomee does it itself if eclipselink is in common.lib otherwise it is to be done by the app
public class Initializer {
    @Resource
    private TransactionManager tm;

    @PostConstruct
    private void init() {
        JTATransactionController.setDefaultTransactionManager(tm);
    }
}
