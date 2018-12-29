package org.superbiz.rest;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.entity.Product;
import org.superbiz.service.ProductService;

import java.net.URL;
import java.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ProductsTest {

    private final static Logger LOGGER = Logger.getLogger(ProductsTest.class.getName());
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Product.class, ProductService.class, ProductsTest.class)
                .addClasses(ProductRest.class, RestApplication.class)
//                .addClass(MoviesMPJWTConfigurationProvider.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml");

        System.out.println(webArchive.toString(true));

        return webArchive;
    }

    @Test
    public void runningOfProductsApiTest() {

        final WebClient webClient = WebClient
                .create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                        singletonList(new LoggingFeature()), null);


        //Testing rest endpoint deployment (GET  without security header)
        String responsePayload = webClient.reset().path("/rest/store/").get(String.class);
        LOGGER.info("responsePayload = " + responsePayload);
        assertTrue(responsePayload.equalsIgnoreCase("running"));
    }

    @Test
    public void createProductTest() {

        throw new RuntimeException("TODO Implement!");
    }

    @Test
    public void getAllProductsTest() {

        throw new RuntimeException("TODO Implement!");
    }

    @Test
    public void getProductWithIdTest() {

        throw new RuntimeException("TODO Implement!");
    }

    @Test
    public void updateProductTest() {

        throw new RuntimeException("TODO Implement!");
    }

    @Test
    public void deleteProductTest() {

        throw new RuntimeException("TODO Implement!");
    }
}
