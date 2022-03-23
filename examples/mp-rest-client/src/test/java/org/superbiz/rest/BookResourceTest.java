/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.superbiz.rest;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class BookResourceTest {

    @Deployment()
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(BookResource.class)
                .addClass(Book.class)
                .addClass(BookBean.class)
                .addClass(BookResourceClient.class)
                .addClass(ApplicationConfig.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .addAsResource("META-INF/microprofile-config.properties");
        return webArchive;
    }


    @Inject
    @RestClient
    private BookResourceClient bookResourceClient;

    @Test()
    public void testServerStatus(){
        bookResourceClient.addBook(new Book(1,"TomEE Book"));
    }

    @Test
    public void testBookResource(){
        bookResourceClient.addBook(new Book(1, "TomEE and MicroProfile Adventures"));
        bookResourceClient.addBook(new Book(2, "Top 10 Tomee Configuration Tips"));


        assertEquals(2, bookResourceClient.getListOfBooks().size());
        assertTrue(bookResourceClient.getBook(1).getName().equalsIgnoreCase("TomEE and MicroProfile Adventures"));

        bookResourceClient.deleteBook(1);
        assertEquals(1, bookResourceClient.getListOfBooks().size());
        assertTrue(bookResourceClient.getBook(2).getName().equalsIgnoreCase("Top 10 Tomee Configuration Tips"));

        bookResourceClient.updateBook(new Book(2, "Top 3 Tomee Configuration Tips"));
        assertEquals(1, bookResourceClient.getListOfBooks().size());
        assertTrue(bookResourceClient.getBook(2).getName().equalsIgnoreCase("Top 3 Tomee Configuration Tips"));
    }

}
