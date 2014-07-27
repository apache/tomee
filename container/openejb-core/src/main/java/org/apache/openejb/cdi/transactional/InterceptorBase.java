/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi.transactional;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.transaction.TransactionPolicy;

import javax.interceptor.InvocationContext;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.TransactionalException;
import java.io.Serializable;
import java.lang.reflect.Method;

public abstract class InterceptorBase implements Serializable {
    protected Object intercept(final InvocationContext ic) throws Exception {
        Exception error = null;
        TransactionPolicy policy = null;

        final boolean forbidsUt = doesForbidUtUsage();
        final RuntimeException oldEx;
        final IllegalStateException illegalStateException;
        if (forbidsUt) {
            illegalStateException = new IllegalStateException("Can't use UserTransaction from @Transaction call");
            oldEx = CoreUserTransaction.error(illegalStateException);
        } else {
            illegalStateException = null;
            oldEx = null;
        }

        try {
            policy = getPolicy();
            return ic.proceed();
        } catch (final Exception e) {
            error = e;
            if (illegalStateException == e) {
                throw e;
            }
            throw new TransactionalException(e.getMessage(), e);
        } finally {
            if (forbidsUt) {
                CoreUserTransaction.resetError(oldEx);
            }

            if (policy != null) {
                if (error != null) {
                    // computed lazily but we could cache it later for sure if that's really a normal case
                    final Method method = ic.getMethod();
                    Transactional tx = method.getAnnotation(Transactional.class);
                    if (tx == null) {
                        tx = method.getDeclaringClass().getAnnotation(Transactional.class);
                    }
                    if (tx != null) { // TODO: find Bean instead of reflection
                        if (new ExceptionPriotiryRules(tx.rollbackOn(), tx.dontRollbackOn()).accept(error)) {
                            policy.setRollbackOnly();
                        }
                    }
                }
                policy.commit();
            }
        }
    }

    protected boolean doesForbidUtUsage() {
        return true;
    }

    protected abstract TransactionPolicy getPolicy() throws SystemException, ApplicationException;

    protected static TransactionManager getTransactionManager() {
        return OpenEJB.getTransactionManager();
    }

    private static class ExceptionPriotiryRules {
        private final Class<?>[] includes;
        private final Class<?>[] excludes;

        private ExceptionPriotiryRules(final Class<?>[] includes, final Class<?>[] excludes) {
            this.includes = includes;
            this.excludes = excludes;
        }

        public boolean accept(final Exception e) {
            if (e == null) {
                return false;
            }

            final Class<? extends Exception> key = e.getClass();
            final int includeScore = contains(includes, e);
            final int excludeScore = contains(excludes, e);

            if (excludeScore < 0) {
                return includeScore >= 0;
            }

            return includeScore >= 0 && includeScore - excludeScore <= 0;
        }

        private static int contains(final Class<?>[] list, final Exception e) {
            int score = -1;
            for (final Class<?> clazz : list) {
                if (clazz.isInstance(e)) {
                    final int thisScore = score(clazz, e.getClass());
                    if (score < 0) {
                        score = thisScore;
                    } else {
                        score = Math.min(thisScore, score);
                    }
                }
            }
            return score;
        }

        private static int score(final Class<?> config, final Class<?> ex) {
            int score = 0;
            Class<?> current = ex;
            while (current != null && !current.equals(config)) {
                score++;
                current = current.getSuperclass();
            }
            return score;
        }
    }

}
