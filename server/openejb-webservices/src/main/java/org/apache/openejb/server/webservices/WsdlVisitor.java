/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.webservices;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WsdlVisitor {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_WS, WsdlVisitor.class);

    protected final Definition definition;

    public WsdlVisitor(Definition definition) {
        this.definition = definition;
    }

    public void walkTree() {
        begin();
        try {
            visit(definition);
            for (Iterator<Map.Entry<String, List<Import>>> iterator = definition.getImports().entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, List<Import>> entry = iterator.next();
                String namespaceURI = entry.getKey();
                List<Import> importsForNamespace = entry.getValue();
                for (Iterator<Import> iterator1 = importsForNamespace.iterator(); iterator1.hasNext(); ) {
                    Import anImport = iterator1.next();
                    visit(anImport);
                }
            }
            visit(definition.getTypes());
            Collection<Message> messages = definition.getMessages().values();
            for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext(); ) {
                Message message = iterator.next();
                visit(message);
                Collection<Part> parts = message.getParts().values();
                for (Iterator<Part> iterator2 = parts.iterator(); iterator2.hasNext(); ) {
                    Part part = iterator2.next();
                    visit(part);
                }
            }
            Collection<Service> services = definition.getServices().values();
            for (Iterator<Service> iterator = services.iterator(); iterator.hasNext(); ) {
                Service service = iterator.next();
                visit(service);
                Collection<Port> ports = service.getPorts().values();
                for (Iterator<Port> iterator1 = ports.iterator(); iterator1.hasNext(); ) {
                    Port port = iterator1.next();
                    visit(port);
                    Binding binding = port.getBinding();
                    visit(binding);
                    List<BindingOperation> bindingOperations = binding.getBindingOperations();
                    for (int i = 0; i < bindingOperations.size(); i++) {
                        BindingOperation bindingOperation = bindingOperations.get(i);
                        visit(bindingOperation);
                        visit(bindingOperation.getBindingInput());
                        visit(bindingOperation.getBindingOutput());
                        Collection<BindingFault> bindingFaults = bindingOperation.getBindingFaults().values();
                        for (Iterator<BindingFault> iterator2 = bindingFaults.iterator(); iterator2.hasNext(); ) {
                            BindingFault bindingFault = iterator2.next();
                            visit(bindingFault);
                        }

                    }
                    PortType portType = binding.getPortType();
                    visit(portType);
                    List<Operation> operations = portType.getOperations();
                    for (int i = 0; i < operations.size(); i++) {
                        Operation operation = operations.get(i);
                        visit(operation);
                        {
                            Input input = operation.getInput();
                            visit(input);
                        }
                        {
                            Output output = operation.getOutput();
                            visit(output);
                        }
                        Collection<Fault> faults = operation.getFaults().values();
                        for (Iterator<Fault> iterator2 = faults.iterator(); iterator2.hasNext(); ) {
                            Fault fault = iterator2.next();
                            visit(fault);
                        }

                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            end();
        }
    }

    protected void begin() {
    }

    protected void end() {
    }

    protected void visit(Fault fault) {
    }

    protected void visit(Definition definition) {
    }

    protected void visit(Import wsdlImport) {
    }

    protected void visit(Types types) {
    }

    protected void visit(BindingFault bindingFault) {
    }

    protected void visit(BindingOutput bindingOutput) {
    }

    protected void visit(BindingInput bindingInput) {
    }

    protected void visit(Output output) {
    }

    protected void visit(Part part) {
    }

    protected void visit(Message message) {
    }

    protected void visit(Input input) {
    }

    protected void visit(Operation operation) {
    }

    protected void visit(PortType portType) {
    }

    protected void visit(BindingOperation bindingOperation) {
    }

    protected void visit(Binding binding) {
    }

    protected void visit(Port port) {
    }

    protected void visit(Service service) {
    }

    protected SOAPBody getSOAPBody(List extensibilityElements) {
        SOAPBody body = null;
        for (int j = 0; j < extensibilityElements.size(); j++) {
            Object element = extensibilityElements.get(j);
            if (element instanceof SOAPBody) {
                body = (SOAPBody) element;
                break;
            }
        }
        return body;
    }

    protected SOAPBinding getSOAPBinding(Binding binding) {
        SOAPBinding soapBinding = null;
        List<ExtensibilityElement> extensibilityElements = binding.getExtensibilityElements();
        for (int i = 0; i < extensibilityElements.size(); i++) {
            Object element = extensibilityElements.get(i);
            if (element instanceof SOAPBinding) {
                soapBinding = (SOAPBinding) element;
            }
        }
        return soapBinding;
    }
}
