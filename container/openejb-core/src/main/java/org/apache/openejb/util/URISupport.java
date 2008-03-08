/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swiped verbatim from ActiveMQ... the URI kings.
 *
 * URI relativize(URI, URI) added afterwards to deal with the
 * non-functional URI.relativize(URI) method
 */
public class URISupport {

    /**
     * URI absoluteA = new URI("/Users/dblevins/work/openejb3/container/openejb-jee/apple/");
     * URI absoluteB = new URI("/Users/dblevins/work/openejb3/container/openejb-core/foo.jar");
     *
     * URI relativeB = URISupport.relativize(absoluteA, absoluteB);
     *
     * assertEquals("../../openejb-core/foo.jar", relativeB.toString());
     *
     * URI resolvedB = absoluteA.resolve(relativeB);
     * assertTrue(resolvedB.equals(absoluteB));
     *
     * @param a
     * @param b
     * @return relative b
     */
    public static URI relativize(URI a, URI b) {
        if (a == null || b == null) return b;

        if (!a.isAbsolute() && b.isAbsolute()) return b;

        if (!b.isAbsolute()) b = a.resolve(b);

        List<String> pathA = Arrays.asList(a.getPath().split("/"));
        List<String> pathB = Arrays.asList(b.getPath().split("/"));

        int limit = Math.min(pathA.size(), pathB.size());


        int lastMatch = 0;
        while (lastMatch < limit) {
            String aa = pathA.get(lastMatch);
            String bb = pathB.get(lastMatch);
            if (aa.equals(bb)) lastMatch++;
            else break;
        }

        List<String> path = new ArrayList<String>();
        for (int x = pathA.size() - lastMatch; x > 0; x--) {
            path.add("..");
        }

        List<String> remaining = pathB.subList(lastMatch, pathB.size());
        path.addAll(remaining);

        try {
            return new URI(null, null, Join.join("/", path), b.getQuery(), b.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static class CompositeData {
        String scheme;
        String path;
        URI components[];
        Map parameters;
        String fragment;
        public String host;

        public URI[] getComponents() {
            return components;
        }
        public String getFragment() {
            return fragment;
        }
        public Map getParameters() {
            return parameters;
        }
        public String getScheme() {
            return scheme;
        }
        public String getPath() {
            return path;
        }
        public String getHost() {
            return host;
        }

        public URI toURI() throws URISyntaxException {
            StringBuffer sb = new StringBuffer();
            if( scheme!=null ) {
                sb.append(scheme);
                sb.append(':');
            }

            if( host!=null && host.length()!=0 ) {
                sb.append(host);
            } else {
                sb.append('(');
                for (int i = 0; i < components.length; i++) {
                    if( i!=0 )
                        sb.append(',');
                    sb.append(components[i].toString());
                }
                sb.append(')');
            }

            if( path !=null ) {
                sb.append('/');
                sb.append(path);
            }
            if(!parameters.isEmpty()) {
                sb.append("?");
                sb.append(createQueryString(parameters));
            }
            if( fragment!=null ) {
                sb.append("#");
                sb.append(fragment);
            }
            return new URI(sb.toString());
        }
    }

    public static Map<String, String> parseQuery(String uri) throws URISyntaxException{
        try{
            Map<String, String> rc = new LinkedHashMap<String,String>();
            if(uri!=null){
                String[] parameters=uri.split("&");
                for(int i=0;i<parameters.length;i++){
                    int p=parameters[i].indexOf("=");
                    if(p>=0){
                        String name= URLDecoder.decode(parameters[i].substring(0,p),"UTF-8");
                        String value=URLDecoder.decode(parameters[i].substring(p+1),"UTF-8");
                        rc.put(name,value);
                    }else{
                        rc.put(parameters[i],null);
                    }
                }
            }
            return rc;
        }catch(UnsupportedEncodingException e){
            throw (URISyntaxException) new URISyntaxException(e.toString(),"Invalid encoding").initCause(e);
        }
    }

    public static Map<String, String> parseParamters(URI uri) throws URISyntaxException {
        return uri.getQuery()==null ? Collections.EMPTY_MAP : parseQuery(stripPrefix(uri.getQuery(), "?"));
    }

    /**
     * Removes any URI query from the given uri
     */
    public static URI removeQuery(URI uri) throws URISyntaxException {
        return createURIWithQuery(uri, null);
    }

    /**
     * Creates a URI with the given query
     */
    public static URI createURIWithQuery(URI uri, String query) throws URISyntaxException {
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
    }

    public static CompositeData parseComposite(URI uri) throws URISyntaxException {

        CompositeData rc = new CompositeData();
        rc.scheme = uri.getScheme();
        String ssp = stripPrefix(uri.getSchemeSpecificPart().trim(), "//").trim();

        parseComposite(uri, rc, ssp);

        rc.fragment = uri.getFragment();
        return rc;
    }

    private static void parseComposite(URI uri, CompositeData rc, String ssp) throws URISyntaxException {
        String componentString;
        String params;

        if(!checkParenthesis(ssp)){
            throw new URISyntaxException(uri.toString(), "Not a matching number of '(' and ')' parenthesis");
        }

        int p;
        int intialParen = ssp.indexOf("(");
        if( intialParen==0 ) {
            rc.host = ssp.substring(0, intialParen);
            p = rc.host.indexOf("/");
            if( p >= 0 ) {
                rc.path = rc.host.substring(p);
                rc.host = rc.host.substring(0,p);
            }
            p = ssp.lastIndexOf(")");
            componentString = ssp.substring(intialParen+1,p);
            params = ssp.substring(p+1).trim();

        } else {
            componentString = ssp;
            params="";
        }

        String components[] = splitComponents(componentString);
        rc.components=new URI[components.length];
        for (int i = 0; i < components.length; i++) {
            rc.components[i] = new URI(components[i].trim());
        }

        p = params.indexOf("?");
        if( p >= 0 ) {
            if( p > 0) {
                rc.path = stripPrefix(params.substring(0, p), "/");
            }
            rc.parameters = parseQuery(params.substring(p+1));
        } else {
            if( params.length() > 0 )
                rc.path = stripPrefix(params, "/");
            rc.parameters = new LinkedHashMap();
        }
    }

    private static String[] splitComponents(String str) {
        ArrayList<String> l = new ArrayList<String>();

        int last=0;
        int depth = 0;
        char chars[] = str.toCharArray();
        for( int i=0; i < chars.length; i ++ ) {
            switch( chars[i] ) {
            case '(':
                depth++;
                break;
            case ')':
                depth--;
                break;
            case ',':
                if( depth == 0 ) {
                    String s = str.substring(last, i);
                    l.add(s);
                    last=i+1;
                }
            }
        }

        String s = str.substring(last);
        if( s.length() !=0 )
            l.add(s);

        String rc[] = new String[l.size()];
        l.toArray(rc);
        return rc;
    }

    public static String stripPrefix(String value, String prefix) {
        if( value.startsWith(prefix) )
            return value.substring(prefix.length());
        return value;
    }

    public static URI stripScheme(URI uri) throws URISyntaxException {
        return new URI(stripPrefix(uri.getRawSchemeSpecificPart().trim(), "//"));
    }

    public static String createQueryString(Map options) throws URISyntaxException {
        try {
            if(options.size()>0) {
                StringBuffer rc = new StringBuffer();
                boolean first=true;
                for (Iterator iter = options.keySet().iterator(); iter.hasNext();) {
                    if( first )
                        first=false;
                    else
                        rc.append("&");

                    String key = (String) iter.next();
                    String value = (String)options.get(key);
                    rc.append(URLEncoder.encode(key, "UTF-8"));
                    rc.append("=");
                    rc.append(URLEncoder.encode(value, "UTF-8"));
                }
                return rc.toString();
            } else {
                return "";
            }
        } catch (UnsupportedEncodingException e) {
            throw (URISyntaxException)new URISyntaxException(e.toString(), "Invalid encoding").initCause(e);
        }
    }

    /**
     * Creates a URI from the original URI and the remaining paramaters
     * @throws URISyntaxException
     */
    public static URI createRemainingURI(URI originalURI, Map params) throws URISyntaxException {
        String s = createQueryString(params);
        if( s.length()==0 )
            s = null;
        return createURIWithQuery(originalURI, s);
    }

    static public URI changeScheme(URI bindAddr, String scheme) throws URISyntaxException {
        return new URI(scheme, bindAddr.getUserInfo(), bindAddr.getHost(), bindAddr.getPort(), bindAddr.getPath(), bindAddr.getQuery(), bindAddr.getFragment());
    }

    public static boolean checkParenthesis(String str){
        boolean result=true;
        if(str!=null){
            int open=0;
            int closed=0;

            int i=0;
            while((i=str.indexOf('(',i)) >=0 ){
                i++;
                open++;
            }
            i=0;
            while((i=str.indexOf(')',i)) >=0 ){
                i++;
                closed++;
            }
            result = open == closed;
        }
        return result;
    }

    public int indexOfParenthesisMatch(String str){
        int result = -1;

        return result;
    }
}
