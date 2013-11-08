package org.apache.openjpa.tools.maven.test; 

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.tools.maven.testentity.TestEntity;

import junit.framework.TestCase;

public class ModifyDatabaseTest extends TestCase {

    
    /**
     * check if the generated classes have been enhanced.
     * @throws Exception
     */
    public void testDatabaseWrite() throws Exception
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "TestUnit" );
        assertNotNull( emf );
        
        EntityManager em = emf.createEntityManager();
        assertNotNull( em );
        
        try 
        {
            em.getTransaction().begin();
            
            TestEntity entity = new TestEntity();
            entity.setInt1( 4711 );
            entity.setString1( "myVal" );
            
            em.persist( entity );
            em.getTransaction().commit();
        }
        finally
        {
            em.close();
        }
    }

}
