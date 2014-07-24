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
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WsdlVisitor {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_WS, WsdlVisitor.class);

    protected final Definition definition;

    public WsdlVisitor(final Definition definition) {
        this.definition = definition;
    }

    public void walkTree() {
        begin();
        try {
            visit(definition);
            for (final Iterator iterator = definition.getImports().entrySet().iterator(); iterator.hasNext(); ) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final String namespaceURI = (String) entry.getKey();
                final List importsForNamespace = (List) entry.getValue();
                for (final Iterator iterator1 = importsForNamespace.iterator(); iterator1.hasNext(); ) {
                    final Import anImport = (Import) iterator1.next();
                    visit(anImport);
                }
            }
            visit(definition.getTypes());
            final Collection messages = definition.getMessages().values();
            for (final Iterator iterator = messages.iterator(); iterator.hasNext(); ) {
                final Message message = (Message) iterator.next();
                visit(message);
                final Collection parts = message.getParts().values();
                for (final Iterator iterator2 = parts.iterator(); iterator2.hasNext(); ) {
                    final Part part = (Part) iterator2.next();
                    visit(part);
                }
            }
            final Collection services = definition.getServices().values();
            for (final Iterator iterator = services.iterator(); iterator.hasNext(); ) {
                final Service service = (Service) iterator.next();
                visit(service);
                final Collection ports = service.getPorts().values();
                for (final Iterator iterator1 = ports.iterator(); iterator1.hasNext(); ) {
                    final Port port = (Port) iterator1.next();
                    visit(port);
                    final Binding binding = port.getBinding();
                    visit(binding);
                    final List bindingOperations = binding.getBindingOperations();
                    for (int i = 0; i < bindingOperations.size(); i++) {
                        final BindingOperation bindingOperation = (BindingOperation) bindingOperations.get(i);
                        visit(bindingOperation);
                        visit(bindingOperation.getBindingInput());
                        visit(bindingOperation.getBindingOutput());
                        final Collection bindingFaults = bindingOperation.getBindingFaults().values();
                        for (final Iterator iterator2 = bindingFaults.iterator(); iterator2.hasNext(); ) {
                            final BindingFault bindingFault = (BindingFault) iterator2.next();
                            visit(bindingFault);
                        }

                    }
                    final PortType portType = binding.getPortType();
                    visit(portType);
                    final List operations = portType.getOperations();
                    for (int i = 0; i < operations.size(); i++) {
                        final Operation operation = (Operation) operations.get(i);
                        visit(operation);
                        {
                            final Input input = operation.getInput();
                            visit(input);
                        }
                        {
                            final Output output = operation.getOutput();
                            visit(output);
                        }
                        final Collection faults = operation.getFaults().values();
                        for (final Iterator iterator2 = faults.iterator(); iterator2.hasNext(); ) {
                            final Fault fault = (Fault) iterator2.next();
                            visit(fault);
                        }

                    }
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            end();
        }
    }

    protected void begin() {
    }

    protected void end() {
    }

    protected void visit(final Fault fault) {
    }

    protected void visit(final Definition definition) {
    }

    protected void visit(final Import wsdlImport) {
    }

    protected void visit(final Types types) {
    }

    protected void visit(final BindingFault bindingFault) {
    }

    protected void visit(final BindingOutput bindingOutput) {
    }

    protected void visit(final BindingInput bindingInput) {
    }

    protected void visit(final Output output) {
    }

    protected void visit(final Part part) {
    }

    protected void visit(final Message message) {
    }

    protected void visit(final Input input) {
    }

    protected void visit(final Operation operation) {
    }

    protected void visit(final PortType portType) {
    }

    protected void visit(final BindingOperation bindingOperation) {
    }

    protected void visit(final Binding binding) {
    }

    protected void visit(final Port port) {
    }

    protected void visit(final Service service) {
    }

    protected SOAPBody getSOAPBody(final List extensibilityElements) {
        SOAPBody body = null;
        for (int j = 0; j < extensibilityElements.size(); j++) {
            final Object element = extensibilityElements.get(j);
            if (element instanceof SOAPBody) {
                body = (SOAPBody) element;
                break;
            }
        }
        return body;
    }

    protected SOAPBinding getSOAPBinding(final Binding binding) {
        SOAPBinding soapBinding = null;
        final List extensibilityElements = binding.getExtensibilityElements();
        for (int i = 0; i < extensibilityElements.size(); i++) {
            final Object element = extensibilityElements.get(i);
            if (element instanceof SOAPBinding) {
                soapBinding = (SOAPBinding) element;
            }
        }
        return soapBinding;
    }
}
