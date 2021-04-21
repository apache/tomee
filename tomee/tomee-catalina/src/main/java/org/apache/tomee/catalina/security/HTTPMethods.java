/*
 * Copyright 2018 OmniFaces.
 * Copyright 2003-2011 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina.security;

/**
 *
 * @author Guillermo González de Agüero
 */
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks sets of HTTP actions for use while computing permissions during web
 * deployment.
 *
 * @version $Rev$ $Date$
 */
public class HTTPMethods {

    private final Set<String> methods = new HashSet<String>();

    private boolean isExcluded = false;

    public HTTPMethods(Set<String> httpMethods, boolean isExcluded) {
        this.isExcluded = isExcluded;
        methods.addAll(httpMethods);
    }

    public HTTPMethods(HTTPMethods httpMethods, boolean complemented) {
        isExcluded = httpMethods.isExcluded ^ complemented;
        methods.addAll(httpMethods.methods);
    }

    /**
     * Generally speaking, add method is to perform a union action between the
     * caller and the parameters
     *
     * @param httpMethods
     * @param addedMethodsExcluded
     */
    public void add(Set<String> httpMethods, boolean addedMethodsExcluded) {
        //JACC 3.1.3.2 Combining HTTP Methods
        //An empty list combines with any other list to yield the empty list.
        if (isExcluded && httpMethods.isEmpty()) {
            return;
        }
        if (httpMethods.size() == 0) {
            isExcluded = addedMethodsExcluded;
            methods.clear();
            return;
        }
        //JACC 3.1.3.2 Combing HTTP Methods
        //Lists of http-method elements combine to yield a list of http-method elements containing the union (without duplicates) of the http-method elements that occur in the individual lists.
        //Lists of http-method-omission elements combine to yield a list containing only the http-method-omission elements that occur in all of the individual lists (i.e., the intersection).
        //A list of http-method-omission elements combines with a list of http-method elements to yield the list of http-method-omission elements minus any elements whose method name occurs in the http-method list
        if (isExcluded) {
            if (addedMethodsExcluded) {
                //ExceptionList + ExceptionList
                methods.retainAll(httpMethods);
            } else {
                //ExceptionList + List
                methods.removeAll(httpMethods);
            }
        } else {
            if (addedMethodsExcluded) {
                //List + ExceptionList
                Set<String> tempHttpMethods = new HashSet<String>(httpMethods);
                tempHttpMethods.removeAll(methods);
                methods.clear();
                methods.addAll(tempHttpMethods);
                isExcluded = true;
            } else {
                //List + List
                methods.addAll(httpMethods);
            }
        }
    }

    public HTTPMethods add(HTTPMethods httpMethods) {
        add(httpMethods.methods, httpMethods.isExcluded);
        return this;
    }

    /**
     * Remove methods is only used while we wish to remove those configurations
     * in role/unchecked constraints, which are also configured in excluded
     * constraints
     *
     * @param httpMethods
     * @return
     */
    public HTTPMethods remove(HTTPMethods httpMethods) {
        if (isExcluded) {
            if (httpMethods.isExcluded) {
                //TODO questionable
                isExcluded = false;
                Set<String> toRemove = new HashSet<String>(methods);
                methods.clear();
                methods.addAll(httpMethods.methods);
                methods.removeAll(toRemove);
            } else {
                methods.addAll(httpMethods.methods);
            }
        } else {
            if (httpMethods.isExcluded) {
                methods.retainAll(httpMethods.methods);
            } else {
                methods.removeAll(httpMethods.methods);
            }
        }
        if (!isExcluded && methods.isEmpty()) {
            return null;
        }
        return this;
    }

    public String getHttpMethods() {
        return getHttpMethodsBuffer(isExcluded).toString();
    }

    public StringBuilder getHttpMethodsBuffer() {
        return getHttpMethodsBuffer(isExcluded);
    }

    public String getComplementedHttpMethods() {
        return getHttpMethodsBuffer(!isExcluded).toString();
    }

    private StringBuilder getHttpMethodsBuffer(boolean excluded) {
        StringBuilder buffer = new StringBuilder();
        if (excluded) {
            buffer.append("!");
        }
        boolean afterFirst = false;
        for (String method : methods) {
            if (afterFirst) {
                buffer.append(",");
            } else {
                afterFirst = true;
            }
            buffer.append(method);
        }
        return buffer;
    }

    public boolean isNone() {
        return !isExcluded && methods.isEmpty();
    }
}
