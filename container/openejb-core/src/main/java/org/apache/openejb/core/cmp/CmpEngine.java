/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.SystemException;
import org.apache.openejb.OpenEJBException;

import javax.persistence.EntityTransaction;
import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.util.List;
import java.lang.reflect.Method;

public interface CmpEngine {

    Object createBean(EntityBean entity, ThreadContext callContext) throws CreateException;

    Object loadBean(ThreadContext callContext, Object primaryKey);

    void removeBean(ThreadContext callContext);

    List<Object> queryBeans(ThreadContext callContext, Method queryMethod, Object[] args) throws FinderException;

    List<Object> queryBeans(CoreDeploymentInfo deploymentInfo, String signature, Object[] args) throws FinderException;

    void deploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException;

    void undeploy(CoreDeploymentInfo deploymentInfo) throws OpenEJBException;

    boolean isEmpty();
}
