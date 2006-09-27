/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
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
 *    (http://openejb.sf.net/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;


/**
 * This class is a bit crufty.  Need something like this, but not static
 * and more along the lines of a collection of deployments registered as gbeans
 */
public class DeploymentIndex {
    // todo delete me
    private static DeploymentIndex deploymentIndex = new DeploymentIndex();

    public static DeploymentIndex getInstance() {
        return deploymentIndex;
    }

    /**
     * The deployment lookup table.
     */
    private RpcEjbDeployment[] deployments = new RpcEjbDeployment[1];

    /**
     * Index from the deployment id to the index (Integer) number in the deployments lookup table
     */
    private final HashMap deploymentIdToIndex = new HashMap();

    /**
     * Index from jndi name to the index (Integer) number in the deployments lookup table
     */
    private final HashMap jndiNameToIndex = new HashMap();

    /**
     * GBean reference collection that we watch for new deployments to register
     */
    private Collection ejbDeployments;

    protected DeploymentIndex() {
    }

    public DeploymentIndex(Collection ejbDeployments) {
        DeploymentIndex.deploymentIndex = this;
    }

    public synchronized void doStart() throws Exception {
        deployments = new RpcEjbDeployment[ejbDeployments.size() + 1];
        Iterator iterator = ejbDeployments.iterator();
        for (int i = 1; i < deployments.length && iterator.hasNext(); i++) {
            RpcEjbDeployment deployment = (RpcEjbDeployment) iterator.next();
            deployment = (RpcEjbDeployment) deployment.getUnmanagedReference();
            deployments[i] = deployment;
            deploymentIdToIndex.put(deployment.getContainerId(), new Integer(i));
            addJNDINames(deployment, i);
        }
    }

    private void addJNDINames(RpcEjbDeployment deployment, int i) {
        String[] jnidNames = deployment.getJndiNames();
        if (jnidNames != null) {
            for (int j = 0; j < jnidNames.length; j++) {
                String jnidName = jnidNames[j];
                jndiNameToIndex.put(jnidName, new Integer(i));
            }
        }
    }

    public synchronized void doStop() throws Exception {
        deploymentIdToIndex.clear();
        Arrays.fill(deployments, null);
        jndiNameToIndex.clear();
    }

    public synchronized void doFail() {
        deploymentIdToIndex.clear();
        Arrays.fill(deployments, null);
        jndiNameToIndex.clear();
    }

    public synchronized void addDeployment(RpcEjbDeployment deployment) {
        deployment = (RpcEjbDeployment) deployment.getUnmanagedReference();
        Object containerId = deployment.getContainerId();
        if (deploymentIdToIndex.containsKey(containerId)) {
            return;
        }

        int i = deployments.length;

        RpcEjbDeployment[] newArray = new RpcEjbDeployment[i + 1];
        System.arraycopy(deployments, 0, newArray, 0, i);
        deployments = newArray;

        deployments[i] = deployment;
        deploymentIdToIndex.put(containerId, new Integer(i));
        addJNDINames(deployment, i);
    }

    public synchronized void removeDeployment(RpcEjbDeployment deployment) {
        Integer index = (Integer) deploymentIdToIndex.remove(deployment.getContainerId());
        if (index != null) {
            deployments[index.intValue()] = null;
        }

        String[] jnidNames = deployment.getJndiNames();
        for (int i = 0; i < jnidNames.length; i++) {
            String jnidName = jnidNames[i];
            jndiNameToIndex.remove(jnidName);
        }
    }

    public synchronized int length() {
        return deployments.length;
    }

    public synchronized int getDeploymentIndex(Object containerId) {
        return getDeploymentIndex((String) containerId);
    }

    public synchronized int getDeploymentIndex(String containerId) {
        Integer index = (Integer) deploymentIdToIndex.get(containerId);
        return (index == null) ? -1 : index.intValue();
    }

    public synchronized int getDeploymentIndexByJndiName(String jndiName) {
        Integer index = (Integer) jndiNameToIndex.get(jndiName);
        return (index == null) ? -1 : index.intValue();
    }

    public synchronized RpcEjbDeployment getDeployment(String containerId) throws DeploymentNotFoundException {
        int deploymentIndex = getDeploymentIndex(containerId);
        if (deploymentIndex < 0) {
            throw new DeploymentNotFoundException(containerId);
        }
        return getDeployment(deploymentIndex);
    }

    public synchronized EjbDeployment getDeployment(Integer index) {
        return (index == null) ? null : getDeployment(index.intValue());
    }

    public synchronized EjbDeployment getDeploymentByJndiName(String jndiName) {
        return getDeployment(getDeploymentIndexByJndiName(jndiName));
    }

    public synchronized RpcEjbDeployment getDeployment(int index) {
        return deployments[index];
    }


}



