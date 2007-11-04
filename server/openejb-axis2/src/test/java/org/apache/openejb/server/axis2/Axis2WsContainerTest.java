/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis2;

import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.axis2.pojo.PojoWsContainer;
import org.apache.openejb.server.axis2.testdata.simple.HelloWorld;
import org.apache.openejb.server.httpd.HttpRequest;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;

public class Axis2WsContainerTest extends Axis2AbstractTestCase {
	private final String RESOURCE_PATH = testDir+"/resources/";
    public Axis2WsContainerTest(String testName) {
        super(testName);
    }

    /*
    public void testDocInvokeWithWSDL() throws Exception {
        invokeWithWSDL("BareDocLitService", "org.apache.openejb.server.axis2.testdata.doclitbare.BareDocLitService",
                       "test_service_doclitbare.wsdl", "test_service_doclitbare_request.xml");
    }
    */
    
    public void testRPCInvokeWithWSDL() throws Exception {
        invokeWithWSDL("RpcLitService", "org.apache.openejb.server.axis2.testdata.rpclit.RpcLitService",
                       "test_service_rpclit.wsdl", "test_service_rpclit_request.xml");
    }
    
    public void XtestGetWSDL() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        PortData port = new PortData();
        port.setLocation("/axis2/HelloWorld");

        Axis2Request req = new Axis2Request(504,
                "text/xml; charset=utf-8",
                null,
                HttpRequest.GET,
                new HashMap<String,String>(),
                new URI("/axis2/HelloWorld?wsdl"),
                new HashMap<String,String>(),
                "127.0.0.1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Axis2Response res = new Axis2Response("text/xml; charset=utf-8", "127.0.0.1", null, null, 8080, out);
        
        PojoWsContainer container = new PojoWsContainer(port, HelloWorld.class, null, "/axis2");
        container.start();
        container.onMessage(req, res);
        out.flush();

    }

    private void invokeWithWSDL(String serviceName, String endPointClassName, String wsdl, String requestFile ) throws Exception {
//        ClassLoader cl = Thread.currentThread().getContextClassLoader();
//        InputStream in = cl.getResourceAsStream(requestFile);
//
//        //This will reduce number of requests files
//        DocumentBuilder documentBuilder = XmlUtil.newDocumentBuilderFactory().newDocumentBuilder();
//        Document doc = documentBuilder.parse(in);
//
//        Element root = doc.getDocumentElement();
//        NodeList nodeList = root.getElementsByTagName("soap:Envelope");
//
//        InputStream request;
//
//        for(int i = 0; i < nodeList.getLength(); i++){
//        	StringBuffer envelope = new StringBuffer("<soap:Envelope");
//        	Element element = (Element)nodeList.item(i);
//        	NamedNodeMap attributes = element.getAttributes();
//        	if(attributes != null){
//        		for(int k=0; k < attributes.getLength(); k++){
//        			envelope.append(" "+attributes.item(k).getNodeName().trim());
//        			envelope.append("=\""+attributes.item(k).getNodeValue().trim()+"\"");
//        		}
//        		String content = element.getTextContent();
//
//        		if(content != null && !content.equals("")){
//        			envelope.append(">");
//
//            		NodeList children = element.getChildNodes();
//            		if(children != null){
//            			for(int j=0; j < children.getLength(); j++){
//            				if(children.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
//                    			Element child = (Element)children.item(j);
//                    			envelope.append(getElementContent(child).trim());
//                			}else if(children.item(j).getNodeType() == org.w3c.dom.Node.TEXT_NODE){
//                				envelope.append(children.item(j).getNodeValue().trim());
//                			}
//                		}
//            		}
//            		envelope.append("</soap:Envelope>");
//        		}else {
//        			envelope.append("/>");
//        		}
//        	}
//
//            System.out.println(envelope.toString());
//
//            request = new ByteArrayInputStream(envelope.toString().getBytes("UTF-8"));
//
//            PortInfo portInfo = new PortInfo();
//            portInfo.location = "/axis2/" + serviceName;
//
//            File wsdlFile = new File(RESOURCE_PATH + wsdl);
//            portInfo.wsdlFile = wsdlFile.toURL().toString();
//
//            try {
//                Axis2Request req = new Axis2Request(504,
//                        "text/xml; charset=utf-8",
//                        request,
//                        HttpRequest.POST,
//                        new HashMap<String,String>(),
//                        new URI("/axis2/"+serviceName),
//                        new HashMap<String,String>(),
//                        "127.0.0.1");
//
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                Axis2Response res = new Axis2Response("text/xml; charset=utf-8", "127.0.0.1", null, null, 8080, out);
//
//                POJOWebServiceContainer container = new POJOWebServiceContainer(portInfo, endPointClassName, cl, null, null, Holder.EMPTY, "/axis2");
//                container.init();
//                container.onMessage(req, res);
//                System.out.println("Response "+out);
//                out.flush();
//
//            } catch (Throwable ex) {
//                ex.printStackTrace();
//                throw new Exception(ex.toString());
//            } finally {
//                if (request != null) {
//                    try {
//                        request.close();
//                    } catch (IOException ignore) {
//                        // ignore
//                    }
//                }
//            }
//        }
    }
    
    private String getElementContent(Element e){
    	StringBuffer content = new StringBuffer("<"+e.getNodeName());
    	NamedNodeMap attributes = e.getAttributes();
    	
    	if(attributes != null){
    		for(int k=0; k < attributes.getLength(); k++){
    			content.append(" "+attributes.item(k).getNodeName()) ;
    			content.append("=\""+attributes.item(k).getNodeValue()+"\"") ;
    		}
    	}
    	
    	String value = e.getTextContent();
		
		if(value != null && !value.equals("")){
			content.append(">");

			NodeList children = e.getChildNodes();
    		if(children != null){
    			for(int j=0; j < children.getLength(); j++){
    				if(children.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE){
            			Element child = (Element)children.item(j);
            			content.append(getElementContent(child).trim());
        			}else if(children.item(j).getNodeType() == org.w3c.dom.Node.TEXT_NODE){
        				content.append(children.item(j).getNodeValue().trim());
        			}
        		}
    		}
    		content.append("</"+e.getNodeName()+">");
		}else {
			content.append("/>");
		}
		
		return content.toString();
    }
    
    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

}
