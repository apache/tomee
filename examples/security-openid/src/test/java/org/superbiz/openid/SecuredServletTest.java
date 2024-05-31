package org.superbiz.openid;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.htmlunit.TextPage;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(ArquillianExtension.class)
public class SecuredServletTest {
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer()
            .withRealmImportFile("keycloak-realm.json");

    @Deployment
    public static WebArchive createDeployment() {
        String mpConfig = "openid.provider-uri = " + KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/tomee" + "\n"
                + "openid.client-id = tomee\n"
                + "openid.client-secret = tomee-client-secret\n";

        return ShrinkWrap.create(WebArchive.class, "ROOT.war")
                .addClasses(SecuredServlet.class, OpenIdConfig.class)
                .addAsResource("META-INF/beans.xml")
                .addAsResource(new StringAsset(mpConfig), "META-INF/microprofile-config.properties");
    }

    @ArquillianResource
    private URL url;

    @Test
    @RunAsClient
    @Disabled("FIXME")
    public void test() throws Exception {
        try (WebClient webClient = new WebClient()) {
            HtmlPage htmlPage = webClient.getPage(url + "/secured");
            assertTrue(htmlPage.getUrl().toString().startsWith(KEYCLOAK_CONTAINER.getAuthServerUrl() + "/realms/tomee/protocol/openid-connect/auth"));

            HtmlForm loginForm = htmlPage.getForms().get(0);
            loginForm.getInputByName("username").setValue("tomee-user");
            loginForm.getInputByName("password").setValue("tomee");
            TextPage securedServletPage = loginForm.getInputByName("login").click();

            assertEquals("Hello, tomee-user", securedServletPage.getContent());
        }
    }
}
