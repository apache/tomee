/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin;

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class PreconfiguredTomEEXMlTest {
    @Rule
    public TestRule TMPRule = RuleChain
            .outerRule(new TestRule() {
                @Override
                public Statement apply(final Statement base, final Description description) {
                    return new Statement() {
                        @Override
                        public void evaluate() throws Throwable {
                            IO.writeString(
                                    new File(Files.mkdirs(new File(PreconfiguredTomEEXMlTest.this.catalinaBase, "conf")), "tomee.xml"),
                                    "<tomee><Resource id=\"foo\" type=\"DataSource\" /><Deployments dir=\"missing\" /></tomee>");
                            base.evaluate();
                        }
                    };
                }
            })
            .around(new TestRule() {
                @Override
                public Statement apply(final Statement base, final Description description) {
                    return new TomEEMavenPluginRule().apply(base, null, PreconfiguredTomEEXMlTest.this);
                }
            });

    @Config
    public final File catalinaBase = new File("target/PreconfiguredTomEEXMlTest");

    @Config
    public final boolean overrideOnUnzip = false;

    @Test
    public void confIsCorrectEvenIfWeEnrichedTheTomEEXmlForApps() throws Exception {
        assertEquals(
                "<tomee><Resource id=\"foo\" type=\"DataSource\" /><Deployments dir=\"missing\" />  <Deployments dir=\"apps\" /></tomee>",
                IO.slurp(new File(catalinaBase, "conf/tomee.xml")).replace("\n", "").replace("\r", ""));
    }
}
