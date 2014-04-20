package org.apache.openejb.junit.jee.statement;

import org.apache.openejb.OpenEJB;
import org.junit.runners.model.Statement;

import javax.ejb.embeddable.EJBContainer;

public class ShutingDownStatement extends DecoratingStatement {
    private final StartingStatement startingStatement;

    public ShutingDownStatement(final Statement statement, final StartingStatement startingStatement) {
        super(statement);
        this.startingStatement = startingStatement;
    }

    @Override
    protected void after() throws Exception {
        final EJBContainer container = startingStatement.getContainer();
        if (container != null) {
            container.close();
            OpenEJB.destroy();
        }
    }
}
