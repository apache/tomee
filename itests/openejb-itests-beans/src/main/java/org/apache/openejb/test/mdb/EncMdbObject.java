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
package org.apache.openejb.test.mdb;

import org.apache.openejb.test.TestFailureException;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface EncMdbObject {

    public void lookupEntityBean() throws TestFailureException;

    public void lookupStatefulBean() throws TestFailureException;

    public void lookupStatelessBean() throws TestFailureException;

    public void lookupStatelessBusinessLocal() throws TestFailureException;

    public void lookupStatelessBusinessRemote() throws TestFailureException;

    public void lookupStatefulBusinessLocal() throws TestFailureException;

    public void lookupStatefulBusinessRemote() throws TestFailureException;

    public void lookupResource() throws TestFailureException;

    public void lookupJMSConnectionFactory() throws TestFailureException;

    public void lookupPersistenceUnit() throws TestFailureException;

    public void lookupPersistenceContext() throws TestFailureException;

    public void lookupMessageDrivenContext() throws TestFailureException;

    public void lookupStringEntry() throws TestFailureException;

    public void lookupDoubleEntry() throws TestFailureException;

    public void lookupLongEntry() throws TestFailureException;

    public void lookupFloatEntry() throws TestFailureException;

    public void lookupIntegerEntry() throws TestFailureException;

    public void lookupShortEntry() throws TestFailureException;

    public void lookupBooleanEntry() throws TestFailureException;

    public void lookupByteEntry() throws TestFailureException;

    public void lookupCharacterEntry() throws TestFailureException;
}
