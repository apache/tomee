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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xlink='http://www.w3.org/1999/xlink'
                exclude-result-prefixes="xlink"
                version='1.0'>

    <xsl:template name="inline.boldermonoseq">
        <xsl:param name="content">
            <xsl:apply-templates />
        </xsl:param>
        <fo:inline font-style="normal" font-weight="bolder"
            xsl:use-attribute-sets="monospace.properties">
            <xsl:if test="@dir">
                <xsl:attribute name="direction">
    				<xsl:choose>
    					<xsl:when test="@dir = 'ltr' or @dir = 'lro'">ltr</xsl:when>
    					<xsl:otherwise>rtl</xsl:otherwise>
    				</xsl:choose>
    			</xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="$content" />
        </fo:inline>
    </xsl:template>

    <xsl:template match="database">
        <xsl:call-template name="inline.monoseq" />
    </xsl:template>

    <xsl:template match="replaceable">
        <xsl:call-template name="inline.boldermonoseq" />
    </xsl:template>


</xsl:stylesheet>

