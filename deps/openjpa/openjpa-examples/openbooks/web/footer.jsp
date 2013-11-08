<%-- 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
--%>
<!-- ========================================================================= -->
<!-- This footer file is included in every page of OpenBooks web application   -->
<!-- The footer closes the tags opened in header.jsp                           -->
<!-- ========================================================================= -->
<%@page import="org.apache.openjpa.conf.OpenJPAVersion"%>
<div id="footer">
   Running on <img src="images/openjpa-logo-small.png" 
                   width="100px" height="40px" border="0"> 
   version <%= OpenJPAVersion.MAJOR_RELEASE + "." +  OpenJPAVersion.MINOR_RELEASE %>
         <div style="float:right;text-align: right;margin-right:1em">
             <img alt="" src="images/java_link.png" border="0"> links to Java Source Code
         </div>
</div>
</BODY>
</HTML>
