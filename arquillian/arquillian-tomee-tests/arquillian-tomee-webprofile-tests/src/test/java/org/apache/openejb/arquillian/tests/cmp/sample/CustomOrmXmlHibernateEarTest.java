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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.cmp.sample;

import org.apache.ziplock.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class CustomOrmXmlHibernateEarTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() {

        final JavaArchive clientJar = ShrinkWrap.create(JavaArchive.class, "client.jar")
                .addClasses(ActorDetails.class, LocalActor.class, LocalActorHome.class,
                        LocalMovie.class, LocalMovieHome.class, MovieDetails.class,
                        MoviesBusiness.class, MoviesBusinessHome.class);

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb-jar.jar")
                .addClasses(ActorBean.class, MovieBean.class, MovieDetails.class, MoviesBusinessBean.class)
                .addAsResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/custom-orm.xml"), "META-INF/custom-orm.xml")
                .addAsResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/persistence-hibernate.xml"), "META-INF/persistence.xml")
                .addAsResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/openejb-jar.xml"), "META-INF/openejb-jar.xml")
                .addAsResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/ejb-jar.xml"), "META-INF/ejb-jar.xml");

        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(MoviesServlet.class)
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/web.xml"), "web.xml");

        final File[] hibernateDependencies = Maven.resolver().resolve(
            "org.hibernate:hibernate-core:5.6.7.Final",
            "org.hibernate.common:hibernate-commons-annotations:5.1.2.Final",
            "antlr:antlr:2.7.7",
            "com.fasterxml:classmate:1.5.1",
            "org.jboss:jandex:2.4.2.Final",
            "org.jboss.logging:jboss-logging:3.4.3.Final",
            "net.bytebuddy:byte-buddy:1.12.8",
            "mysql:mysql-connector-java:5.1.13"
        ).withoutTransitivity().asFile();
        
        final EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
                .addAsLibrary(clientJar)
                .addAsLibraries(hibernateDependencies)
                .addAsModule(ejbJar)
                .addAsModule(testWar);

        System.out.println(archive.toString(true));
        return archive;
    }


    @Test
    @RunAsClient
    public void checkCmpJpaEntityORMMappings() throws Exception {
        final String output = IO.slurp(new URL(url.toExternalForm() + "/test/test/"));
        System.out.println(output);

        Assert.assertTrue(output.contains("TABLE_NAME: ACTOR, COLUMN_NAME: ACTORID, DATA_TYPE: INTEGER, CHARACTER_MAXIMUM_LENGTH: null"));
        Assert.assertTrue(output.contains("TABLE_NAME: ACTOR, COLUMN_NAME: ACTOR_NAME, DATA_TYPE: CHARACTER VARYING, CHARACTER_MAXIMUM_LENGTH: 250"));
        Assert.assertTrue(output.contains("TABLE_NAME: ACTOR_MOVIE, COLUMN_NAME: ACTORS_ACTORID, DATA_TYPE: INTEGER, CHARACTER_MAXIMUM_LENGTH: null"));
        Assert.assertTrue(output.contains("TABLE_NAME: ACTOR_MOVIE, COLUMN_NAME: MOVIES_MOVIEID, DATA_TYPE: INTEGER, CHARACTER_MAXIMUM_LENGTH: null"));
        Assert.assertTrue(output.contains("TABLE_NAME: MOVIE, COLUMN_NAME: MOVIEID, DATA_TYPE: INTEGER, CHARACTER_MAXIMUM_LENGTH: null"));
        Assert.assertTrue(output.contains("TABLE_NAME: MOVIE, COLUMN_NAME: GENRE, DATA_TYPE: CHARACTER VARYING, CHARACTER_MAXIMUM_LENGTH: 255"));
        Assert.assertTrue(output.contains("TABLE_NAME: MOVIE, COLUMN_NAME: MOVIE_NAME, DATA_TYPE: CHARACTER VARYING, CHARACTER_MAXIMUM_LENGTH: 250"));

        final String[] split = output.split("\r?\n");
        Assert.assertEquals(7, split.length);
    }
}