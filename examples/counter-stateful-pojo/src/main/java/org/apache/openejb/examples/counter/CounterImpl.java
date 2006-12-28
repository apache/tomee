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
package org.apache.openejb.examples.counter;

import javax.ejb.Stateful;

/**
 * This is an EJB 3 style pojo stateful session bean
 * Every stateful session bean implementation must be annotated
 * using the annotation @Stateful
 * This EJB has 2 business interfaces: CounterRemote, a remote business
 * interface, and CounterLocal, a local business interface
 *
 * Per EJB3 rules when the @Remote or @Local annotation isn't present
 * in the bean class (this class), all interfaces are considered
 * local unless explicitly annotated otherwise.  If you look
 * in the CounterRemote interface, you'll notice it uses the @Remote
 * annotation while the CounterLocal interface is not annotated relying
 * on the EJB3 default rules to make it a local interface.
 */
@Stateful
public class CounterImpl implements CounterLocal, CounterRemote {

    private int count = 0;

    public int increment() {
        return ++count;
    }

    public int reset() {
        return (count = 0);
    }

}
