package org.superbiz.rest;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.entity.Product;
import org.superbiz.service.ProductService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ProductsTest {

    @Inject
    @RestClient
    private ProductRestClient productRestClient;

    @Deployment()
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(ProductRest.class, RestApplication.class)
                .addClasses(Product.class)
                .addClass(ProductService.class)
                .addClasses(ProductRestClient.class, TokenUtils.class)
                .addPackages(true, "com.nimbusds", "net.minidev.json")
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .addAsResource("META-INF/microprofile-config.properties")
                .addAsResource("jwt-john.json")
                .addAsResource("privateKey002.pem")
                .addAsResource("jwks.json");
        return webArchive;
    }

    @Test
    public void runningOfProductsApiTest() {

        assertEquals("running", productRestClient.status());
    }

    @Test
    public void shouldMakeProductFlow() throws Exception {
        Product productHuawei = new Product();
        productHuawei.setId(1);
        productHuawei.setName("Huawei P20 Pro");
        productHuawei.setPrice(new BigDecimal(820.41));
        productHuawei.setStock(6);

        int statusCode = productRestClient.addProduct("Bearer " + createJwtToken(), productHuawei).getStatus();

        assertEquals(Response.Status.CREATED.getStatusCode(), statusCode);


        Product productSamsung = new Product();
        productSamsung.setId(2);
        productSamsung.setName("Samsung S9");
        productSamsung.setPrice(new BigDecimal(844.42));
        productSamsung.setStock(2);

        statusCode = productRestClient.addProduct("Bearer " + createJwtToken(), productSamsung).getStatus();

        assertEquals(Response.Status.CREATED.getStatusCode(), statusCode);

        productSamsung.setStock(5);

        Response response = productRestClient.updateProduct("Bearer " + createJwtToken(), productSamsung);

        Product updatedProduct = response.readEntity(Product.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(5, updatedProduct.getStock().intValue());

        List<Product> products = productRestClient.getProductList("Bearer " + createJwtToken());

        assertEquals(2, products.size());

        statusCode = productRestClient.deleteProduct("Bearer " + createJwtToken(), 2).getStatus();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), statusCode);
    }

    private String createJwtToken() throws Exception {
        return TokenUtils.generateJWTString("jwt-john.json", "privateKey002.pem");
    }
}
