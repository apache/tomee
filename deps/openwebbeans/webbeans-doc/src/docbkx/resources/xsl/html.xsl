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
    version="1.0">

    <!-- import the docbook-stylesheet without chunking -->
    <xsl:import href="urn:docbkx:stylesheet" />

    <!-- ================ Customization Layer =========== -->
    <!--
        customhtml/component.xsl must be imported not included since the
        template would not get the right priority otherwise.
    -->
    <xsl:import href="customhtml/component.xsl" />
    <xsl:import href="customhtml/sections.xsl" />
    
    <xsl:include href="customhtml/admon.xsl" />
    <xsl:include href="customhtml/autoidx.xsl" />
    <xsl:include href="customhtml/inline.xsl" />
    <xsl:include href="customhtml/titlepage.xsl" />
    <xsl:include href="customhtml/highlight.xsl" />

    <!-- ================ Header and Footer data ============== -->
    <xsl:param name="apache.logo.src" select="concat($img.src.path, 'asf-logo.gif')" />
    <xsl:param name="apache.logo.alt" select="'Apache Software Foundation'" />
    <xsl:param name="document.logo.src" select="concat($img.src.path, 'openwebbeans.png')" />
    <xsl:param name="document.logo.alt" select="'Apache OpenWebBeans'" />
    <xsl:param name="document.copyright" select="'&#169; 2009 The Apache OpenWebBeans development community'" />

    <!-- =============== Customized I18n ============== -->
    <xsl:param name="local.l10n.xml" select="document('customcommon/en.xml')" />

    <!-- ================ Fileextension, rootname and path =========== -->
    <xsl:param name="html.ext" select="'.html'" />
    <xsl:param name="root.filename" select="'index'" />
    <xsl:param name="use.id.as.filename" select="'0'" />

    <!-- ================ Chunking-Level ============================= -->
    <xsl:param name="chunk.section.depth" select="2" />
    <!--
        chunk.first.sections: if non-zero, first-sections will appear on their
        own page
    -->
    <xsl:param name="chunk.first.sections" select="1" />

    <!-- ================ CSS Stylesheet ============================= -->
    <xsl:param name="css.decoration" select="1" />

    <!-- ================ Enumeration of Sections ==================== -->
    <xsl:param name="section.autolabel" select="1" />
    <xsl:param name="section.label.includes.component.label" select="1" />

    <!-- ================ ToC ======================================== -->
    <xsl:param name="toc.max.depth">
        1
    </xsl:param>
    <xsl:param name="toc.section.depth">
        3
    </xsl:param>
    <xsl:param name="generate.section.toc.level" select="1" />

    <!-- ================ Glossary =================================== -->
    <xsl:param name="glossentry.show.acronym" select="'primary'" />
    <xsl:param name="glossterm.auto.link" select="1" />

    <!-- ================ Index =================================== -->
    <xsl:param name="generate.index" select="1" />
    <xsl:param name="index.on.type" select="0" />
    <xsl:param name="index.on.role" select="0" />
    <xsl:param name="index.prefer.titleabbrev" select="1" />

    <!-- ================ Images ================================= -->
    <xsl:param name="draft.mode" select="'no'" />
    <!-- ignore scaling-options for html-output -->
    <xsl:param name="ignore.image.scaling" select="1" />
    <xsl:param name="graphic.default.extension" select="'.png'" />
    <xsl:param name="draft.watermark.image" select="concat($img.src.path, 'admons/draft.png')" />

    <!-- ================ Admontation ================================ -->
    <xsl:param name="admon.graphics" select="1" />
    <xsl:param name="admon.graphics.extension" select="'.png'" />
    <xsl:param name="admon.graphics.path" select="concat($img.src.path, 'admons/')" />
    <!--
        1 = Admontation are presented with a generated text label such as Note
        or Warning
    -->
    <xsl:param name="admon.textlabel" select="1" />
    <xsl:param name="admon.style">
        <xsl:text><!-- no default margins; use css instead --></xsl:text>
    </xsl:param>

    <!-- ================ Callout =================================== -->
    <xsl:param name="callout.graphics" select="'1'" />
    <xsl:param name="callouts.extension" select="'1'" />
    <xsl:param name="callout.graphics.extension" select="'.png'" />
    <xsl:param name="callout.graphics.path" select="concat($img.src.path, 'callouts/')" />

    <!-- ================ Refentry =================================== -->
    <xsl:param name="refentry.generate.name" select="0" />
    <xsl:param name="refentry.generate.title" select="1" />
    <xsl:param name="annotate.toc" select="1" />

    <!-- ================ Navigation ================================= -->
    <xsl:param name="navig.graphics" select="1" />
    <xsl:param name="navig.graphics.path" select="concat($img.src.path, 'admons/')" />
    <xsl:param name="navig.graphics.extension" select="'.gif'" />
    <xsl:param name="navig.showtitles" select="1" />
    <xsl:param name="header.rule" select="0" />
    <xsl:param name="footer.rule" select="0" />

    <!-- ================ Tabledesign ================================ -->
    <xsl:param name="default.table.width" select="'700'" />
    <xsl:param name="html.cellspacing" select="'0'" />
    <xsl:param name="html.cellpadding" select="'1'" />

    <!-- Code highlighting -->
    <xsl:param name="highlight.source" select="1" />

</xsl:stylesheet>
