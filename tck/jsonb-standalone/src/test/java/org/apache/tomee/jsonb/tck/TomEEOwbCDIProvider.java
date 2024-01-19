/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tomee.jsonb.tck;

import jakarta.enterprise.inject.spi.CDI;
import org.apache.webbeans.container.OwbCDIProvider;

// CDI Integration TCKs rely on undefined CDI behaviour: they expect that calling CDI.current() throws an Exception when CDI is not started
// OWB doesn't do that, so we try to achieve this here as a workaround by calling CDI#getBeanManager before returning CDI
public class TomEEOwbCDIProvider extends OwbCDIProvider {
    @Override
    public CDI<Object> getCDI() {
        CDI<Object> cdi = super.getCDI();
        cdi.getBeanManager();

        return cdi;
    }
}