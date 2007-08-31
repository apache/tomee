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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.rules;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiReference;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class CheckInjectionTargets extends ValidationBase {
    public void validate(EjbModule ejbModule) {

        for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            List<JndiReference> entries = new ArrayList<JndiReference>();
            entries.addAll(bean.getEjbLocalRef());
            entries.addAll(bean.getEjbRef());
            entries.addAll(bean.getEnvEntry());
            entries.addAll(bean.getMessageDestinationRef());
            entries.addAll(bean.getPersistenceContextRef());
            entries.addAll(bean.getPersistenceUnitRef());
            entries.addAll(bean.getResourceEnvRef());
            entries.addAll(bean.getResourceRef());
            entries.addAll(bean.getServiceRef());

            for (JndiReference reference : entries) {
                // check injection target
                for (InjectionTarget target : reference.getInjectionTarget()) {
                    boolean classPrefix = false;

                    String name = target.getInjectionTargetName();
                    if (name.startsWith(target.getInjectionTargetClass() + "/")) {
                        classPrefix = true;
                        name = name.substring(target.getInjectionTargetClass().length() + 1);
                    }

                    String shortNameInvalid = name;

                    if (name.startsWith("set") && name.length() >= 4 && Character.isUpperCase(name.charAt(3))) {
                        StringBuffer correctName = new StringBuffer(name);
                        correctName.delete(0, 3);
                        correctName.setCharAt(0, Character.toLowerCase(correctName.charAt(0)));
                        String shortNameCorrect = correctName.toString();
                        if (classPrefix) correctName.insert(0, target.getInjectionTargetClass() + "/");

                        warn(bean, "injectionTarget.nameContainsSet", target.getInjectionTargetName(), shortNameInvalid, shortNameCorrect, correctName, reference.getName(), reference.getClass().getSimpleName());
                        target.setInjectionTargetName(correctName.toString());
                    }
                }
            }
        }
    }

}
