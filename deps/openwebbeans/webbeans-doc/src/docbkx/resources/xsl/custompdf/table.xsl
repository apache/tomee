<?xml version="1.0" encoding="UTF-8"?>
    <!--
        Licensed to the Apache Software Foundation (ASF) under one or more
        contributor license agreements. See the NOTICE file distributed with
        this work for additional information regarding copyright ownership. The
        ASF licenses this file to you under the Apache License, Version 2.0 (the
        "License"); you may not use this file except in compliance with the
        License. You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
        law or agreed to in writing, software distributed under the License is
        distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied. See the License for the specific
        language governing permissions and limitations under the License.
    -->
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format" 
    version="1.0">
    
    <xsl:template name="table.row.properties">

        <xsl:variable name="tabstyle">
            <xsl:call-template name="tabstyle" />
        </xsl:variable>

        <xsl:variable name="bgcolor">
            <xsl:call-template name="dbfo-attribute">
                <xsl:with-param name="pis" select="processing-instruction('dbfo')" />
                <xsl:with-param name="attribute" select="'bgcolor'" />
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="rownum">
            <xsl:number from="tgroup" count="row" />
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$bgcolor != ''">
                <xsl:attribute name="background-color">
                    <xsl:value-of select="$bgcolor" />
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$tabstyle = 'striped'">
                <xsl:if test="$rownum mod 2 = 0">
                    <xsl:attribute name="background-color">#EFEFEF</xsl:attribute>
                </xsl:if>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>