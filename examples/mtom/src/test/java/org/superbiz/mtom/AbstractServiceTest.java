package org.superbiz.mtom;

import org.apache.openejb.junit.ApplicationComposer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.ws.WebServiceRef;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RunWith(ApplicationComposer.class)
public abstract class AbstractServiceTest {

    @WebServiceRef
    private Service service;

    @Test
    public void test() throws IOException {
        final Response response = this.service.convertToBytes(new Request("hello world!"));

        Assert.assertNotNull(response.getResult());

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        response.getResult().writeTo(outputStream);

        Assert.assertTrue("datahandler is empty", outputStream.size() > 0);
    }

}
