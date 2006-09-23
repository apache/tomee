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
 *    please contact info@openejb.org.
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
 * Copyright 2006 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.assembler.spring;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.openejb.SystemException;
import org.openejb.core.CoreDeploymentInfo;

/**
 * @org.apache.xbean.XBean element="cmpDeployment"
 */
public class CmpDeploymentFactory extends AbstractDeploymentFactory {
    private boolean reentrant;
    private String[] cmpFields;
    private String primKeyField;
    private final Map<String, String> queries = new TreeMap<String, String>();
    protected String pkClass;

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    public String getPkClass() {
        return pkClass;
    }

    public void setPkClass(String pkClass) {
        this.pkClass = pkClass;
    }

    public String[] getCmpFields() {
        return cmpFields;
    }

    public void setCmpFields(String[] cmpFields) {
        this.cmpFields = cmpFields;
    }

    public String getPrimKeyField() {
        return primKeyField;
    }

    public void setPrimKeyField(String primKeyField) {
        this.primKeyField = primKeyField;
    }

    /**
     * @org.apache.xbean.Map entryName="query" keyName="method"
     */
    public Map<String, String> getQueries() {
        return new TreeMap<String, String>(queries);
    }

    public void setQueries(Map<String, String> queries) {
        this.queries.clear();
        this.queries.putAll(queries);
    }

    protected boolean isBeanManagedTransaction() {
        return false;
    }

    protected byte getComponentType() {
        return CoreDeploymentInfo.CMP_ENTITY;
    }

    protected CoreDeploymentInfo createDeploymentInfo() throws SystemException {
        CoreDeploymentInfo deploymentInfo = super.createDeploymentInfo();
        deploymentInfo.setCmrFields(cmpFields == null? new String[0] : cmpFields);
        deploymentInfo.setIsReentrant(reentrant);
        if (primKeyField != null) {
            try {
                deploymentInfo.setPrimKeyField(primKeyField);
            } catch (NoSuchFieldException e) {
                throw new SystemException("Can not set prim-key-field on deployment " + id, e);
            }
        }

        Class home = deploymentInfo.getHomeInterface();
        Class localHome = deploymentInfo.getLocalHomeInterface();
        for (Map.Entry<String, String> entry : queries.entrySet()) {
            String signatureText = entry.getKey();
            MethodSignature methodSignature = new MethodSignature(signatureText);
            String query = entry.getValue();


            if (home != null) {
                Method homeMethod = methodSignature.getMethod(home);
                if (homeMethod != null) {
                    deploymentInfo.addQuery(homeMethod, query);
                }
            }

            if (localHome != null) {
                Method localHomeMethod = methodSignature.getMethod(localHome);
                if (localHomeMethod != null) {
                    deploymentInfo.addQuery(localHomeMethod, query);
                }
            }
        }

        return deploymentInfo;
    }
}
