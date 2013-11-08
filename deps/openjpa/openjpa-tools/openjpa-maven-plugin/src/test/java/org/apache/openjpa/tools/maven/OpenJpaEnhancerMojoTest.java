/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.tools.maven;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.openjpa.tools.maven.OpenJpaEnhancerMojo;

/**
 * @version $Id: OpenJpaEnhancerMojoTest.java 9348 2009-04-03 22:59:47Z struberg $
 * @since 1.0.0
 */
public class OpenJpaEnhancerMojoTest extends AbstractMojoTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }
    
    
    public void testExecution() throws Exception {
      File testPom = new File( getBasedir(), "target/test-classes/projects/project-01/plugin-config.xml" );
      
      OpenJpaEnhancerMojo mojo = (OpenJpaEnhancerMojo) lookupMojo( "enhance", testPom );
      assertNotNull( mojo );
      
      mojo.classes = new File( getBasedir(), "target/test-classes/" );
      mojo.compileClasspathElements = new ArrayList<String>();
      mojo.compileClasspathElements.add( mojo.classes.getAbsolutePath() );
      
      mojo.execute();
    }

}
