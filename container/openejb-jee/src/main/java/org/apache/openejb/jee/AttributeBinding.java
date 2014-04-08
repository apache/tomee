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
package org.apache.openejb.jee;

import java.util.List;

/**
 * TODO this interface may not make sense any more because one of the two classes implementing it is not ins the schema any more
 * and its replacement, ConcurrentMethod, uses a single  NamedMethod rather than a list of Method.
 * @version $Rev$ $Date$
 */
public interface AttributeBinding<A> {

    public A getAttribute();

    public List<Method> getMethod();

}
