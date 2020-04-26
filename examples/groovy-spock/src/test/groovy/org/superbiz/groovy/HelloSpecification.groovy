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
package org.superbiz.groovy

import org.apache.ziplock.JarLocation
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.ArchivePaths
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.runner.RunWith
import spock.lang.Specification

import jakarta.inject.Inject

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

@RunWith(ArquillianSputnik.class)
class HelloSpecification extends Specification {

    @Inject
    private Hello hello

    @Deployment
    def static WebArchive "create archive"() {
        ShrinkWrap.create(WebArchive.class)
                  .addAsLibraries(JarLocation.jarLocation(GroovyObject.class))
                  .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                  .addClasses(Hello.class)
    }

    def "Hello.hi() method should return 'hi'"() {
        when:
        println("Checking hello instance: " + hello)
        assertNotNull hello

        then:
        println("Comparing 'hi' to '" + hello.hi() + "'")
        assertEquals "hi", hello.hi()
    }
}
