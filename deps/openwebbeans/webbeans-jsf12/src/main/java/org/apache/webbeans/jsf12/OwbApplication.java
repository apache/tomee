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
package org.apache.webbeans.jsf12;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;

import org.apache.webbeans.config.WebBeansContext;

@SuppressWarnings("unchecked")
public class OwbApplication extends Application
{
    private Application wrappedApplication;
    
    private volatile ExpressionFactory expressionFactory;
    
    public OwbApplication(Application wrappedApplication)
    {
        this.wrappedApplication = wrappedApplication;
    }

    @Override
    public void addComponent(String arg0, String arg1)
    {
        this.wrappedApplication.addComponent(arg0, arg1);
    }
    

    @Override
    public void addConverter(String arg0, String arg1)
    {
        this.wrappedApplication.addConverter(arg0, arg1);
    }

    @Override
    public void addValidator(String arg0, String arg1)
    {
        this.wrappedApplication.addValidator(arg0, arg1);        
    }

    @Override
    public UIComponent createComponent(String arg0) throws FacesException
    {        
        return this.wrappedApplication.createComponent(arg0);
    }

    @Override
    public UIComponent createComponent(ValueBinding arg0, FacesContext arg1, String arg2) throws FacesException
    {        
        return this.wrappedApplication.createComponent(arg0, arg1, arg2);
    }

    @Override
    public Converter createConverter(String arg0)
    {        
        return this.wrappedApplication.createConverter(arg0);
    }


    @Override
    public Validator createValidator(String arg0) throws FacesException
    {
        
        return this.wrappedApplication.createValidator(arg0);
    }

    @Override
    public ValueBinding createValueBinding(String arg0) throws ReferenceSyntaxException
    {        
        return this.wrappedApplication.createValueBinding(arg0);
    }

    @Override
    public ActionListener getActionListener()
    {        
        return this.wrappedApplication.getActionListener();
    }

    @Override
    public Iterator<String> getComponentTypes()
    {        
        return this.wrappedApplication.getComponentTypes();
    }

    @Override
    public Iterator<String> getConverterIds()
    {        
        return this.wrappedApplication.getConverterIds();
    }


    @Override
    public Locale getDefaultLocale()
    {        
        return this.wrappedApplication.getDefaultLocale();
    }

    @Override
    public String getDefaultRenderKitId()
    {        
        return this.wrappedApplication.getDefaultRenderKitId();
    }

    @Override
    public String getMessageBundle()
    {        
        return this.wrappedApplication.getMessageBundle();
    }

    @Override
    public NavigationHandler getNavigationHandler()
    {        
        return this.wrappedApplication.getNavigationHandler();
    }

    @Override
    public PropertyResolver getPropertyResolver()
    {        
        return this.wrappedApplication.getPropertyResolver();
    }

    @Override
    public StateManager getStateManager()
    {        
        return this.wrappedApplication.getStateManager();
    }

    @Override
    public Iterator<Locale> getSupportedLocales()
    {        
        return this.wrappedApplication.getSupportedLocales();
    }

    @Override
    public Iterator<String> getValidatorIds()
    {        
        return this.wrappedApplication.getValidatorIds();
    }

    @Override
    public VariableResolver getVariableResolver()
    {        
        return this.wrappedApplication.getVariableResolver();
    }

    @Override
    public ViewHandler getViewHandler()
    {        
        return this.wrappedApplication.getViewHandler();
    }

    @Override
    public void setActionListener(ActionListener arg0)
    {        
        this.wrappedApplication.setActionListener(arg0);
    }

    @Override
    public void setDefaultLocale(Locale arg0)
    {
        this.wrappedApplication.setDefaultLocale(arg0);        
    }

    @Override
    public void setDefaultRenderKitId(String arg0)
    {
        this.wrappedApplication.setDefaultRenderKitId(arg0);        
    }

    @Override
    public void setMessageBundle(String arg0)
    {
        this.wrappedApplication.setMessageBundle(arg0);        
    }

    @Override
    public void setNavigationHandler(NavigationHandler arg0)
    {
        this.wrappedApplication.setNavigationHandler(arg0);        
    }

    @Override
    public void setPropertyResolver(PropertyResolver arg0)
    {        
        this.wrappedApplication.setPropertyResolver(arg0);
    }

    @Override
    public void setStateManager(StateManager arg0)
    {        
        this.wrappedApplication.setStateManager(arg0);
    }

    @Override
    public void setSupportedLocales(Collection<Locale> arg0)
    {
        this.wrappedApplication.setSupportedLocales(arg0);
        
    }

    @Override
    public void setVariableResolver(VariableResolver arg0)
    {        
        this.wrappedApplication.setVariableResolver(arg0);
    }

    @Override
    public void setViewHandler(ViewHandler arg0)
    {        
        this.wrappedApplication.setViewHandler(arg0);
    }

    /* (non-Javadoc)
     * @see javax.faces.application.Application#getExpressionFactory()
     */
    @Override
    public ExpressionFactory getExpressionFactory()
    {
        if(this.expressionFactory == null)
        {
            expressionFactory = wrappedApplication.getExpressionFactory();
            expressionFactory = WebBeansContext.getInstance().getBeanManagerImpl().wrapExpressionFactory(expressionFactory);
        }
        
        return expressionFactory;
    }

    /* (non-Javadoc)
     * @see javax.faces.application.Application#addELContextListener(javax.el.ELContextListener)
     */
    @Override
    public void addELContextListener(ELContextListener listener)
    {
       
        wrappedApplication.addELContextListener(listener);
    }

    /* (non-Javadoc)
     * @see javax.faces.application.Application#addELResolver(javax.el.ELResolver)
     */
    @Override
    public void addELResolver(ELResolver resolver)
    {
       
        wrappedApplication.addELResolver(resolver);
    }


    /* (non-Javadoc)
     * @see javax.faces.application.Application#createComponent(javax.el.ValueExpression, javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType) throws FacesException
    {
       
        return wrappedApplication.createComponent(componentExpression, context, componentType);
    }


    /* (non-Javadoc)
     * @see javax.faces.application.Application#getELContextListeners()
     */
    @Override
    public ELContextListener[] getELContextListeners()
    {
       
        return wrappedApplication.getELContextListeners();
    }

    /* (non-Javadoc)
     * @see javax.faces.application.Application#getELResolver()
     */
    @Override
    public ELResolver getELResolver()
    {
       
        return wrappedApplication.getELResolver();
    }

 
    /* (non-Javadoc)
     * @see javax.faces.application.Application#getResourceBundle(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    public ResourceBundle getResourceBundle(FacesContext ctx, String name) throws FacesException, NullPointerException
    {
       
        return wrappedApplication.getResourceBundle(ctx, name);
    }

    /* (non-Javadoc)
     * @see javax.faces.application.Application#removeELContextListener(javax.el.ELContextListener)
     */
    @Override
    public void removeELContextListener(ELContextListener listener)
    {
       
        wrappedApplication.removeELContextListener(listener);
    }

    @Override
    public void addConverter(Class arg0, String arg1)
    {
        wrappedApplication.addConverter(arg0, arg1);
    }

    @Override
    public Converter createConverter(Class arg0)
    {
        return wrappedApplication.createConverter(arg0);
    }

    @Override
    public MethodBinding createMethodBinding(String arg0, Class[] arg1) throws ReferenceSyntaxException
    {
        return wrappedApplication.createMethodBinding(arg0, arg1);
    }

    @Override
    public Iterator<Class> getConverterTypes()
    {
        return wrappedApplication.getConverterTypes();
    }

}
