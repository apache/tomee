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
package org.apache.openejb;

import junit.framework.TestCase;
import junit.framework.Test;

/**
 * Surefire hack for running iTest class via maven.  Not for use in IDEs
 *
 * At some point Surefire decided it wasn't going to run anything that wasn't an
 * immediate subclass of TestCase, even if your parent's parent was TestCase or
 * your class was assignable to TestCase.
 *
 * So this class is a dirty hack to get the iTest class to run with the build.
 *
 * NOTE: use the iTest class in your IDE instead of this one.
 *
 * @see iTest
 * @version $Revision$ $Date$
 */
public class SomeoneBrokeSurefireAndThisIsADirtyHackForItTest extends TestCase {
    public static Test suite() {
        System.setProperty("openejb.assembler", org.apache.openejb.assembler.classic.Assembler.class.getName());
        System.setProperty("openejb.deployments.classpath.include", ".*openejb-itests-*.*");
        System.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        return iTest.suite();
    }
}
