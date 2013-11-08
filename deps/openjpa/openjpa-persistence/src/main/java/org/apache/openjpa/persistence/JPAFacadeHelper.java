/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.BigDecimalId;
import org.apache.openjpa.util.BigIntegerId;
import org.apache.openjpa.util.ByteId;
import org.apache.openjpa.util.CharId;
import org.apache.openjpa.util.DoubleId;
import org.apache.openjpa.util.FloatId;
import org.apache.openjpa.util.Id;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.IntId;
import org.apache.openjpa.util.LongId;
import org.apache.openjpa.util.ObjectId;
import org.apache.openjpa.util.OpenJPAId;
import org.apache.openjpa.util.ShortId;
import org.apache.openjpa.util.StringId;
import org.apache.openjpa.util.UserException;

/**
 * Helper class for switching between OpenJPA's JPA facade and the underlying
 * Broker kernel.
 *
 * @since 1.0.0
 * @nojavadoc
 */
public class JPAFacadeHelper {

    public static final String EM_KEY =
        "org.apache.openjpa.persistence.EntityManager";
    public static final String EMF_KEY =
        "org.apache.openjpa.persistence.EntityManagerFactory";

    private static final Localizer _loc =
        Localizer.forPackage(JPAFacadeHelper.class);

    public static OpenJPAEntityManagerFactory toEntityManagerFactory(
        BrokerFactory factory) {
        if (factory == null)
            return null;

        factory.lock();
        try {
            OpenJPAEntityManagerFactory emf = (OpenJPAEntityManagerFactory)
                factory.getUserObject(EMF_KEY);
            if (emf == null) {
                emf = EntityManagerFactoryValue.newFactory(factory);
                factory.putUserObject(EMF_KEY, emf);
            }
            return emf;
        } catch (Exception e) {
            throw PersistenceExceptions.toPersistenceException(e);
        } finally {
            factory.unlock();
        }
    }

    /**
     * Return the underlying broker factory for the given persistence manager
     * factory facade.
     */
    public static BrokerFactory toBrokerFactory(EntityManagerFactory emf) {
        if (emf == null)
            return null;
        if (!(emf instanceof EntityManagerFactoryImpl)) {
            Class c = emf.getClass();
            try {
                // either cast here may fail
                emf = (EntityManagerFactoryImpl) ((OpenJPAEntityManagerFactory)
                    emf).getUserObject(EMF_KEY);
            } catch (ClassCastException cce) {
                throw new ArgumentException(_loc.get(
                    "cant-convert-brokerfactory", c), null, null, false);
            }
        }
        return ((EntityManagerFactoryImpl) emf).getBrokerFactory();
    }

    /**
     * Return a persistence manager facade to the given broker retaining
     * previously associated persistence context type.
     */
    public static OpenJPAEntityManager toEntityManager(Broker broker) {
        if (broker == null)
            return null;

        broker.lock();
        try {
            OpenJPAEntityManager em = (OpenJPAEntityManager)
                broker.getUserObject(EM_KEY);
            if (em == null) {
                EntityManagerFactoryImpl emf = (EntityManagerFactoryImpl)
                    toEntityManagerFactory(broker.getBrokerFactory());
                em = emf.newEntityManagerImpl(broker);
                broker.putUserObject(EM_KEY, em);
            }
            return em;
        } catch (Exception e) {
            throw PersistenceExceptions.toPersistenceException(e);
        } finally {
            broker.unlock();
        }
    }

    /**
     * Return the underlying broker for the given entity manager facade.
     */
    public static Broker toBroker(EntityManager em) {
        if (em == null)
            return null;
        if (!(em instanceof EntityManagerImpl)) {
            Class c = em.getClass();
            try {
                // either cast here may fail
                em = (EntityManagerImpl) ((OpenJPAEntityManager) em).
                    getUserObject(EM_KEY);
            } catch (ClassCastException cce) {
                throw new ArgumentException(_loc.get("cant-convert-broker", c),
                    new Throwable[] { cce }, null, false);
            }
        }
        return ((EntityManagerImpl) em).getBroker();
    }

    /**
     * Returns the {@link org.apache.openjpa.meta.ClassMetaData} associated with
     * the persistent object <code>o</code>.
     */
    public static ClassMetaData getMetaData(Object o) {
        if (o == null)
            return null;
        EntityManager em = OpenJPAPersistence.getEntityManager(o);
        return (em == null) ? null : getMetaData(em,
            ImplHelper.getManagedInstance(o).getClass());
    }

    /**
     * Returns the {@link org.apache.openjpa.meta.ClassMetaData} associated
     * with the persistent type <code>cls</code>.
     */
    public static ClassMetaData getMetaData(EntityManager em, Class cls) {
        if (em == null)
            throw new NullPointerException("em == null");

        OpenJPAEntityManagerSPI kem = (OpenJPAEntityManagerSPI)
            OpenJPAPersistence.cast(em);
        try {
            return kem.getConfiguration().getMetaDataRepositoryInstance().
                getMetaData(cls, kem.getClassLoader(), false);
        } catch (Exception e) {
            throw PersistenceExceptions.toPersistenceException(e);
        }
    }

