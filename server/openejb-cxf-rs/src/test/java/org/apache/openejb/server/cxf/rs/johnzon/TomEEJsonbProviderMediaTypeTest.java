package org.apache.openejb.server.cxf.rs.johnzon;

import jakarta.ws.rs.core.MediaType;
import junit.framework.TestCase;

public class TomEEJsonbProviderMediaTypeTest extends TestCase {

    public void testIsJson() {

        // easy scenarios
        assertTrue(TomEEJsonbProvider.isJson(MediaType.APPLICATION_JSON_TYPE));
        assertFalse(TomEEJsonbProvider.isJson(MediaType.APPLICATION_XML_TYPE));
        assertFalse(TomEEJsonbProvider.isJson(MediaType.WILDCARD_TYPE));

        // easy scenarios
        assertTrue(TomEEJsonbProvider.isJson(MediaType.APPLICATION_JSON_PATCH_JSON_TYPE));
        assertTrue(TomEEJsonbProvider.isJson(new MediaType("application", "foo+json")));
    }
}