/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.superbiz.cdi.stereotype;

/**
 * without @Mock annotation which specifies this class as an alternative
 * you'll have this exception:
 *
 * Caused by: jakarta.enterprise.inject.AmbiguousResolutionException: There is more than one api type with : org.superbiz.cdi.stereotype.Society with qualifiers : Qualifiers: [@jakarta.enterprise.inject.Default()]
 * for injection into Field Injection Point, field name :  society, Bean Owner : [Journey, Name:null, WebBeans Type:ENTERPRISE, API Types:[org.superbiz.cdi.stereotype.Journey,java.lang.Object], Qualifiers:[jakarta.enterprise.inject.Any,jakarta.enterprise.inject.Default]]
 * found beans:
 * AirOpenEJB, Name:null, WebBeans Type:MANAGED, API Types:[org.superbiz.cdi.stereotype.Society,org.superbiz.cdi.stereotype.AirOpenEJB,java.lang.Object], Qualifiers:[jakarta.enterprise.inject.Any,jakarta.enterprise.inject.Default]
 * LowCostCompanie, Name:null, WebBeans Type:MANAGED, API Types:[org.superbiz.cdi.stereotype.Society,org.superbiz.cdi.stereotype.LowCostCompanie,java.lang.Object], Qualifiers:[jakarta.enterprise.inject.Any,jakarta.enterprise.inject.Default]
 *
 * because 2 implementations match the same injection point (Journey.society).
 */
@Mock
public class AirOpenEJB implements Society {

    @Override
    public String category() {
        return "simply the best";
    }
}
