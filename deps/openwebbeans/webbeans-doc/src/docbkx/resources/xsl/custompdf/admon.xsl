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
	version='1.0'>

	<!-- === Parameter === -->
	<!-- 1 = Admonition are presented with a generated text label such as Note or Warning -->
	<xsl:param name="admon.textlabel" select="1"/>
	<xsl:param name="admon.graphics" select="1"></xsl:param>
	
	<!-- Admonition Title -->
	<xsl:attribute-set name="admonition.title.properties">
		<xsl:attribute name="font-size">12pt</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="hyphenate">false</xsl:attribute>
		<xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
	</xsl:attribute-set>
	<!-- Admonition Block -->
	<xsl:attribute-set name="graphical.admonition.properties">
		<xsl:attribute name="border-style">solid</xsl:attribute>
		<xsl:attribute name="border-right-width">thin</xsl:attribute>
		<xsl:attribute name="border-left-width">thin</xsl:attribute>
		<xsl:attribute name="border-top-width">thin</xsl:attribute>
		<xsl:attribute name="border-bottom-width">thin</xsl:attribute>
		<xsl:attribute name="border-right-color">black</xsl:attribute>
		<xsl:attribute name="border-left-color">black</xsl:attribute>
		<xsl:attribute name="border-top-color">black</xsl:attribute>
		<xsl:attribute name="border-bottom-color">black</xsl:attribute>
		<xsl:attribute name="margin-left">0.25in</xsl:attribute>
		<xsl:attribute name="margin-right">0.25in</xsl:attribute>
		<xsl:attribute name="space-before.optimum">1em</xsl:attribute>
		<xsl:attribute name="space-before.minimum">0.8em</xsl:attribute>
		<xsl:attribute name="space-before.maximum">1.2em</xsl:attribute>
		<xsl:attribute name="space-after.optimum">1em</xsl:attribute>
		<xsl:attribute name="space-after.minimum">0.8em</xsl:attribute>
		<xsl:attribute name="space-after.maximum">1.2em</xsl:attribute>
	</xsl:attribute-set>

	<!-- === Template === -->
	<xsl:template match="*" mode="admon.graphic.width">
		<xsl:param name="node" select="."/>
		<xsl:text>40pt</xsl:text>
	</xsl:template>
		
	<xsl:template name="graphical.admonition">
		<xsl:variable name="id">
			<xsl:call-template name="object.id"/>
		</xsl:variable>
		<xsl:variable name="graphic.width">
			<xsl:apply-templates select="." mode="admon.graphic.width"/>
		</xsl:variable>
		<fo:block id="{$id}"
			xsl:use-attribute-sets="graphical.admonition.properties">
			<fo:list-block provisional-distance-between-starts="{$graphic.width} + 18pt"
				provisional-label-separation="18pt">
				<fo:list-item margin-top="4pt" margin-bottom="4pt" margin-left="4pt" margin-right="4pt">
					<fo:list-item-label end-indent="label-end()">
						<fo:block>
							<fo:external-graphic width="auto" height="auto"
								content-width="{$graphic.width}" >
								<xsl:attribute name="src">
									<xsl:call-template name="admon.graphic"/>
								</xsl:attribute>
							</fo:external-graphic>
						</fo:block>
					</fo:list-item-label>
					<fo:list-item-body start-indent="body-start()">
						<xsl:if test="$admon.textlabel != 0 or title">
							<fo:block xsl:use-attribute-sets="admonition.title.properties">
								<xsl:apply-templates select="." mode="object.title.markup"/>
							</fo:block>
						</xsl:if>
						<fo:block xsl:use-attribute-sets="admonition.properties">
							<xsl:apply-templates/>
						</fo:block>
					</fo:list-item-body>
				</fo:list-item>
			</fo:list-block>
		</fo:block>
	</xsl:template>
		
</xsl:stylesheet>
