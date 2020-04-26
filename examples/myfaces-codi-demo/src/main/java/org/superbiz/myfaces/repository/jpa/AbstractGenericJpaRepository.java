/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.myfaces.repository.jpa;

import org.superbiz.myfaces.domain.AbstractDomainObject;
import org.superbiz.myfaces.repository.GenericRepository;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Abstract repository class which provides default implementations for common repository methods.
 */
public abstract class AbstractGenericJpaRepository<T extends AbstractDomainObject> implements GenericRepository<T> {

    protected final Class<? extends AbstractDomainObject> entityClass;

    @Inject
    protected EntityManager entityManager;

    public AbstractGenericJpaRepository() {
        Class currentClass = getClass();

        if (currentClass.getName().contains("$$")) { //we are in a proxy
            currentClass = currentClass.getSuperclass();
        }

        for (Type interfaceClass : currentClass.getGenericInterfaces()) {
            for (Type genericInterfaceClass : ((Class) interfaceClass).getGenericInterfaces()) {
                if (genericInterfaceClass instanceof ParameterizedType &&
                        GenericRepository.class.isAssignableFrom((Class) ((ParameterizedType) genericInterfaceClass).getRawType())) {
                    for (Type parameterizedType : ((ParameterizedType) genericInterfaceClass).getActualTypeArguments()) {
                        if (AbstractDomainObject.class.isAssignableFrom((Class) parameterizedType)) {
                            this.entityClass = (Class<? extends AbstractDomainObject>) parameterizedType;
                            return;
                        }
                    }
                }
            }
        }

        throw new IllegalStateException("Entity type of " + currentClass.getName() + " not detected!");
    }

    @Override
    public T createNewEntity() {
        try {
            return (T) this.entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void save(T entity) {
        if (entity.isTransient()) {
            this.entityManager.persist(entity);
        } else {
            this.entityManager.merge(entity);
        }
    }

    public void remove(T entity) {
        if (entity.isTransient()) {
            throw new IllegalStateException("entity is not persistent");
        }

        this.entityManager.remove(loadById(entity.getId()));
    }

    public List<T> loadAll() {
        return (List<T>) this.entityManager.createQuery("select entity from " + this.entityClass.getSimpleName() + " entity")
                .getResultList();
    }

    public T loadById(Long id) {
        return (T) this.entityManager.find(this.entityClass, id);
    }
}