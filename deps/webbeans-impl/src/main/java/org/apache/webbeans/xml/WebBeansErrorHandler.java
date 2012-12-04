/*
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
package org.apache.webbeans.xml;

import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link ErrorHandler} .
 * <p>
 * Error handler that is used for handling errors while parsing the document.
 * </p>
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 */
public class WebBeansErrorHandler implements ErrorHandler
{
    private static Logger logger = WebBeansLoggerFacade.getLogger(WebBeansErrorHandler.class);

    public void error(SAXParseException exception) throws SAXException
    {
        logger.log(Level.SEVERE, exception.getMessage(), exception.getCause());
        throw new WebBeansException(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException
    {
        logger.log(Level.SEVERE, exception.getMessage(), exception.getCause());
        throw new WebBeansException(exception);
    }

    public void warning(SAXParseException exception) throws SAXException
    {
        logger.log(Level.WARNING, exception.getMessage(), exception.getCause());
    }

}
