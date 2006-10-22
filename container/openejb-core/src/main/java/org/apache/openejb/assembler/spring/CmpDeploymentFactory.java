/**
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
package org.apache.openejb.assembler.spring;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import org.apache.openejb.SystemException;
import org.apache.openejb.BeanType;
import org.apache.openejb.core.CoreDeploymentInfo;

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

    protected BeanType getComponentType() {
        return BeanType.CMP_ENTITY;
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
