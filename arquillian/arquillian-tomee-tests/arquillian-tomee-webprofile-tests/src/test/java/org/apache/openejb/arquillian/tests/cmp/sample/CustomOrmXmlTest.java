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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
@RunWith(Arquillian.class)
public class CustomOrmXmlTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, CustomOrmXmlTest.class.getSimpleName() + ".war")
                .addClasses(ActorBean.class, ActorDetails.class, LocalActor.class, LocalActorHome.class,
                        LocalMovie.class, LocalMovieHome.class, MovieBean.class, MovieDetails.class,
                        MoviesBusiness.class, MoviesBusinessBean.class, MoviesBusinessHome.class,
                        MoviesServlet.class)
                .addAsResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/custom-orm.xml"), "META-INF/custom-orm.xml")
                .addAsResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/persistence.xml"), "META-INF/persistence.xml")
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/openejb-jar.xml"), "openejb-jar.xml")
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/ejb-jar.xml"), "ejb-jar.xml")
                .addAsWebInfResource(new ClassLoaderAsset("org/apache/openejb/arquillian/tests/cmp/sample/web.xml"), "web.xml");

        System.out.println(archive.toString(true));
        return archive;
    }

    @Test
    @RunAsClient
    public void checkCmpJpaEntityORMMappings() throws Exception {
        final String output = IO.slurp(new URL(url.toExternalForm()));
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
