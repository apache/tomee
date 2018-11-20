package org.superbiz.rest;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

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
        bookResourceClient.addBook(new Book(2, "Top 10 Tomee Configuraiton Tips"));


        assertTrue(bookResourceClient.getListOfBooks().size() == 2);
        assertTrue(bookResourceClient.getBook(1).getName().equalsIgnoreCase("TomEE and MicroProfile Adventures"));

        bookResourceClient.deleteBook(1);
        assertTrue(bookResourceClient.getListOfBooks().size() == 1);
        assertTrue(bookResourceClient.getBook(2).getName().equalsIgnoreCase("Top 10 Tomee Configuraiton Tips"));

        bookResourceClient.updateBook(new Book(2, "Top 3 Tomee Configuraiton Tips"));
        assertTrue(bookResourceClient.getListOfBooks().size() == 1);
        assertTrue(bookResourceClient.getBook(2).getName().equalsIgnoreCase("Top 3 Tomee Configuraiton Tips"));
    }

}
