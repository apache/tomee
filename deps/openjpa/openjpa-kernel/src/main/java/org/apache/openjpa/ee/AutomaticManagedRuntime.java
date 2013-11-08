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

import java.util.LinkedList;
import java.util.List;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Implementation of the {@link ManagedRuntime} interface that searches
 * through a set of known JNDI locations and method invocations to locate the
 * appropriate mechanism for obtaining a TransactionManager.
 *  Built in support is provided for the following Application Servers:
 * <ul>
 * <li>Bluestone</li>
 * <li>GlassFish</li>
 * <li>HP Application Server</li>
 * <li>JBoss</li>
 * <li>JRun</li>
 * <li>OpenEJB</li>
 * <li>Oracle Application Server</li>
 * <li>Orion</li>
 * <li>SunONE</li>
 * <li>Weblogic</li>
 * <li>Websphere</li>
 * </ul>
 *
 * @author Marc Prud'hommeaux
 */
public class AutomaticManagedRuntime extends AbstractManagedRuntime
    implements ManagedRuntime, Configurable {

    private static final String [] JNDI_LOCS = new String []{
        "javax.transaction.TransactionManager", // weblogic
        "java:/TransactionManager", // jboss, jrun, Geronimo
        "java:/DefaultDomain/TransactionManager", // jrun too
        "java:comp/pm/TransactionManager", // orion & oracle
        "java:comp/TransactionManager", // generic
        "java:appserver/TransactionManager", // GlassFish
        "java:pm/TransactionManager", // borland
        "aries:services/javax.transaction.TransactionManager", // Apache Aries
    };
    private static final String [] METHODS = new String[]{
        "com.arjuna.jta.JTA_TransactionManager.transactionManager", // hp
        "com.bluestone.jta.SaTransactionManagerFactory.SaGetTransactionManager",
        "org.openejb.OpenEJB.getTransactionManager",
        "com.sun.jts.jta.TransactionManagerImpl.getTransactionManagerImpl",
        "com.inprise.visitransact.jta.TransactionManagerImpl."
            + "getTransactionManagerImpl", // borland
    };

    private final ManagedRuntime REGISTRY;
    private final WLSManagedRuntime WLS;
    private final SunOneManagedRuntime SUNONE;
    private final WASManagedRuntime WAS;
    private final WASRegistryManagedRuntime WAS_REG;

    private static Localizer _loc = Localizer.forPackage
        (AutomaticManagedRuntime.class);

    public AutomaticManagedRuntime() {
        ManagedRuntime mr = null;

        mr = null;
        try {
            mr = (ManagedRuntime) Class.
                forName("org.apache.openjpa.ee.RegistryManagedRuntime").
                    newInstance();
        } catch (Throwable t) {
            // might be JTA version lower than 1.1, which doesn't have 
            // TransactionSynchronizationRegistry
        }
        REGISTRY = mr;

        mr = null;
        try {
            mr = new WLSManagedRuntime();
        } catch (Throwable t) {
        }
        WLS = (WLSManagedRuntime) mr;

        mr = null;
        try {
            mr = new SunOneManagedRuntime();
        } catch (Throwable t) {
        }
        SUNONE = (SunOneManagedRuntime) mr;

        mr = null;
        try {
            mr = new WASManagedRuntime();
        } catch (Throwable t) {
        }
        WAS = (WASManagedRuntime) mr;

        mr = null;
        try {
            // In a WebSphere environment the thread's current classloader might
            // not have access to the WebSphere APIs. However the "runtime"
            // classloader will have access to them.
            
            // Should not need a doPriv getting this class' classloader
            ClassLoader cl = AutomaticManagedRuntime.class.getClassLoader();

            Class<WASRegistryManagedRuntime> mrClass =
                (Class<WASRegistryManagedRuntime>) J2DoPrivHelper
                        .getForNameAction(
                                WASRegistryManagedRuntime.class.getName(),
                                true, cl).run();
            mr = J2DoPrivHelper.newInstanceAction(mrClass).run();
        } catch (Throwable t) {
            // safe to ignore
        }
        WAS_REG = (WASRegistryManagedRuntime) mr;
    }

    private Configuration _conf = null;
    private ManagedRuntime _runtime = null;
    
    public TransactionManager getTransactionManager()
        throws Exception {
        if (_runtime != null)
            return _runtime.getTransactionManager();

        List<Throwable> errors = new LinkedList<Throwable>();
        TransactionManager tm = null;

        // Try the registry extensions first so that any applicable vendor
        // specific extensions are used.
        if (WAS_REG != null) {
            try {
                tm = WAS_REG.getTransactionManager();
            } catch (Throwable t) {
                errors.add(t);
            }
            if (tm != null) {
                _runtime = WAS_REG;
                return tm;
            }
        }

        // Then try the registry, which is the official way to obtain
        // transaction synchronication in JTA 1.1
        if (REGISTRY != null) {
            try {
                tm = REGISTRY.getTransactionManager();
            } catch (Throwable t) {
                errors.add(t);
            }
            if (tm != null) {
                _runtime = REGISTRY;
                return tm;
            }
        }

        if (WLS != null) {
            try {
                tm = WLS.getTransactionManager();
            } catch (Throwable t) {
                errors.add(t);
            }
            if (tm != null) {
                _runtime = WLS;
                return tm;
            }
        }

        if (WAS != null) {
            try {
                WAS.setConfiguration(_conf);
                WAS.startConfiguration();
                WAS.endConfiguration();
                tm = WAS.getTransactionManager();
            } catch (Throwable t) {
                errors.add(t);
            }
            if (tm != null) {
                _runtime = WAS;
                return tm;
            }
        }

        // try to find a jndi runtime
        JNDIManagedRuntime jmr = new JNDIManagedRuntime();
        for (int i = 0; i < JNDI_LOCS.length; i++) {
            jmr.setTransactionManagerName(JNDI_LOCS[i]);
            try {
                tm = jmr.getTransactionManager();
            } catch (Throwable t) {
                errors.add(t);
            }
            if (tm != null) {
                _runtime = jmr;
                return tm;
            }
        }

        // look for a method runtime
        InvocationManagedRuntime imr = new InvocationManagedRuntime();
        for (int i = 0; i < METHODS.length; i++) {
            imr.setConfiguration(_conf);
            imr.setTransactionManagerMethod(METHODS[i]);
            try {
                tm = imr.getTransactionManager();
            } catch (Throwable t) {
                errors.add(t);
            }
            if (tm != null) {
                _runtime = imr;
                return tm;
            }
        }

        if (SUNONE != null) {
            try {
                tm = SUNONE.getTransactionManager();
            } catch (Throwable t) {
                errors.add(t);
            }
            if (tm != null) {
                _runtime = SUNONE;
                return tm;
            }
        }

        Throwable[] t = (Throwable []) errors.toArray(
            new Throwable [errors.size()]);
        throw new InvalidStateException(_loc.get("tm-not-found")).
            setFatal(true).setNestedThrowables(t);
    }

    public void setConfiguration(Configuration conf) {
        _conf = conf;
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
    }

    public void setRollbackOnly(Throwable cause)
        throws Exception {
        // check to see if the runtime is cached
        if (_runtime == null)
            getTransactionManager();

        if (_runtime != null)
            _runtime.setRollbackOnly(cause);
    }

    public Throwable getRollbackCause()
        throws Exception {
        // check to see if the runtime is cached
        if (_runtime == null)
            getTransactionManager();

        if (_runtime != null)
            return _runtime.getRollbackCause();

        return null;
    }
    
    public Object getTransactionKey() throws Exception, SystemException {
        if(_runtime == null) 
            getTransactionManager();
        
        if(_runtime != null )
            return _runtime.getTransactionKey();
        
        return null;
    }

    /**
     * Delegate nonTransactional work to the appropriate managed runtime. If no
     * managed runtime is found then delegate {@link AbstractManagedRuntime}.
     */
    public void doNonTransactionalWork(Runnable runnable)
            throws NotSupportedException {
        // Obtain a transaction manager to initialize the runtime.
        try {
            getTransactionManager();
        } catch (Exception e) {
            NotSupportedException nse =
                new NotSupportedException(_loc
                        .get("tm-unavailable", _runtime).getMessage());
            nse.initCause(e);
            throw nse;
        }
        _runtime.doNonTransactionalWork(runnable);
    }
}
