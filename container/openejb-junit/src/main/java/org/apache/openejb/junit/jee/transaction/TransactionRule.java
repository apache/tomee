package org.apache.openejb.junit.jee.transaction;

import java.lang.reflect.Method;
import javax.transaction.TransactionManager;
import org.apache.openejb.core.transaction.JtaTransactionPolicyFactory;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openejb.loader.SystemInstance;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TransactionRule implements TestRule {
    @Override
    public Statement apply(final Statement base, final Description description) {
        final Method mtd = getMethod(description.getTestClass(), description.getMethodName());
        final Transaction tx = mtd.getAnnotation(Transaction.class);
        if (tx != null) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
                    final JtaTransactionPolicyFactory factory = new JtaTransactionPolicyFactory(transactionManager);
                    final TransactionPolicy policy = factory.createTransactionPolicy(TransactionType.RequiresNew);
                    try {
                        base.evaluate();
                    } finally {
                        if (tx.rollback()) {
                            policy.setRollbackOnly();
                        }
                        policy.commit();
                    }
                }
            };
        } else {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    base.evaluate();
                }
            };
        }
    }

    private static Method getMethod(final Class<?> testClass, final String methodName) {
        try {
            return testClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            for (Method mtd : testClass.getMethods()) {
                if (methodName.equals(mtd.getName())) {
                    return mtd;
                }
            }
            return null;
        }
    }
}
