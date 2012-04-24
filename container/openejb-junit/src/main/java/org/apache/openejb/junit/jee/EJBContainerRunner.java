package org.apache.openejb.junit.jee;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.junit.jee.config.Properties;
import org.apache.openejb.junit.jee.config.Property;
import org.apache.openejb.junit.jee.config.PropertyFile;
import org.apache.openejb.junit.jee.resources.TestResource;
import org.apache.openejb.junit.jee.transaction.TransactionRule;
import org.apache.openejb.osgi.client.LocalInitialContextFactory;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openejb.util.SingleLineFormatter;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class EJBContainerRunner extends BlockJUnit4ClassRunner {
    static { // logging conf
        if (!System.getProperties().containsKey("java.util.logging.manager")) {
            System.setProperty("java.util.logging.manager", JuliLogStreamFactory.OpenEJBLogManager.class.getName());
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger("net");
            logger.setUseParentHandlers(false);
            logger.addHandler(new ConsoleHandler());
            logger.getHandlers()[0].setFormatter(new SingleLineFormatter());
        }
    }

    private java.util.Properties properties;
    private EJBContainer container;

    public EJBContainerRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        final Statement superStatement = super.withBeforeClasses(statement);
        return new StartingStatement(superStatement);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        final Statement superStatement = super.withAfterClasses(statement);
        return new ShutingDownStatement(superStatement);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        final List<FrameworkMethod> methods = super.computeTestMethods();
        Collections.shuffle(methods); // real tests should manage shuffle ordering
        return methods;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        final List<TestRule> rules = new ArrayList<TestRule>();
        rules.add(new InjectRule(target));
        rules.add(new TransactionRule());
        rules.addAll(getTestClass().getAnnotatedFieldValues(target, Rule.class, TestRule.class));
        return rules;
    }

    private static abstract class DecoratingStatement extends Statement {
        protected Statement decorated;

        public DecoratingStatement(final Statement statement) {
            decorated = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            before();
            try {
                decorated.evaluate();
            } finally {
                after();
            }
        }

        protected void before() throws Exception {
            // no-op
        }

        protected void after() throws Exception {
            // no-op
        }
    }

    private class StartingStatement extends DecoratingStatement {
        public StartingStatement(final Statement statement) {
            super(statement);
        }

        @Override
        protected void before() throws Exception {
            final Class<?> clazz = getTestClass().getJavaClass();
            properties = new java.util.Properties();
            properties.put(OpenEjbContainer.Provider.OPENEJB_ADDITIONNAL_CALLERS_KEY, clazz.getName());

            final PropertyFile propertyFile = clazz.getAnnotation(PropertyFile.class);
            if (propertyFile != null) {
                final String path = propertyFile.value();
                if (!path.isEmpty()) {
                    InputStream is = clazz.getClassLoader().getResourceAsStream(path);
                    if (is == null) {
                        final File file = new File(path);
                        if (file.exists()) {
                            is = new FileInputStream(file);
                        } else {
                            throw new OpenEJBException("properties resource '" + path + "' not found");
                        }
                    }

                    final java.util.Properties fileProps = new java.util.Properties();
                    fileProps.load(is);
                    for (Map.Entry<Object, Object> entry : fileProps.entrySet()) {
                        properties.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }

            final Properties annotationConfig = clazz.getAnnotation(Properties.class);
            if (annotationConfig != null) {
                for (Property property : annotationConfig.value()) {
                    properties.put(property.key(), property.value());
                }
            }

            if (!properties.containsKey(Context.INITIAL_CONTEXT_FACTORY)) {
                properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
            }

            container = EJBContainer.createEJBContainer(properties);
        }
    }

    private class ShutingDownStatement extends DecoratingStatement {
        public ShutingDownStatement(final Statement statement) {
            super(statement);
        }

        @Override
        protected void after() throws Exception {
            if (container != null) {
                container.close();
                OpenEJB.destroy();
                container = null;
            }
        }
    }

    private class InjectRule implements TestRule {
        private Object test;

        public InjectRule(final Object target) {
            this.test = target;
        }

        @Override
        public Statement apply(final Statement base, final Description description) {
            return new InjectStatement(base, test);
        }
    }

    private class InjectStatement extends Statement {
        private Object test;
        private Statement statement;

        public InjectStatement(final Statement stat, final Object o) {
            statement = stat;
            test = o;
        }

        @Override
        public void evaluate() throws Throwable {
            Class<?> clazz = test.getClass();
            while (!Object.class.equals(clazz)) {
                for (Field field : clazz.getDeclaredFields()) {
                    final TestResource resource = field.getAnnotation(TestResource.class);
                    if (resource != null) {
                        if (Context.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(test, container.getContext());
                        } else if (Hashtable.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(test, properties);
                        } else if (EJBContainer.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(test, container);
                        } else {
                            throw new OpenEJBException("can't inject field '" + field.getName() + "'");
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
            container.getContext().bind("inject", test);
            statement.evaluate();
        }
    }
}

