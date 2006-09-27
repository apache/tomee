/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.apache.openejb;

import org.apache.geronimo.timer.PersistenceException;
import org.apache.openejb.cache.InstanceCache;
import org.apache.openejb.cache.SimpleInstanceCache;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.sfsb.BusinessMethod;
import org.apache.openejb.sfsb.CreateMethod;
import org.apache.openejb.sfsb.RemoveMethod;
import org.apache.openejb.sfsb.StatefulInstanceContextFactory;
import org.apache.openejb.sfsb.StatefulInstanceFactory;
import org.apache.openejb.util.Index;

import javax.security.auth.Subject;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;


/**
 * @version $Revision$ $Date$
 */
public class StatefulEjbDeployment extends AbstractRpcDeployment implements ExtendedEjbDeployment {
    private final StatefulInstanceFactory instanceFactory;
    private final InstanceCache instanceCache;
    private final Index dispatchIndex;

    public StatefulEjbDeployment(String containerId,
                                 String ejbName,

                                 String homeInterfaceName,
                                 String remoteInterfaceName,
                                 String localHomeInterfaceName,
                                 String localInterfaceName,
                                 String beanClassName,
                                 ClassLoader classLoader,

                                 StatefulEjbContainer ejbContainer,

                                 String[] jndiNames,
                                 String[] localJndiNames,

                                 boolean securityEnabled,
                                 String policyContextId,
                                 Subject defaultSubject,
                                 Subject runAs,

                                 boolean beanManagedTransactions,
                                 SortedMap transactionPolicies,

                                 Map componentContext,

                                 // connector stuff
                                 Set unshareableResources,
                                 Set applicationManagedSecurityResources) throws Exception {

        this(containerId,
                ejbName,


                loadClass(homeInterfaceName, classLoader, "home interface"),
                loadClass(remoteInterfaceName, classLoader, "remote interface"),
                loadClass(localHomeInterfaceName, classLoader, "local home interface"),
                loadClass(localInterfaceName, classLoader, "local interface"),
                loadClass(beanClassName, classLoader, "bean class"),
                classLoader,

                ejbContainer,

                jndiNames,
                localJndiNames,

                securityEnabled,
                policyContextId,
                defaultSubject,
                runAs,

                beanManagedTransactions,
                transactionPolicies,

                componentContext,

                unshareableResources,
                applicationManagedSecurityResources);
    }

    public StatefulEjbDeployment(String containerId,
                                 String ejbName,

                                 Class homeInterface,
                                 Class remoteInterface,
                                 Class localHomeInterface,
                                 Class localInterface,
                                 Class beanClass,
                                 ClassLoader classLoader,

                                 StatefulEjbContainer ejbContainer,

                                 String[] jndiNames,
                                 String[] localJndiNames,

                                 boolean securityEnabled,
                                 String policyContextId,
                                 Subject defaultSubject,
                                 Subject runAs,

                                 boolean beanManagedTransactions,
                                 SortedMap transactionPolicies,

                                 Map componentContext,

                                 // connector stuff
                                 Set unshareableResources,
                                 Set applicationManagedSecurityResources) throws Exception {

        super(containerId,
                ejbName,

                new ProxyInfo(EJBComponentType.STATEFUL,
                        containerId,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        null,
                        null),
                beanClass,
                classLoader,

                new RpcSignatureIndexBuilder(beanClass,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        null),

                ejbContainer,

                jndiNames,
                localJndiNames,

                securityEnabled,
                policyContextId,
                defaultSubject,
                runAs,

                beanManagedTransactions,
                transactionPolicies,

                componentContext,
                unshareableResources,
                applicationManagedSecurityResources);

        dispatchIndex = buildDispatchMethodMap();

        // build the instance factory
        StatefulInstanceContextFactory contextFactory;
        contextFactory = new StatefulInstanceContextFactory(this, ejbContainer, proxyFactory);

        instanceFactory = new StatefulInstanceFactory(contextFactory);

        // build the cache
        InstanceCache instanceCache = new SimpleInstanceCache();
        this.instanceCache = instanceCache;
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        VirtualOperation vop = (VirtualOperation) dispatchIndex.get(methodIndex);
        return vop;
    }

    private Index buildDispatchMethodMap() throws Exception {
        Class beanClass = getBeanClass();

        Index dispatchIndex = new Index(signatures);

        MethodSignature removeSignature = new MethodSignature("ejbRemove");
        RemoveMethod removeVop = new RemoveMethod(beanClass, removeSignature);
        for (Iterator iterator = dispatchIndex.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) entry.getKey();
            String methodName = methodSignature.getMethodName();
            if (methodSignature.isHomeMethod()) {
                if (methodName.startsWith("create")) {
                    String baseName = methodName.substring(6);
                    MethodSignature createSignature = new MethodSignature("ejbCreate" + baseName, methodSignature.getParameterTypes());
                    entry.setValue(new CreateMethod(beanClass, createSignature));
                } else if (methodName.startsWith("remove")) {
                    entry.setValue(removeVop);
                }
            } else {
                if (methodName.startsWith("remove") && methodSignature.getParameterTypes().length == 0) {
                    entry.setValue(removeVop);
                } else if (!methodName.startsWith("ejb") &&
                        !methodName.equals("setSessionContext") &&
                        !methodName.equals("afterBegin") &&
                        !methodName.equals("beforeCompletion") &&
                        !methodName.equals("afterCompletion")) {
                    MethodSignature signature = new MethodSignature(methodName, methodSignature.getParameterTypes());
                    entry.setValue(new BusinessMethod(beanClass, signature));
                }
            }
        }
        return dispatchIndex;

    }

    public boolean isBeanManagedTransactions() {
        return beanManagedTransactions;
    }

    public StatefulInstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public InstanceCache getInstanceCache() {
        return instanceCache;
    }

    public void doStart() throws Exception {
        super.doStart();
    }

    protected void destroy() throws PersistenceException {
        super.destroy();
    }

}
