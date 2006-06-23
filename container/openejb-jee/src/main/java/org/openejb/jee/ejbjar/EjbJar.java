/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import org.openejb.jee.common.Icon;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class EjbJar {

    private List<String> description = new ArrayList<String>();
    private List<String> displayName = new ArrayList<String>();
    private List<Icon> icons = new ArrayList<Icon>();
    private List<EnterpriseBean> enterpriseBeans = new ArrayList<EnterpriseBean>();
    private List<Interceptor> interceptors = new ArrayList<Interceptor>();
    private List<EjbRelation> relationships = new ArrayList<EjbRelation>();
    private AssemblyDescriptor assemblyDescriptor;
    private String ejbClientJar;
    private String version;
    private Boolean metadataComplete;
    private String id;

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public List<String> getDisplayName() {
        return displayName;
    }

    public void setDisplayName(List<String> displayName) {
        this.displayName = displayName;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    public List<EnterpriseBean> getEnterpriseBeans() {
        return enterpriseBeans;
    }

    public void setEnterpriseBeans(List<EnterpriseBean> enterpriseBeans) {
        this.enterpriseBeans = enterpriseBeans;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public List<EjbRelation> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<EjbRelation> relationships) {
        this.relationships = relationships;
    }

    public AssemblyDescriptor getAssemblyDescriptor() {
        return assemblyDescriptor;
    }

    public void setAssemblyDescriptor(AssemblyDescriptor assemblyDescriptor) {
        this.assemblyDescriptor = assemblyDescriptor;
    }

    public String getEjbClientJar() {
        return ejbClientJar;
    }

    public void setEjbClientJar(String ejbClientJar) {
        this.ejbClientJar = ejbClientJar;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getMetadataComplete() {
        return metadataComplete;
    }

    public void setMetadataComplete(Boolean metadataComplete) {
        this.metadataComplete = metadataComplete;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        final EjbJar ejbJar = (EjbJar) o;
//
//        if (assemblyDescriptor != null ? !assemblyDescriptor.equals(ejbJar.assemblyDescriptor) : ejbJar.assemblyDescriptor != null)
//            return false;
//        if (description != null ? !description.equals(ejbJar.description) : ejbJar.description != null) return false;
//        if (displayName != null ? !displayName.equals(ejbJar.displayName) : ejbJar.displayName != null) return false;
//        if (ejbClientJar != null ? !ejbClientJar.equals(ejbJar.ejbClientJar) : ejbJar.ejbClientJar != null) return false;
//        if (enterpriseBeans != null ? !enterpriseBeans.equals(ejbJar.enterpriseBeans) : ejbJar.enterpriseBeans != null)
//            return false;
//        if (icons != null ? !icons.equals(ejbJar.icons) : ejbJar.icons != null) return false;
//        if (id != null ? !id.equals(ejbJar.id) : ejbJar.id != null) return false;
//        if (interceptors != null ? !interceptors.equals(ejbJar.interceptors) : ejbJar.interceptors != null) return false;
//        if (metadataComplete != null ? !metadataComplete.equals(ejbJar.metadataComplete) : ejbJar.metadataComplete != null)
//            return false;
//        if (relationships != null ? !relationships.equals(ejbJar.relationships) : ejbJar.relationships != null)
//            return false;
//        if (version != null ? !version.equals(ejbJar.version) : ejbJar.version != null) return false;
//
//        return true;
//    }
//
//    public int hashCode() {
//        int result;
//        result = (description != null ? description.hashCode() : 0);
//        result = 29 * result + (displayName != null ? displayName.hashCode() : 0);
//        result = 29 * result + (icons != null ? icons.hashCode() : 0);
//        result = 29 * result + (enterpriseBeans != null ? enterpriseBeans.hashCode() : 0);
//        result = 29 * result + (interceptors != null ? interceptors.hashCode() : 0);
//        result = 29 * result + (relationships != null ? relationships.hashCode() : 0);
//        result = 29 * result + (assemblyDescriptor != null ? assemblyDescriptor.hashCode() : 0);
//        result = 29 * result + (ejbClientJar != null ? ejbClientJar.hashCode() : 0);
//        result = 29 * result + (version != null ? version.hashCode() : 0);
//        result = 29 * result + (metadataComplete != null ? metadataComplete.hashCode() : 0);
//        result = 29 * result + (id != null ? id.hashCode() : 0);
//        return result;
//    }
}
