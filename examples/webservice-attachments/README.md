index-group=Web Services
type=page
status=published
title=Webservice Attachments
~~~~~~

*Help us document this example! Click the blue pencil icon in the upper right to edit this page.*

## AttachmentImpl

    package org.superbiz.attachment;
    
    import javax.activation.DataHandler;
    import javax.activation.DataSource;
    import javax.ejb.Stateless;
    import javax.jws.WebService;
    import javax.xml.ws.BindingType;
    import javax.xml.ws.soap.SOAPBinding;
    import java.io.IOException;
    import java.io.InputStream;
    
    /**
     * This is an EJB 3 style pojo stateless session bean
     * Every stateless session bean implementation must be annotated
     * using the annotation @Stateless
     * This EJB has a single interface: {@link AttachmentWs} a webservice interface.
     */
    @Stateless
    @WebService(
            portName = "AttachmentPort",
            serviceName = "AttachmentWsService",
            targetNamespace = "http://superbiz.org/wsdl",
            endpointInterface = "org.superbiz.attachment.AttachmentWs")
    @BindingType(value = SOAPBinding.SOAP12HTTP_MTOM_BINDING)
    public class AttachmentImpl implements AttachmentWs {
    
        public String stringFromBytes(byte[] data) {
            return new String(data);
        }
    
        public String stringFromDataSource(DataSource source) {
    
            try {
                InputStream inStr = source.getInputStream();
                int size = inStr.available();
                byte[] data = new byte[size];
                inStr.read(data);
                inStr.close();
                return new String(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    
        public String stringFromDataHandler(DataHandler handler) {
    
            try {
                return (String) handler.getContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

## AttachmentWs

    package org.superbiz.attachment;
    
    import javax.activation.DataHandler;
    import javax.jws.WebService;
    
    /**
     * This is an EJB 3 webservice interface to send attachments throughout SAOP.
     */
    @WebService(targetNamespace = "http://superbiz.org/wsdl")
    public interface AttachmentWs {
    
        public String stringFromBytes(byte[] data);
    
        // Not working at the moment with SUN saaj provider and CXF
        //public String stringFromDataSource(DataSource source);
    
        public String stringFromDataHandler(DataHandler handler);
    }

## ejb-jar.xml

    <ejb-jar/>

## AttachmentTest

    package org.superbiz.attachment;
    
    import junit.framework.TestCase;
    
    import javax.activation.DataHandler;
    import javax.activation.DataSource;
    import javax.mail.util.ByteArrayDataSource;
    import javax.naming.Context;
    import javax.naming.InitialContext;
    import javax.xml.namespace.QName;
    import javax.xml.ws.BindingProvider;
    import javax.xml.ws.Service;
    import javax.xml.ws.soap.SOAPBinding;
    import java.net.URL;
    import java.util.Properties;
    
    public class AttachmentTest extends TestCase {
    
        //START SNIPPET: setup	
        private InitialContext initialContext;
    
        protected void setUp() throws Exception {
    
            Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
            properties.setProperty("openejb.embedded.remotable", "true");
    
            initialContext = new InitialContext(properties);
        }
        //END SNIPPET: setup    
    
        /**
         * Create a webservice client using wsdl url
         *
         * @throws Exception
         */
        //START SNIPPET: webservice
        public void testAttachmentViaWsInterface() throws Exception {
            Service service = Service.create(
                    new URL("http://127.0.0.1:4204/AttachmentImpl?wsdl"),
                    new QName("http://superbiz.org/wsdl", "AttachmentWsService"));
            assertNotNull(service);
    
            AttachmentWs ws = service.getPort(AttachmentWs.class);
    
            // retrieve the SOAPBinding
            SOAPBinding binding = (SOAPBinding) ((BindingProvider) ws).getBinding();
            binding.setMTOMEnabled(true);
    
            String request = "tsztelak@gmail.com";
    
            // Byte array
            String response = ws.stringFromBytes(request.getBytes());
            assertEquals(request, response);
    
            // Data Source
            DataSource source = new ByteArrayDataSource(request.getBytes(), "text/plain; charset=UTF-8");
    
            // not yet supported !
    //        response = ws.stringFromDataSource(source);
    //        assertEquals(request, response);
    
            // Data Handler
            response = ws.stringFromDataHandler(new DataHandler(source));
            assertEquals(request, response);
        }
        //END SNIPPET: webservice
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.attachment.AttachmentTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/webservice-attachments
    INFO - openejb.base = /Users/dblevins/examples/webservice-attachments
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/webservice-attachments/target/classes
    INFO - Beginning load: /Users/dblevins/examples/webservice-attachments/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/webservice-attachments/classpath.ear
    INFO - Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
    INFO - Auto-creating a container for bean AttachmentImpl: Container(type=STATELESS, id=Default Stateless Container)
    INFO - Enterprise application "/Users/dblevins/examples/webservice-attachments/classpath.ear" loaded.
    INFO - Assembling app: /Users/dblevins/examples/webservice-attachments/classpath.ear
    INFO - Created Ejb(deployment-id=AttachmentImpl, ejb-name=AttachmentImpl, container=Default Stateless Container)
    INFO - Started Ejb(deployment-id=AttachmentImpl, ejb-name=AttachmentImpl, container=Default Stateless Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/webservice-attachments/classpath.ear)
    INFO - Initializing network services
    INFO - Creating ServerService(id=httpejbd)
    INFO - Creating ServerService(id=cxf)
    INFO - Creating ServerService(id=admin)
    INFO - Creating ServerService(id=ejbd)
    INFO - Creating ServerService(id=ejbds)
    INFO - Initializing network services
      ** Starting Services **
      NAME                 IP              PORT  
      httpejbd             127.0.0.1       4204  
      admin thread         127.0.0.1       4200  
      ejbd                 127.0.0.1       4201  
      ejbd                 127.0.0.1       4203  
    -------
    Ready!
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.034 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
