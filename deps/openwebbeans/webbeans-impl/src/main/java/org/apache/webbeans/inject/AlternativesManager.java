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
package org.apache.webbeans.inject;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.Bean;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.spi.ScannerService;
import org.apache.webbeans.util.AnnotationUtil;

public class AlternativesManager
{
    private final Set<Class<?>> alternatives = new HashSet<Class<?>>();
    
    private final Set<Class<? extends Annotation>> stereoAlternatives = new HashSet<Class<? extends Annotation>>();

    private final WebBeansContext webBeansContext;

    public AlternativesManager(WebBeansContext webBeansContext)
    {

        this.webBeansContext = webBeansContext;
    }

    @SuppressWarnings("unchecked")
    public void addStereoTypeAlternative(Class<?> alternative,String fileName,ScannerService scanner)
    {                
        if(Annotation.class.isAssignableFrom(alternative))
        {
            Class<? extends Annotation> stereo = (Class<? extends Annotation>)alternative;
            boolean ok = false;
            if(webBeansContext.getAnnotationManager().isStereoTypeAnnotation(stereo))
            {
                if(AnnotationUtil.hasClassAnnotation(stereo, Alternative.class))
                {
                    boolean isBDAScanningEnabled=(scanner!=null && scanner.isBDABeansXmlScanningEnabled());
                    if(isBDAScanningEnabled && !scanner.getBDABeansXmlScanner().addStereoType(stereo, fileName) ||
                            (!isBDAScanningEnabled && stereoAlternatives.contains(stereo)) )
                    {
                        throw new WebBeansConfigurationException("Given alternative class : " + alternative.getName() + " is already added as @Alternative" );
                    }
                    
                    ok = true;

                    stereoAlternatives.add(stereo);
                }
            }
            
            if(!ok)
            {
                throw new WebBeansConfigurationException("Given stereotype class : " + alternative.getName() + " is not annotated with @Alternative" );
            }
        }
        else
        {
            throw new WebBeansConfigurationException("Given stereotype class : " + alternative.getName() + " is not an annotation" );
        }        
    }
    
    public void addClazzAlternative(Class<?> alternative,String fileName,ScannerService scanner)
    {
        if(AnnotationUtil.hasClassAnnotation(alternative, Alternative.class))
        {
            boolean isBDAScanningEnabled=(scanner!=null && scanner.isBDABeansXmlScanningEnabled());
            if((isBDAScanningEnabled && !scanner.getBDABeansXmlScanner().addAlternative(alternative, fileName)) ||
                    (!isBDAScanningEnabled && alternatives.contains(alternative)))
            {
                throw new WebBeansConfigurationException("Given class : " + alternative.getName() + " is already added as @Alternative" );
            }

            alternatives.add(alternative);
        }
        else
        {
            throw new WebBeansConfigurationException("Given class : " + alternative.getName() + " is not annotated with @Alternative" );
        }
    }
    
    public boolean isClassAlternative(Class<?> clazz)
    {
        if(alternatives.contains(clazz))
        {
            return true;
        }
        
        return false;
    }

    public boolean isStereoAlternative(Class<? extends Annotation> stereo)
    {
        return stereoAlternatives.contains(stereo);
    }

    public boolean isBeanHasAlternative(Bean<?> bean)
    {
        Class<?> returnType = bean.getBeanClass();
        
        if(alternatives.contains(returnType))
        {
            return true;
        }
        
        Set<Class<? extends Annotation>> set = bean.getStereotypes();
        for(Class<? extends Annotation> ann : set)
        {
            if(stereoAlternatives.contains(ann))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public void clear()
    {
        alternatives.clear();
        stereoAlternatives.clear();
    }
}
