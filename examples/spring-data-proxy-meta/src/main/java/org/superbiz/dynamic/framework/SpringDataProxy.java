package org.superbiz.dynamic.framework;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.Repository;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class SpringDataProxy implements InvocationHandler {
    @PersistenceContext(unitName = "dynamic")
    private EntityManager em;

    @Resource(name = "implementingInterfaceClass")
    private Class<Repository<?, ?>> implementingInterfaceClass; // implicitly for this kind of proxy

    private final AtomicReference<Repository<?, ?>> repository = new AtomicReference<Repository<?, ?>>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (repository.get() == null) {
            synchronized (this) {
                if (repository.get() == null) {
                    repository.set(new JpaRepositoryFactory(em).getRepository(implementingInterfaceClass));
                }
            }
        }
        return method.invoke(repository.get(), args);
    }
}
