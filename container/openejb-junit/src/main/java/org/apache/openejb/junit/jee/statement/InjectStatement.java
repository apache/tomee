package org.apache.openejb.junit.jee.statement;

import org.apache.openejb.Injector;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.junit.jee.resources.TestResource;
import org.junit.runners.model.Statement;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

public class InjectStatement extends Statement {
    private final StartingStatement startingStatement;
    private final Object test;
    private final Statement statement;
    private final Class<?> clazz;

    public InjectStatement(final Statement stat, final Class<?> clazz, final Object o, final StartingStatement startingStatement) {
        this.statement = stat;
        this.clazz = clazz;
        this.test = o;
        this.startingStatement = startingStatement;
    }

    @Override
    public void evaluate() throws Throwable {
        if (startingStatement != null) {
            Class<?> clazz = this.clazz;
            while (!Object.class.equals(clazz)) {
                for (Field field : clazz.getDeclaredFields()) {
                    final TestResource resource = field.getAnnotation(TestResource.class);
                    if (resource != null) {
                        if (Context.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, startingStatement.getContainer().getContext());
                        } else if (Hashtable.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, startingStatement.getProperties());
                        } else if (EJBContainer.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, startingStatement.getContainer());
                        } else {
                            throw new OpenEJBException("can't inject field '" + field.getName() + "'");
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        if (test != null) {
            Injector.inject(test);
        }
        if (statement != null) {
            statement.evaluate();
        }
    }
}
