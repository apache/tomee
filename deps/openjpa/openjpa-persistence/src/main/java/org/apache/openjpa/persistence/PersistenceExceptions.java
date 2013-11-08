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

import java.lang.reflect.InvocationTargetException;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.MixedLockLevels;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.LockException;
import org.apache.openjpa.util.NoTransactionException;
import org.apache.openjpa.util.ObjectExistsException;
import org.apache.openjpa.util.ObjectNotFoundException;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.OptimisticException;
import org.apache.openjpa.util.QueryException;
import org.apache.openjpa.util.RuntimeExceptionTranslator;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UserException;

/**
 * Converts from OpenJPA to persistence exception types.
 *
 * @author Abe White
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class PersistenceExceptions
    extends Exceptions {

    public static final RuntimeExceptionTranslator TRANSLATOR =
        new RuntimeExceptionTranslator() {
            public RuntimeException translate(RuntimeException re) {
                return PersistenceExceptions.toPersistenceException(re);
            }
        };

    /**
     * Returns a {@link RuntimeExceptionTranslator} that will perform
     * the correct exception translation as well as roll back the
     * current transaction when for all but {@link NoResultException}
     * and {@link NonUniqueResultException} in accordance with
     * section 3.7 of the EJB 3.0 specification.
     */
    public static RuntimeExceptionTranslator getRollbackTranslator(
        final OpenJPAEntityManager em) {
        return new RuntimeExceptionTranslator() {
            private boolean throwing = false;

            public RuntimeException translate(RuntimeException re) {
                RuntimeException ex = toPersistenceException(re);
                if (!(ex instanceof NonUniqueResultException)
                    && !(ex instanceof NoResultException)
                    && !(ex instanceof LockTimeoutException)
                    && !(ex instanceof QueryTimeoutException)
                    && !throwing) {
                    try {
                        throwing = true;
                        if (em.isOpen() && ((EntityManagerImpl) em).isActive())
                            ((EntityManagerImpl) em).setRollbackOnly(ex);
                    } finally {
                        // handle re-entrancy
                        throwing = false;
                    }
                }

                return ex;
            }
        };
    }

    /**
     * Convert the given throwable to the proper persistence exception.
     */
    public static RuntimeException toPersistenceException(Throwable t) {
        return (RuntimeException) translateException(t, true);
    }

    /**
     * Translate the given exception.
     *
     * @param checked whether to translate checked exceptions
     */
    private static Throwable translateException(Throwable t, boolean checked) {
        if (isPersistenceException(t))
            return t;

        // immediately throw errors
        if (t instanceof Error)
            throw (Error) t;

        OpenJPAException ke;
        if (!(t instanceof OpenJPAException)) {
            if (!checked || t instanceof RuntimeException)
                return t;
            ke = new org.apache.openjpa.util.GeneralException(t.getMessage());
            ke.setStackTrace(t.getStackTrace());
            return ke;
        }

        // if only nested exception is a persistence one, return it directly
        ke = (OpenJPAException) t;
        if (ke.getNestedThrowables().length == 1
            && isPersistenceException(ke.getCause()))
            return ke.getCause();

        // RuntimeExceptions thrown from callbacks should be thrown directly
        if (ke.getType() == OpenJPAException.USER
            && ke.getSubtype() == UserException.CALLBACK
            && ke.getNestedThrowables().length == 1) {
            Throwable e = ke.getCause();
            if (e instanceof InvocationTargetException)
                e = e.getCause();

            if (e instanceof RuntimeException)
                return e;
        }

        // perform intelligent translation of openjpa exceptions
        switch (ke.getType()) {
            case OpenJPAException.STORE:
                return translateStoreException(ke);
            case OpenJPAException.USER:
                return translateUserException(ke);
            case OpenJPAException.WRAPPED:
                return translateWrappedException(ke);
            default:
                return translateGeneralException(ke);
        }
    }

    /**
     * Translate the given store exception.
     */
    private static Throwable translateStoreException(OpenJPAException ke) {
        Exception e;
        int subtype = ke.getSubtype();
        String msg  = ke.getMessage();
        Throwable[] nested = getNestedThrowables(ke);
        Object failed = getFailedObject(ke);
        boolean fatal = ke.isFatal();
        Throwable cause = (ke.getNestedThrowables() != null
                        && ke.getNestedThrowables().length == 1)
                         ? ke.getNestedThrowables()[0] : null;
        if (subtype == StoreException.OBJECT_NOT_FOUND || cause instanceof ObjectNotFoundException) {
                e = new org.apache.openjpa.persistence.EntityNotFoundException(msg, nested, failed, fatal);
        } else if (subtype == StoreException.OPTIMISTIC	|| cause instanceof OptimisticException) {
            	e = new org.apache.openjpa.persistence.OptimisticLockException(msg, nested, failed, fatal);
        } else if (subtype == StoreException.LOCK || cause instanceof LockException) {
            LockException lockEx = (LockException) (ke instanceof LockException ? ke : cause); 
            if (lockEx != null && lockEx.getLockLevel() >= MixedLockLevels.LOCK_PESSIMISTIC_READ) { 
                if (!lockEx.isFatal()) { 
                    e = new org.apache.openjpa.persistence.LockTimeoutException(msg, nested, failed); 
                } else { 
                    e = new org.apache.openjpa.persistence.PessimisticLockException(msg, nested, failed); 
                } 
            } else { 
                e = new org.apache.openjpa.persistence.OptimisticLockException(msg, nested, failed, fatal); 
            } 
        } else if (subtype == StoreException.OBJECT_EXISTS || cause instanceof ObjectExistsException) {
                e = new org.apache.openjpa.persistence.EntityExistsException(msg, nested, failed, fatal);
        } else if (subtype == StoreException.QUERY || cause instanceof QueryException) {
            QueryException queryEx = (QueryException) (ke instanceof QueryException ? ke : cause);
            if (!queryEx.isFatal()) {
                e = new org.apache.openjpa.persistence.QueryTimeoutException(msg, nested, failed, false);
            } else {
                e = new org.apache.openjpa.persistence.PersistenceException(msg, nested, failed, true);
            }
        } else {
            e = new org.apache.openjpa.persistence.PersistenceException(msg, nested, failed, fatal);
        }
        e.setStackTrace(ke.getStackTrace());
        return e;
    }

    /**
     * Translate the given user exception.
     * If a {link {@link OpenJPAException#getSubtype() sub type} is set on the
     * given exception then a corresponding facade-level exception i.e. the
     * exceptions that inherit JPA-defined exceptions is generated.
     * If given exception is not further classified to a sub type, then
     * an [@link {@link #translateInternalException(OpenJPAException)} attempt}
     * is made to translate the given OpenJPAException by its internal cause.
     */
    private static Exception translateUserException(OpenJPAException ke) {
        Exception e;
        switch (ke.getSubtype()) {
            case UserException.NO_TRANSACTION:
                e = new
                    org.apache.openjpa.persistence.TransactionRequiredException
                        (ke.getMessage(), getNestedThrowables(ke),
                            getFailedObject(ke), ke.isFatal());
                break;
            case UserException.NO_RESULT:
                e = new org.apache.openjpa.persistence.NoResultException
                    (ke.getMessage(), getNestedThrowables(ke),
                        getFailedObject(ke), ke.isFatal());
                break;
            case UserException.NON_UNIQUE_RESULT:
                e = new org.apache.openjpa.persistence.NonUniqueResultException
                    (ke.getMessage(), getNestedThrowables(ke),
                        getFailedObject(ke), ke.isFatal());
                break;
            case UserException.INVALID_STATE:
                e = new org.apache.openjpa.persistence.InvalidStateException
                    (ke.getMessage(), getNestedThrowables(ke),
                        getFailedObject(ke), ke.isFatal());
                break;
            default:
            	e = translateCause(ke);
        }
        e.setStackTrace(ke.getStackTrace());
        return e;
    }
    
    /*
     * Translate the given wrapped exception.  If contains an Exception, return
     * the exception.  If contains a Throwable, wrap the throwable and
     * return it.
     */
    private static Exception translateWrappedException(OpenJPAException ke) {
        Throwable t = ke.getCause();
        if (t instanceof Exception)
        	return (Exception)t;
        return translateCause(ke);
    }

    /**
     * Translate to a facade-level exception if the given exception
     *     a) has a cause i.e. one and only nested Throwable
     * and b) that cause is one of the known internal exception which has a
     *        direct facade-level counterpart
     *        (for example, ObjectNotFoundException can be translated to
     *         EntityNotFoundException).
     * If the above conditions are not met then return generic
     *    ArgumentException.
     *
     * In either case, preserve all the details.
     */
    private static Exception translateCause(OpenJPAException ke) {
    	Throwable cause = ke.getCause();
    	if (cause instanceof ObjectNotFoundException) {
    		return new EntityNotFoundException(
    		        ke.getMessage(), getNestedThrowables(ke),
            	    getFailedObject(ke), ke.isFatal());
    	} else if (cause instanceof ObjectExistsException) {
    		return new EntityExistsException(
    		        ke.getMessage(), getNestedThrowables(ke),
            	    getFailedObject(ke), ke.isFatal());
    	} else if (cause instanceof NoTransactionException) {
    		return new TransactionRequiredException(
        		    ke.getMessage(), getNestedThrowables(ke),
                	getFailedObject(ke), ke.isFatal());
    	} else if (cause instanceof OptimisticException) {
    		return new OptimisticLockException(
        		    ke.getMessage(), getNestedThrowables(ke),
                	getFailedObject(ke), ke.isFatal());
    	} else {
    		return new org.apache.openjpa.persistence.ArgumentException(
        		ke.getMessage(), getNestedThrowables(ke),
        		getFailedObject(ke), ke.isFatal());
    	}
    }

    /**
     * Translate the given general exception.
     */
    private static Throwable translateGeneralException(OpenJPAException ke) {
        Exception e = new org.apache.openjpa.persistence.PersistenceException
            (ke.getMessage(), getNestedThrowables(ke),
                getFailedObject(ke), ke.isFatal());
        e.setStackTrace(ke.getStackTrace());
        return e;
    }

    /**
     * Return true if the given exception is a persistence exception.
     */
    private static boolean isPersistenceException(Throwable t) {
        return t.getClass().getName()
            .startsWith("org.apache.openjpa.persistence.");
    }

    /**
     * Translate the nested throwables of the given openjpa exception into
     * nested throwables for a persistence exception.
     */
    private static Throwable[] getNestedThrowables(OpenJPAException ke) {
        Throwable[] nested = ke.getNestedThrowables();
        if (nested.length == 0)
            return nested;

        Throwable[] trans = new Throwable[nested.length];
        for (int i = 0; i < nested.length; i++)
            trans[i] = translateException(nested[i], false);
        return trans;
    }

    /**
     * Return the failed object for the given exception, performing any
     * necessary conversions.
     */
    private static Object getFailedObject(OpenJPAException ke) {
        Object o = ke.getFailedObject();
        if (o == null)
            return null;
        if (o instanceof Broker)
            return JPAFacadeHelper.toEntityManager((Broker) o);
        return JPAFacadeHelper.fromOpenJPAObjectId(o);
    }

    /**
     * Helper method to extract a nested exception from an internal nested
     * array in a safe way.
     */
    static Throwable getCause(Throwable[] nested) {
        if (nested == null || nested.length == 0)
            return null;
		return nested[0];
	}
}
