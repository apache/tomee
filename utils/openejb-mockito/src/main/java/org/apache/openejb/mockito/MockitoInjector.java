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
package org.apache.openejb.mockito;

import javax.enterprise.inject.spi.CDI;

import org.apache.openejb.Injection;
import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.annotation.NamedLiteral;

/**
 * this class is instantiated when the FallbackPropertyInjector is set
 * it is generally when the container is started
 * it will resolve @EJB mock injections.
 * You don't need it if you don't use any @EJB injection.
 */
public class MockitoInjector implements FallbackPropertyInjector {

    @Override
    public Object getValue(Injection injection) {
    	try {
    		Class<?> targetType = injection.getTarget().getDeclaredField(injection.getName()).getType();
    		try {
    			return CDI.current().select(targetType, DefaultLiteral.INSTANCE).get();
    		} catch (Exception e) {
    			return CDI.current().select(targetType, new NamedLiteral(injection.getName())).get();
    		}
    	} catch (NoSuchFieldException noSuchFieldException) {
    		return null;
    	}
    }
}
