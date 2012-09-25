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
    xmlns:xslthl="http://xslthl.sf.net" 
    exclude-result-prefixes="xslthl" 
    version="1.0">

    <xsl:template match="a/@*|img/@*|xslthl:number/@*|a|img|xslthl:number">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match='xslthl:keyword'>
        <span class="hl-keyword">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:string'>
        <span class="hl-string">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:comment'>
        <span class="hl-comment">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:tag'>
        <span class="hl-tag">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:attribute'>
        <span class="hl-attribute">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:value'>
        <span class="hl-value">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:html'>
        <span class="hl-html">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:xslt'>
        <span class="hl-xslt">
            <xsl:apply-templates />
        </span>
    </xsl:template>

    <xsl:template match='xslthl:section'>
        <span class="hl-section">
            <xsl:apply-templates />
        </span>
    </xsl:template>

</xsl:stylesheet>
