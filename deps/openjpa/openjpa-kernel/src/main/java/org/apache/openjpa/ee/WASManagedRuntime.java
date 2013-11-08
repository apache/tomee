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
package org.apache.openjpa.ee;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.AsmAdaptor;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InvalidStateException;
import org.apache.openjpa.util.NoTransactionException;

import serp.bytecode.BCClass;
import serp.bytecode.Project;

/**
 * {@link ManagedRuntime} implementation that allows synchronization with a
 * WebSphere managed transaction.
 *
 * <P>
 * WebSphere Application Server does not expose the TransactionManager to an
 * application. Instead it provides a proprietary interface to register for
 * synchronization and obtain transaction ids.
 *
 * <P>
 * WASManagedRuntime provides the wrapper classes needed to interact with the
 * WAS proprietary interface and the OpenJPA kernel.
 *
 * @author Kevin Sutter
 */
public class WASManagedRuntime extends AbstractManagedRuntime
        implements ManagedRuntime, Configurable {

    private static final Localizer _loc =
        Localizer.forPackage(WASManagedRuntime.class);

    private Object _extendedTransaction = null;
    private Method _getGlobalId = null;
    private Method _registerSync = null;
    private OpenJPAConfiguration _conf = null;
    private Log _log = null;

    /**
     * Gets an extendedJTATransaction from JNDI and creates a transaction
     * wrapper
     */
    public javax.transaction.TransactionManager getTransactionManager()
        throws Exception {
        return new WASTransaction();
    }

    /**
     * Transaction wrapper for WebSphere. WebSphere exposes a subset of the
     * Transaction and TransactionManager interfaces to the customer. Any
     * methods which are not exposed by WebSphere will throw an
     * IllegalStateException to the caller.
     *
     * <P>
     * Methods supported by WAS are
     * <UL>
     * <LI>RegisterSynchronization </LI>
     * <LI>GetStatus</LI>
     * </UL>
     */
    class WASTransaction implements javax.transaction.TransactionManager,
        javax.transaction.Transaction {

        public int getStatus() throws SystemException {
            int rval;
            try {
                if (getGlobalId() != null) {
                    rval = Status.STATUS_ACTIVE;
                } else {
                    rval = Status.STATUS_NO_TRANSACTION;
                }
            } catch (Exception e) {
                throw new NoTransactionException(_loc
                        .get("was-transaction-id-exception")).setCause(e);
            }
            return rval;
        }

        /**
         * Provides a Transaction wrapper. The transaction wrapper mayb only be
         * used to determine the status of the current transaction. WebSphere
         * does not allow direct control of container transactions.
         *
         * @return A WebSphere transaction wrapper.
         */
        public Transaction getTransaction() throws SystemException {
            return this;
        }

        /**
         * Register for synchronization with a WebSphere managed transaction via
         * the extendedJTATransaction interface.
         */
        public void registerSynchronization(Synchronization arg0)
            throws IllegalStateException, RollbackException, SystemException {
            if (_extendedTransaction != null) {
                try {
                    _registerSync.invoke(_extendedTransaction,
                        new Object[] { new WASSynchronization(arg0) });
                } catch (Exception e) {
                    throw new InvalidStateException(_loc
                        .get("was-reflection-exception")).setCause(e);
                }
            } else {
                throw new InvalidStateException(_loc.get("was-lookup-error"));
            }
        }

        /**
         * Gets the GlobalTransaction ID of the WebSphere managed transaction.
         * If no Global Transaction is active null will be returned.
         *
         * @return Null if a global transaction is not active or if an error
         *         occurs. byte[] id if a global transaction is active.
         */
        private byte[] getGlobalId() {
            try {
                return (byte[]) _getGlobalId.invoke(_extendedTransaction, null);
            } catch (Exception e) {
                throw new InvalidStateException(_loc
                    .get("was-reflection-exception")).setCause(e);
            }
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Throws an
         * IllegalStateException
         */
        public void begin() throws NotSupportedException, SystemException {
            throw new InvalidStateException(_loc.get("was-unsupported-op",
                "begin"));
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Throws an
         * IllegalStateException
         */
        public void commit() throws HeuristicMixedException,
            HeuristicRollbackException, IllegalStateException,
            RollbackException, SecurityException, SystemException {
            throw new InvalidStateException(_loc.get("was-unsupported-op",
                "commit"));
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Throws an
         * IllegalStateException
         */
        public void resume(Transaction arg0) throws IllegalStateException,
            InvalidTransactionException, SystemException {
            throw new InvalidStateException(_loc.get("was-unsupported-op",
                "resume"));
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Log a
         * trace instead of throwing an exception. Rollback may be used in
         * some error paths, throwing another exception may result in the
         * original exception being lost.
         */
        public void rollback() throws IllegalStateException, SecurityException,
            SystemException {
            if (_log.isTraceEnabled()) {
                _log.trace(_loc.get("was-unsupported-op", "rollback"));
            }
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Log a
         * trace instead of throwing an exception. SetRollbackOnly may be used
         * in some error paths, throwing another exception may result in the
         * original exception being lost.
         */
        public void setRollbackOnly() throws IllegalStateException,
            SystemException {
            if (_log.isTraceEnabled()) {
                _log.trace(_loc.get("was-unsupported-op", "setRollbackOnly"));
            }
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Throws an
         * IllegalStateException
         */
        public void setTransactionTimeout(int arg0) throws SystemException {
            throw new InvalidStateException(_loc.get("was-unsupported-op",
                "setTransactionTimeout"));
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Throws an
         * IllegalStateException
         */
        public Transaction suspend() throws SystemException {
            throw new InvalidStateException(_loc.get("was-unsupported-op",
                "suspend"));
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Throws an
         * IllegalStateException
         */
        public boolean delistResource(XAResource arg0, int arg1)
            throws IllegalStateException, SystemException {
            throw new InvalidStateException(_loc.get("was-unsupported-op",
                "delistResource"));
        }

        /**
         * Unimplemented, WAS does not provide this level of control. Throws an
         * IllegalStateException
         */
        public boolean enlistResource(XAResource arg0)
            throws IllegalStateException, RollbackException, SystemException {
            throw new InvalidStateException(_loc.get("was-unsupported-op",
                "enlistResource"));
        }
    }

    /**
     * WASSynchronization wrapper. This class translates the WAS proprietary
     * synchronization callback methods to javax.transaction.Synchronization
     * methods.
     *
     * <P>
     * This class implements the
     * com.ibm.websphere.jtaextensions.SynchronizationCallback interface. Since
     * SynchronizationCallback is not available at compile time we use Serp to
     * add the interface to the class after all classes have been compiled.
     *
     * <P>
     * SynchronizationCallback is expected to be available whenever this class
     * is instantiated, therefore this class should only be used when running in
     * WebSphere.
     *
     * @see org.apache.openjpa.util.WASTransformer
     */
    static class WASSynchronization {

        Synchronization _sync = null;

        WASSynchronization(Synchronization sync) {
            _sync = sync;
        }

        /**
         * AfterCompletion wrapper. Translates the WAS proprietary call to a
         * javax.transaction.Synchronization call.
         */
        public void afterCompletion(int localTransactionId,
            byte[] globalTransactionId, boolean committed) {
            if (_sync != null) {
                if (committed) {
                    _sync.afterCompletion(Status.STATUS_COMMITTED);
                } else {
                    _sync.afterCompletion(Status.STATUS_UNKNOWN);
                }
            }
        }

        /**
         * BeforeCompletion wrapper. Translates WAS proprietary call to a
         * javax.transaction.Synchronization call.
         */
        public void beforeCompletion(int arg0, byte[] arg1) {
            if (_sync != null) {
                _sync.beforeCompletion();
            }
        }
    }

    /**
     * Caches a copy of the configuration. The configuration is used to obtain
     * the logger and classloader.
     */
    public void setConfiguration(Configuration conf) {
        _conf = (OpenJPAConfiguration) conf;
        _log = _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
    }

    /**
     * EndConfiguration stub.
     */
    public void endConfiguration() {
        try {
            Context ctx = new InitialContext();
            try {
                _extendedTransaction =
                    ctx.lookup("java:comp/websphere/ExtendedJTATransaction");
            } finally {
                ctx.close();
            }

            Class extendedJTATransaction = Class.forName(
                    "com.ibm.websphere.jtaextensions.ExtendedJTATransaction");
            Class synchronizationCallback = Class.forName(
                    "com.ibm.websphere.jtaextensions.SynchronizationCallback");

            _registerSync = extendedJTATransaction.getMethod(
                    "registerSynchronizationCallbackForCurrentTran",
                    new Class[] { synchronizationCallback });
            _getGlobalId = extendedJTATransaction.
                getMethod("getGlobalId", null);
        } catch (Exception e) {
            throw new InvalidStateException(_loc
                .get("was-reflection-exception"), e).setFatal(true);
        }
    }

    /**
     * StartConfiguration stub.
     */
    public void startConfiguration() {
        // Nothing to do
    }

    /**
     * Class that will be modified
     */
    static final String CLASS =
        "org.apache.openjpa.ee.WASManagedRuntime$WASSynchronization";

    /**
     * Interface which will be added
     */
    static final String INTERFACE =
        "com.ibm.websphere.jtaextensions.SynchronizationCallback";

    public static void main(String[] args)
        throws IOException {
        Project project = new Project();
        
        InputStream in = WASManagedRuntime.class.getClassLoader()
            .getResourceAsStream(CLASS.replace('.', '/') + ".class");
        BCClass bcClass = project.loadClass(in);
        
        String [] interfaces = bcClass.getInterfaceNames();
        
        if(interfaces != null) {
        	for(int i = 0; i < interfaces.length; i++) { 
        		if(interfaces[i].equals(INTERFACE)) {
        			return; 
        		}
        	}
        }
        bcClass.declareInterface(INTERFACE);
        AsmAdaptor.write(bcClass);
    }

    public void setRollbackOnly(Throwable cause)
        throws Exception {
        // there is no generic support for setting the rollback cause
        getTransactionManager().getTransaction().setRollbackOnly();
    }

    public Throwable getRollbackCause()
        throws Exception {
        // there is no generic support for setting the rollback cause
        return null;
    }
}
