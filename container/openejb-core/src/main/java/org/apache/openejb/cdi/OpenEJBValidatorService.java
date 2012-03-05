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


package org.apache.openejb.cdi;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.webbeans.spi.ValidatorService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * @version $Rev:$ $Date:$
 */
public class OpenEJBValidatorService implements ValidatorService {
    @Override
    public ValidatorFactory getDefaultValidatorFactory() {
        try {
            return (ValidatorFactory) new InitialContext().lookup("java:comp/ValidatorFactory");
        } catch (NamingException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public Validator getDefaultValidator() {
        try {
            return (Validator) new InitialContext().lookup("java:comp/Validator");
        } catch (NamingException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }
}
