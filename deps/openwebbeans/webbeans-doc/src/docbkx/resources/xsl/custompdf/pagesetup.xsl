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

    <xsl:template name="header.table">
        <xsl:param name="pageclass" select="''" />
        <xsl:param name="sequence" select="''" />
        <xsl:param name="gentext-key" select="''" />
        <!-- default is a single table style for all headers -->
        <!-- Customize it for different page classes or sequence location -->
        <xsl:choose>
            <xsl:when test="$pageclass = 'index'">
                <xsl:attribute name="margin-left">0pt</xsl:attribute>
            </xsl:when>
        </xsl:choose>
        <xsl:variable name="column1">
            <xsl:choose>
                <xsl:when test="$double.sided = 0">1</xsl:when>
                <xsl:when test="$sequence = 'first' or $sequence = 'odd'">1</xsl:when>
                <xsl:otherwise>3</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="column3">
            <xsl:choose>
                <xsl:when test="$double.sided = 0">3</xsl:when>
                <xsl:when test="$sequence = 'first' or $sequence = 'odd'">3</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="candidate">
            <fo:table table-layout="fixed" width="100%">
                <xsl:call-template name="head.sep.rule">
                    <xsl:with-param name="pageclass" select="$pageclass" />
                    <xsl:with-param name="sequence" select="$sequence" />
                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                </xsl:call-template>
                <fo:table-column column-number="1">
                    <xsl:attribute name="column-width">
                        <xsl:text>proportional-column-width(</xsl:text>
                            <xsl:call-template name="header.footer.width">
                                <xsl:with-param name="location">header</xsl:with-param>
                                <xsl:with-param name="position" select="$column1" />
                            </xsl:call-template>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </fo:table-column>
                <fo:table-column column-number="2">
                    <xsl:attribute name="column-width">
                        <xsl:text>proportional-column-width(</xsl:text>
                            <xsl:call-template name="header.footer.width">
                                <xsl:with-param name="location">header</xsl:with-param>
                                <xsl:with-param name="position" select="2" />
                            </xsl:call-template>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </fo:table-column>
                <fo:table-column column-number="3">
                    <xsl:attribute name="column-width">
                        <xsl:text>proportional-column-width(</xsl:text>
                            <xsl:call-template name="header.footer.width">
                                <xsl:with-param name="location">header</xsl:with-param>
                                <xsl:with-param name="position" select="$column3" />
                            </xsl:call-template>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </fo:table-column>
                <fo:table-body>
                    <!-- header-hight (org:14pt) -->
                    <fo:table-row height="50pt">
                        <fo:table-cell text-align="left"
                            display-align="before">
                            <xsl:if test="$fop.extensions = 0">
                                <xsl:attribute name="relative-align">baseline</xsl:attribute>
                            </xsl:if>
                            <fo:block>
                                <xsl:call-template name="header.content">
                                    <xsl:with-param name="pageclass" select="$pageclass" />
                                    <xsl:with-param name="sequence" select="$sequence" />
                                    <xsl:with-param name="position" select="'left'" />
                                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                                </xsl:call-template>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="center"
                            display-align="before">
                            <xsl:if test="$fop.extensions = 0">
                                <xsl:attribute name="relative-align">baseline</xsl:attribute>
                            </xsl:if>
                            <fo:block>
                                <xsl:call-template name="header.content">
                                    <xsl:with-param name="pageclass" select="$pageclass" />
                                    <xsl:with-param name="sequence" select="$sequence" />
                                    <xsl:with-param name="position" select="'center'" />
                                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                                </xsl:call-template>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right"
                            display-align="before">
                            <xsl:if test="$fop.extensions = 0">
                                <xsl:attribute name="relative-align">baseline</xsl:attribute>
                            </xsl:if>
                            <fo:block>
                                <xsl:call-template name="header.content">
                                    <xsl:with-param name="pageclass" select="$pageclass" />
                                    <xsl:with-param name="sequence" select="$sequence" />
                                    <xsl:with-param name="position" select="'right'" />
                                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                                </xsl:call-template>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </xsl:variable>
        <xsl:copy-of select="$candidate" />
    </xsl:template>

    <xsl:template name="header.content">
        <xsl:param name="pageclass" select="''" />
        <xsl:param name="sequence" select="''" />
        <xsl:param name="position" select="''" />
        <xsl:param name="gentext-key" select="''" />
        <fo:block xsl:use-attribute-sets="header.title.properties">
            <!-- sequence can be odd, even, first, blank -->
            <!-- position can be left, center, right -->
            <xsl:if test="$position='left'">
                <!-- 
                <xsl:apply-templates select="/" mode="title.markup" />
                <fo:block />
                <xsl:apply-templates select="/" mode="subtitle.markup" />
                <fo:block />
                -->
                <fo:block>
                    <fo:external-graphic src="url({$document.logo.src})"
                        width="4.5cm" height="auto" 
                        content-width="scale-to-fit" content-height="scale-to-fit" />
                </fo:block>
                <xsl:if
                    test="($pageclass !='titlepage' and $pageclass !='lot' and $sequence != 'blank')">
                    <xsl:apply-templates select="." mode="title.markup" />
                </xsl:if>
            </xsl:if>
            <xsl:if test="$position='right'">
                <fo:block>
                    <fo:external-graphic src="url({$apache.logo.src})"
                        width="4.5cm" height="auto" 
                        content-width="scale-to-fit" content-height="scale-to-fit" />
                </fo:block>
            </xsl:if>
        </fo:block>
    </xsl:template>

    <xsl:template name="footer.table">
        <xsl:param name="pageclass" select="''" />
        <xsl:param name="sequence" select="''" />
        <xsl:param name="gentext-key" select="''" />
        <!-- default is a single table style for all footers -->
        <!-- Customize it for different page classes or sequence location -->
        <xsl:choose>
            <xsl:when test="$pageclass = 'index'">
                <xsl:attribute name="margin-left">0pt</xsl:attribute>
            </xsl:when>
        </xsl:choose>
        <xsl:variable name="column1">
            <xsl:choose>
                <xsl:when test="$double.sided = 0">1</xsl:when>
                <xsl:when test="$sequence = 'first' or $sequence = 'odd'">1</xsl:when>
                <xsl:otherwise>3</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="column3">
            <xsl:choose>
                <xsl:when test="$double.sided = 0">3</xsl:when>
                <xsl:when test="$sequence = 'first' or $sequence = 'odd'">3</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="candidate">
            <fo:table table-layout="fixed" width="100%">
                <xsl:call-template name="foot.sep.rule">
                    <xsl:with-param name="pageclass" select="$pageclass" />
                    <xsl:with-param name="sequence" select="$sequence" />
                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                </xsl:call-template>
                <fo:table-column column-number="1">
                    <xsl:attribute name="column-width">
                        <xsl:text>proportional-column-width(</xsl:text>
                        <xsl:call-template name="header.footer.width">
                            <xsl:with-param name="location">footer</xsl:with-param>
                            <xsl:with-param name="position" select="$column1" />
                        </xsl:call-template>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </fo:table-column>
                <fo:table-column column-number="2">
                    <xsl:attribute name="column-width">
                        <xsl:text>proportional-column-width(</xsl:text>
                        <xsl:call-template name="header.footer.width">
                            <xsl:with-param name="location">footer</xsl:with-param>
                            <xsl:with-param name="position" select="2" />
                        </xsl:call-template>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </fo:table-column>
                <fo:table-column column-number="3">
                    <xsl:attribute name="column-width">
                        <xsl:text>proportional-column-width(</xsl:text>
                        <xsl:call-template name="header.footer.width">
                            <xsl:with-param name="location">footer</xsl:with-param>
                            <xsl:with-param name="position" select="$column3" />
                        </xsl:call-template>
                        <xsl:text>)</xsl:text>
                    </xsl:attribute>
                </fo:table-column>
                <fo:table-body>
                    <fo:table-row height="26pt">
                        <fo:table-cell text-align="left"
                            display-align="before">
                            <xsl:if test="$fop.extensions = 0">
                                <xsl:attribute name="relative-align">before</xsl:attribute>
                            </xsl:if>
                            <fo:block>
                                <xsl:call-template name="footer.content">
                                    <xsl:with-param name="pageclass" select="$pageclass" />
                                    <xsl:with-param name="sequence" select="$sequence" />
                                    <xsl:with-param name="position" select="'left'" />
                                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                                </xsl:call-template>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="center" display-align="before">
                            <xsl:if test="$fop.extensions = 0">
                                <xsl:attribute name="relative-align">before</xsl:attribute>
                            </xsl:if>
                            <fo:block>
                                <xsl:call-template name="footer.content">
                                    <xsl:with-param name="pageclass" select="$pageclass" />
                                    <xsl:with-param name="sequence" select="$sequence" />
                                    <xsl:with-param name="position" select="'center'" />
                                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                                </xsl:call-template>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right" display-align="before">
                            <xsl:if test="$fop.extensions = 0">
                                <xsl:attribute name="relative-align">before</xsl:attribute>
                            </xsl:if>
                            <fo:block>
                                <xsl:call-template name="footer.content">
                                    <xsl:with-param name="pageclass" select="$pageclass" />
                                    <xsl:with-param name="sequence" select="$sequence" />
                                    <xsl:with-param name="position" select="'right'" />
                                    <xsl:with-param name="gentext-key" select="$gentext-key" />
                                </xsl:call-template>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </xsl:variable>
        <xsl:copy-of select="$candidate" />
    </xsl:template>

    <xsl:template name="footer.content">
        <xsl:param name="pageclass" select="''" />
        <xsl:param name="sequence" select="''" />
        <xsl:param name="position" select="''" />
        <xsl:param name="gentext-key" select="''" />
        <xsl:variable name="RevInfo">
            <xsl:choose>
                <xsl:when test="//revhistory/revision[1]/revnumber">
                    <xsl:text>Version </xsl:text>
                    <xsl:value-of select="//revhistory/revision[1]/revnumber" />
                </xsl:when>
                <xsl:otherwise>
                    <!-- nop -->
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <fo:block xsl:use-attribute-sets="footer.title.properties">
            <!-- pageclass can be front, body, back -->
            <!-- sequence can be odd, even, first, blank -->
            <!-- position can be left, center, right -->
            <xsl:choose>
                <xsl:when test="$position = 'left'">
                    <xsl:value-of select="$document.copyright"/>
                </xsl:when>
                <xsl:when test="$position = 'center'">
                    <xsl:value-of select="$RevInfo" />
                </xsl:when>
                <xsl:when test="$position = 'right'">Page: <fo:page-number /></xsl:when>
                <xsl:otherwise />
            </xsl:choose>
        </fo:block>
    </xsl:template>
    <!-- ==================================================================== -->

    <xsl:template name="page.number.format">
        <xsl:param name="element" select="local-name(.)" />
        <xsl:param name="master-reference" select="1" />
    </xsl:template>

    <xsl:template name="initial.page.number">
        <xsl:param name="element" select="local-name(.)"/>
        <xsl:param name="master-reference" select="''"/>
        <xsl:choose>
          <!-- double-sided output -->
          <xsl:when test="$double.sided != 0">
            <xsl:choose>
              <xsl:when test="$element = 'toc'">auto-odd</xsl:when>
              <xsl:when test="$element = 'book'">1</xsl:when>
              <xsl:when test="$element = 'preface'">auto-odd</xsl:when>
              <xsl:when test="$element = 'part' and not(preceding::chapter) and not(preceding::part)">auto-odd</xsl:when>
              <xsl:when test="($element = 'dedication' or $element = 'article') and
                not(preceding::chapter or preceding::preface or
                preceding::appendix or preceding::article or
                preceding::dedication or parent::part or
                parent::reference)">auto-odd</xsl:when>
              <xsl:when test="($element = 'chapter' or $element = 'appendix') and
                not(preceding::chapter or preceding::appendix or
                preceding::article or preceding::dedication or
                parent::part or parent::reference)">auto-odd</xsl:when>
              <xsl:otherwise>auto-odd</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- single-sided output -->
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="$element = 'toc'">auto</xsl:when>
              <xsl:when test="$element = 'book'">1</xsl:when>
              <xsl:when test="$element = 'preface'">auto</xsl:when>
              <xsl:when test="$element = 'part' and not(preceding::chapter) and
                not(preceding::part)">auto</xsl:when>
              <xsl:when test="($element = 'dedication' or $element = 'article') and
                not(preceding::chapter or preceding::preface or
                preceding::appendix or preceding::article or
                preceding::dedication or parent::part or
                parent::reference)">auto</xsl:when>
              <xsl:when test="($element = 'chapter' or $element = 'appendix') and
                not(preceding::chapter or preceding::appendix or
                preceding::article or preceding::dedication or
                parent::part or parent::reference)">auto</xsl:when>
              <xsl:otherwise>auto</xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
  </xsl:template>

    <xsl:template name="force.page.count">
        <xsl:param name="element" select="local-name(.)" />
        <xsl:param name="master-reference" select="''" />
        <xsl:choose>
            <!-- double-sided output -->
            <xsl:when test="$double.sided != 0">end-on-even</xsl:when>
            <!-- single-sided output -->
            <xsl:otherwise>no-force</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="set.flow.properties">
        <xsl:param name="element" select="local-name(.)" />
        <xsl:param name="master-reference" select="''" />
        <!-- This template is called after each <fo:flow> starts. -->
        <!-- Customize this template to set attributes on fo:flow -->
        <xsl:choose>
            <xsl:when test="$fop.extensions != 0 or $passivetex.extensions != 0">
                <!-- body.start.indent does not work well with these processors -->
            </xsl:when>
            <xsl:when
                test="$master-reference = 'body' or $master-reference = 'lot' or
                        $master-reference = 'front' or $element = 'preface' or
                        ($master-reference = 'back' and $element = 'appendix')">
                <xsl:attribute name="start-indent">
          <xsl:value-of select="$body.start.indent" />
        </xsl:attribute>
                <xsl:attribute name="end-indent">
          <xsl:value-of select="$body.end.indent" />
        </xsl:attribute>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <!-- ==================================================================== -->
</xsl:stylesheet>
