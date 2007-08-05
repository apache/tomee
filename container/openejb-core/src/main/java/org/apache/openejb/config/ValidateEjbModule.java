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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

/**
 * @version $Rev$ $Date$
 */
public class ValidateEjbModule implements DynamicDeployer {

    public ValidateEjbModule() {
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            deploy(ejbModule);
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            deploy(clientModule);
        }
        return appModule;
    }

    public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {
//        return deployer.deploy(clientModule);
        return null;
    }

    public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {

        EjbValidator validator = new EjbValidator();
        ValidationResults set = validator.validateJar(ejbModule);
        if (set.hasErrors() || set.hasFailures()) {
            Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_VALIDATION, "org.apache.openejb.config.rules");

            ValidationError[] errors = set.getErrors();
            for (int j = 0; j < errors.length; j++) {
                ValidationError e = errors[j];
                String ejbName = e.getComponentName();
                logger.error(e.getPrefix() + " ... " + ejbName + ":\t" + e.getMessage(2));
            }
            ValidationFailure[] failures = set.getFailures();
            for (int j = 0; j < failures.length; j++) {
                ValidationFailure e = failures[j];
                logger.error(e.getPrefix() + " ... " + e.getComponentName() + ":\t" + e.getMessage(2));
            }

            throw new ValidationFailedException("Jar failed validation.", set);
        }
        return ejbModule;
    }

}
