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
package org.apache.webbeans.atinject.tck.container;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Test;

import org.apache.webbeans.atinject.tck.specific.SpecificProducer;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.tck.StandaloneContainersImpl;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.jboss.testharness.api.DeploymentException;

public class AtInjectContainer extends StandaloneContainersImpl
{ 
    private static Set<Class<?>> deploymentClasses = null;
    
    static
    {
        deploymentClasses = new HashSet<Class<?>>();
        deploymentClasses.add(Convertible.class);
        deploymentClasses.add(Seat.class);
        deploymentClasses.add(Tire.class);
        deploymentClasses.add(V8Engine.class);
        deploymentClasses.add(Cupholder.class);
        deploymentClasses.add(FuelTank.class);
        
        //Adding our special producer
        deploymentClasses.add(SpecificProducer.class);
        
    }
    
    public AtInjectContainer()
    {
        
    }
    
    public Test start()
    {
        try
        {
            deploy(deploymentClasses);
            
            BeanManager manager = WebBeansContext.getInstance().getBeanManagerImpl();
            Set<Bean<?>> beans = manager.getBeans(Car.class, new Annotation[0]);
            Bean<?> carBean = beans.iterator().next();
            
            Car car = (Car)manager.getReference(carBean , Car.class , manager.createCreationalContext(carBean));
            
            return Tck.testsFor(car, false, true);
            
        } catch(DeploymentException e)
        {
            logger.log(Level.SEVERE, "AtInjectContainer", e);
            excpetion = e;
        }
        
        return null;
    }
    
    
    public void stop()
    {
        undeploy();            

    }
    
}
