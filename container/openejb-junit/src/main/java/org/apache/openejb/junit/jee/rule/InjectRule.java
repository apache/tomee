package org.apache.openejb.junit.jee.rule;

import org.apache.openejb.junit.jee.EJBContainerRule;
import org.apache.openejb.junit.jee.statement.InjectStatement;
import org.apache.openejb.junit.jee.statement.StartingStatement;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class InjectRule implements TestRule {
    private final StartingStatement startingStatement;
    private final Object test;

    public InjectRule(final Object target, final EJBContainerRule rule) {
        this(target, rule.getStartingStatement());
    }

    public InjectRule(final Object target, final StartingStatement startingStatement) {
        this.test = target;
        this.startingStatement = startingStatement;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new InjectStatement(base, test.getClass(), test, startingStatement);
    }
}
