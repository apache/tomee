/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.openejb.server.axis2;

import org.apache.axis2.description.AxisService;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WsdlQueryHandler {
    private static final Logger logger = Logger.getInstance(LogCategory.AXIS2, WsdlQueryHandler.class);

    private Map<String, Definition> mp = new ConcurrentHashMap<String, Definition>();
    private Map<String, SchemaReference> smp = new ConcurrentHashMap<String, SchemaReference>();
    private AxisService service;
    
    public WsdlQueryHandler(AxisService service) {
        this.service = service;
    }
    
    public void writeResponse(String baseUri, String wsdlUri, OutputStream os) throws Exception {
        int idx = baseUri.toLowerCase().indexOf("?wsdl");
        String base = null;
        String wsdl = "";
        String xsd = null;
        if (idx != -1) {
            base = baseUri.substring(0, baseUri.toLowerCase().indexOf("?wsdl"));
            wsdl = baseUri.substring(baseUri.toLowerCase().indexOf("?wsdl") + 5);
            if (wsdl.length() > 0) {
                wsdl = wsdl.substring(1);
            }
        } else {
            base = baseUri.substring(0, baseUri.toLowerCase().indexOf("?xsd="));
            xsd = baseUri.substring(baseUri.toLowerCase().indexOf("?xsd=") + 5);
        }

        if (!mp.containsKey(wsdl)) {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            reader.setFeature("javax.wsdl.verbose", false);
            Definition def = reader.readWSDL(wsdlUri);
            updateDefinition(def, mp, smp, base);
            updateServices(this.service.getName(), this.service.getEndpointName(), def, base);
            mp.put("", def);
        }

        Element rootElement;

        if (xsd == null) {
            Definition def = mp.get(wsdl);

            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLWriter writer = factory.newWSDLWriter();

            rootElement = writer.getDocument(def).getDocumentElement();
        } else {
            SchemaReference si = smp.get(xsd);
            rootElement = si.getReferencedSchema().getElement();
        }

        NodeList nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema",
                "import");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String sl = el.getAttribute("schemaLocation");
            if (smp.containsKey(sl)) {
                el.setAttribute("schemaLocation", base + "?xsd=" + sl);
            }
        }
        nl = rootElement.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "include");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String sl = el.getAttribute("schemaLocation");
            if (smp.containsKey(sl)) {
                el.setAttribute("schemaLocation", base + "?xsd=" + sl);
            }
        }
        nl = rootElement.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "import");
        for (int x = 0; x < nl.getLength(); x++) {
            Element el = (Element) nl.item(x);
            String sl = el.getAttribute("location");
            if (mp.containsKey(sl)) {
                el.setAttribute("location", base + "?wsdl=" + sl);
            }
        }

        writeTo(rootElement, os);
    }
       
    protected void updateDefinition(Definition def,
                                    Map<String, Definition> done,
                                    Map<String, SchemaReference> doneSchemas,
                                    String base) {
        Collection<List> imports = def.getImports().values();
        for (List lst : imports) {
            List<Import> impLst = lst;
            for (Import imp : impLst) {
                String start = imp.getLocationURI();
                try {
                    //check to see if it's aleady in a URL format.  If so, leave it.
                    new URL(start);
                } catch (MalformedURLException e) {
                    done.put(start, imp.getDefinition());
                    updateDefinition(imp.getDefinition(), done, doneSchemas, base);
                }
            }
        }      
        
        
        /* This doesn't actually work.   Setting setSchemaLocationURI on the import
        * for some reason doesn't actually result in the new URI being written
        * */
        Types types = def.getTypes();
        if (types != null) {
            for (ExtensibilityElement el : (List<ExtensibilityElement>)types.getExtensibilityElements()) {
                if (el instanceof Schema) {
                    Schema see = (Schema)el;
                    updateSchemaImports(see, doneSchemas, base);
                }
            }
        }
    }
    
    protected void updateSchemaImports(Schema schema,
                                       Map<String, SchemaReference> doneSchemas,
                                       String base) {
        Collection<List>  imports = schema.getImports().values();
        for (List lst : imports) {
            List<SchemaImport> impLst = lst;
            for (SchemaImport imp : impLst) {
                String start = imp.getSchemaLocationURI();
                if (start != null) {
                    try {
                        //check to see if it's aleady in a URL format.  If so, leave it.
                        new URL(start);
                    } catch (MalformedURLException e) {
                        if (!doneSchemas.containsKey(start)) {
                            doneSchemas.put(start, imp);
                            updateSchemaImports(imp.getReferencedSchema(), doneSchemas, base);
                        }
                    }
                }
            }
        }
        List<SchemaReference> includes = schema.getIncludes();
        for (SchemaReference included : includes) {
            String start = included.getSchemaLocationURI();
            if (start != null) {
                try {
                    //check to see if it's aleady in a URL format.  If so, leave it.
                    new URL(start);
                } catch (MalformedURLException e) {
                    if (!doneSchemas.containsKey(start)) {
                        doneSchemas.put(start, included);
                        updateSchemaImports(included.getReferencedSchema(), doneSchemas, base);
                    }
                }
            }
        }
    }
    
    public static void writeTo(Node node, OutputStream os) {
        writeTo(new DOMSource(node), os);
    }
    
    public static void writeTo(Source src, OutputStream os) {
        Transformer it;
        try {
            it = TransformerFactory.newInstance().newTransformer();
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "false");
            it.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            it.transform(src, new StreamResult(os));
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void updateServices(String serviceName, String portName, Definition def, String baseUri)
            throws Exception {
        boolean updated = false;
        Map services = def.getServices();
        if (services != null) {
            ArrayList<QName> servicesToRemove = new ArrayList<QName>();
            
            Iterator serviceIterator = services.entrySet().iterator();
            while (serviceIterator.hasNext()) {
                Map.Entry serviceEntry = (Map.Entry) serviceIterator.next();
                QName currServiceName = (QName) serviceEntry.getKey();
                if (currServiceName.getLocalPart().equals(serviceName)) {
                    Service service = (Service) serviceEntry.getValue();
                    updatePorts(portName, service, baseUri);
                    updated = true;
                } else {
                    servicesToRemove.add(currServiceName);
                }
            }
            
            for (QName serviceToRemove : servicesToRemove) {
                def.removeService(serviceToRemove);                
            }
        }
        if (!updated) {
            logger.warning("WSDL '" + serviceName + "' service not found.");
        }
    }

    private void updatePorts(String portName, Service service, String baseUri) throws Exception {
        boolean updated = false;
        Map ports = service.getPorts();
        if (ports != null) {
            ArrayList<String> portsToRemove = new ArrayList<String>();
            
            Iterator portIterator = ports.entrySet().iterator();
            while (portIterator.hasNext()) {
                Map.Entry portEntry = (Map.Entry) portIterator.next();
                String currPortName = (String) portEntry.getKey();
                if (currPortName.equals(portName)) {
                    Port port = (Port) portEntry.getValue();
                    updatePortLocation(port, baseUri);
                    updated = true;
                } else {
                    portsToRemove.add(currPortName);
                }
            }
            
            for (String portToRemove : portsToRemove) {
                service.removePort(portToRemove);               
            }
        }
        if (!updated) {
            logger.warning("WSDL '" + portName + "' port not found.");
        }
    }

    private void updatePortLocation(Port port, String baseUri) throws URISyntaxException {
        List<?> exts = port.getExtensibilityElements();
        if (exts != null && exts.size() > 0) {
            ExtensibilityElement el = (ExtensibilityElement) exts.get(0);
            if (el instanceof SOAP12Address) {
                SOAP12Address add = (SOAP12Address) el;
                add.setLocationURI(baseUri);
            } else if (el instanceof SOAPAddress) {
                SOAPAddress add = (SOAPAddress) el;
                add.setLocationURI(baseUri);
            }
        }
    }
}