    /**
     * Returns the {@link org.apache.openjpa.meta.ClassMetaData} associated
     * with the persistent type <code>cls</code>.
     */
    public static ClassMetaData getMetaData(EntityManagerFactory emf,
        Class cls) {
        if (emf == null)
            throw new NullPointerException("emf == null");

        OpenJPAEntityManagerFactorySPI emfSPI =
            (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(emf);
        try {
            return emfSPI.getConfiguration().getMetaDataRepositoryInstance().
                getMetaData(cls, null, false);
        } catch (Exception e) {
            throw PersistenceExceptions.toPersistenceException(e);
        }
    }

    /**
     * Translate from a OpenJPA identity object to a Persistence one.
     */
    public static Object fromOpenJPAObjectId(Object oid) {
        if (oid instanceof OpenJPAId)
            return ((OpenJPAId) oid).getIdObject();
        return oid;
    }

    /**
     * Translate from a Persistence identity object to a OpenJPA one. If the provided oid isn't of the expected type
     * a UserException will be thrown.
     */
    public static Object toOpenJPAObjectId(ClassMetaData meta, Object oid) {
        if (oid == null || meta == null)
            return null;
        if (oid instanceof OpenJPAId) {
            return oid;
        }
        Class<?> cls = meta.getDescribedType();
        Class<?> oidType = meta.getObjectIdType();
        FieldMetaData[] pks = meta.getPrimaryKeyFields();

        Object expected = oidType;
        Object actual = oid.getClass();

        // embedded id and derived id
        if (pks.length > 0 && (pks[0].isEmbedded() || pks[0].isTypePC())) {
            if (pks[0].getDeclaredType().equals(oid.getClass())) {
                return new ObjectId(cls, oid);
            }
            expected = pks[0].getDeclaredType();
        }
        if (oidType != null && oidType.equals(oid.getClass())) {
            // Check for compound id class
            return new ObjectId(cls, oid);
        }
        if (meta.getIdentityType() == ClassMetaData.ID_DATASTORE) {
            // no id field
            try {
                return new Id(cls, ((Number) oid).longValue());
            } catch (ClassCastException cce) {
                // swallow, the proper exception will be thrown below
                expected = Number.class;
            }
        }
        if (pks.length > 0) {
            Class<?> pkType = pks[0].getDeclaredType();
            try {
                // Check for basic types, cast provided object to expected type. Catch CCE for invalid input
                if (pkType.equals(Byte.class) || pkType.equals(byte.class))
                    return new ByteId(cls, (Byte) oid);
                if (pkType.equals(Character.class) || pkType.equals(char.class))
                    return new CharId(cls, (Character) oid);
                if (pkType.equals(Double.class) || pkType.equals(double.class))
                    return new DoubleId(cls, (Double) oid);
                if (pkType.equals(Float.class) || pkType.equals(float.class))
                    return new FloatId(cls, (Float) oid);
                if (pkType.equals(Integer.class) || pkType.equals(int.class))
                    return new IntId(cls, (Integer) oid);
                if (pkType.equals(Long.class) || pkType.equals(long.class))
                    return new LongId(cls, (Long) oid);
                if (pkType.equals(Short.class) || pkType.equals(short.class))
                    return new ShortId(cls, (Short) oid);
                if (pkType.equals(String.class))
                    return new StringId(cls, (String) oid);
                if (pkType.equals(BigDecimal.class))
                    return new BigDecimalId(cls, (BigDecimal) oid);
                if (pkType.equals(BigInteger.class))
                    return new BigIntegerId(cls, (BigInteger) oid);
            } catch (ClassCastException cce) {
                // swallow, the proper exception will be thrown below
                expected = pkType;
            }
        }
        // At this point we don't have a basic ID field, we don't have an embedded or composite id. fail.
        throw new UserException(_loc.get("invalid-oid", new Object[] { expected, actual }));
    }

    /**
     * Return an array of OpenJPA oids for the given native oid array.
     */
    public static Object[] toOpenJPAObjectIds(ClassMetaData meta,
        Object... oids) {
        if (oids == null || oids.length == 0)
            return oids;

        // since the class if fixed for all oids, we can tell if we have to
        // translate the array based on whether the first oid needs translating
        Object oid = toOpenJPAObjectId(meta, oids[0]);
        if (oid == oids[0])
            return oids;

        Object[] copy = new Object[oids.length];
        copy[0] = oid;
        for (int i = 1; i < oids.length; i++)
            copy[i] = toOpenJPAObjectId(meta, oids[i]);
        return copy;
    }

    /**
     * Return a collection of OpenJPA oids for the given native oid collection.
     */
    public static Collection<Object> toOpenJPAObjectIds(ClassMetaData meta, Collection<Object> oids) {
        if (oids == null || oids.size() == 0) {
            return oids;
        }
        return Arrays.asList(toOpenJPAObjectIds(meta, oids.toArray()));
    }

    
    /**
     * Translate from a OpenJPA identity class to a native one.
     */
    public static Class fromOpenJPAObjectIdClass(Class oidClass) {
        if (oidClass == null)
            return null;
        if (oidClass == Id.class)
            return Long.class;
        if (oidClass == ByteId.class)
            return Byte.class;
        if (oidClass == CharId.class)
            return Character.class;
        if (oidClass == DoubleId.class)
            return Double.class;
        if (oidClass == FloatId.class)
            return Float.class;
        if (oidClass == IntId.class)
            return Integer.class;
        if (oidClass == LongId.class)
            return Long.class;
        if (oidClass == ShortId.class)
            return Short.class;
        if (oidClass == StringId.class)
            return String.class;
        if (oidClass == BigDecimalId.class)
            return BigDecimal.class;
        if (oidClass == BigIntegerId.class)
            return BigInteger.class;
        return oidClass;
	}
}
