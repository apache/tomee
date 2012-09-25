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
    xmlns:exsl="http://exslt.org/common" 
    version="1.0" 
    exclude-result-prefixes="exsl">

    <xsl:template name="user.header.content">
        <xsl:param name="node" select="." />
        <xsl:call-template name="add.header.logo"/>
    </xsl:template>
        
    <xsl:template name="add.header.logo">
        <div class="navheader">
            <table>
                <tr>
                    <td width="20%" align="left"><img src="{$document.logo.src}" border="0" alt="{$document.logo.alt}"/></td>
                    <td width="60%">&#160;</td>
                    <td width="20%" align="right"><img src="{$apache.logo.src}" border="0" alt="{$apache.logo.alt}"/></td>
                </tr>
            </table>
        </div>
    </xsl:template>

    <xsl:template name="book.titlepage.recto">
        <xsl:choose>
            <xsl:when test="bookinfo/title">
                <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/title" />
            </xsl:when>
            <xsl:when test="info/title">
                <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/title" />
            </xsl:when>
            <xsl:when test="title">
                <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="title" />
            </xsl:when>
        </xsl:choose>
    
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/productname" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/productname" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/revhistory" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/revhistory" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/author" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/author" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/abstract" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/abstract" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/legalnotice" />
        <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/legalnotice" />
    </xsl:template>

    <xsl:template name="book.titlepage">
      <div class="titlepage">
        <xsl:variable name="recto.content">
          <xsl:call-template name="book.titlepage.before.recto"/>
          <xsl:call-template name="book.titlepage.recto"/>
        </xsl:variable>
        <xsl:variable name="recto.elements.count">
          <xsl:choose>
            <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
            <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
              <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
          <div><xsl:copy-of select="$recto.content"/></div>
        </xsl:if>
        <xsl:variable name="verso.content">
          <xsl:call-template name="book.titlepage.before.verso"/>
          <xsl:call-template name="book.titlepage.verso"/>
        </xsl:variable>
        <xsl:variable name="verso.elements.count">
          <xsl:choose>
            <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
            <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
              <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
          <div><xsl:copy-of select="$verso.content"/></div>
        </xsl:if>
        <xsl:call-template name="book.titlepage.separator"/>
      </div>
    </xsl:template>

    <xsl:template match="*" mode="book.titlepage.recto.mode">
        <!-- if an element isn't found in this mode, -->
        <!-- try the generic titlepage.mode -->
        <xsl:apply-templates select="." mode="titlepage.mode" />
    </xsl:template>
    
    <xsl:template match="*" mode="book.titlepage.verso.mode">
        <!-- if an element isn't found in this mode, -->
        <!-- try the generic titlepage.mode -->
        <xsl:apply-templates select="." mode="titlepage.mode" />
    </xsl:template>
    
    <xsl:template match="title" mode="book.titlepage.recto.auto.mode">
        <div xsl:use-attribute-sets="book.titlepage.recto.style">
            <xsl:call-template name="title.mainpage">
            </xsl:call-template>
        </div>
    </xsl:template>
    
    <xsl:template match="productname" mode="book.titlepage.recto.auto.mode">
        <div xsl:use-attribute-sets="book.titlepage.recto.style">
            <xsl:call-template name="productname.mainpage">
            </xsl:call-template>
        </div>
    </xsl:template>
    
    <xsl:template match="revhistory" mode="book.titlepage.recto.auto.mode">
        <div xsl:use-attribute-sets="book.titlepage.recto.style">
            <xsl:call-template name="revhistory.mainpage">
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template match="author" mode="book.titlepage.recto.auto.mode">
        <div xsl:use-attribute-sets="book.titlepage.recto.style">
            <xsl:call-template name="author.mainpage">
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template match="abstract" mode="book.titlepage.recto.auto.mode">
        <div xsl:use-attribute-sets="book.titlepage.recto.style">
            <xsl:apply-templates select="."
                mode="book.titlepage.recto.mode" />
        </div>
    </xsl:template>

    <xsl:template match="legalnotice" mode="book.titlepage.recto.auto.mode">
        <div xsl:use-attribute-sets="book.titlepage.recto.style">
            <xsl:call-template name="legalnotice.mainpage">
            </xsl:call-template>
        </div>
    </xsl:template>

    <xsl:template name="title.mainpage">
        <xsl:variable name="id">
            <xsl:choose>
                <xsl:when test="contains(local-name(..), 'info')">
                <xsl:call-template name="object.id">
                    <xsl:with-param name="object" select="../.."/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="object.id">
                    <xsl:with-param name="object" select=".."/>
                </xsl:call-template>
            </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <h1 class="subtitle">
            <a name="{$id}"/>
			<xsl:choose>
                <xsl:when test="$show.revisionflag != 0 and @revisionflag">
                    <span class="{@revisionflag}">
                    <xsl:apply-templates mode="titlepage.mode"/>
                    </span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="titlepage.mode"/>
                </xsl:otherwise>
            </xsl:choose>
    		<xsl:if test="//bookinfo//subtitle">
                <xsl:call-template name="gentext.space"/>
                :
                <xsl:call-template name="gentext.space"/>
                <xsl:apply-templates select="//bookinfo//subtitle" mode="mainpage"/>
            </xsl:if>
		</h1>
    </xsl:template>

    <xsl:template match="subtitle" mode="mainpage">
        <xsl:apply-templates mode="titlepage.mode"/>
    </xsl:template>

    <xsl:template name="revhistory.mainpage">
        <xsl:variable name="id">
            <xsl:call-template name="object.id"/>
        </xsl:variable>
        <xsl:variable name="title">
            <xsl:call-template name="gentext">
                <xsl:with-param name="key">RevHistory</xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <div class="{name(.)}">
			<b>
				<xsl:call-template name="gentext">
                    <xsl:with-param name="key" select="'RevHistory'"/>
                </xsl:call-template>
			</b>
			<table border="1" cellpadding="1" cellspacing="0" width="800" summary="Revision history">
				<tr>
					<th align="left" valign="top">
						<p>Version</p>
					</th>
					<th align="left" valign="top">
						<p>Date</p>
					</th>
					<th align="left" valign="top">
						<p>Author</p>
					</th>
					<th align="left" valign="top">
						<p>Reason of modification</p>
					</th>
				</tr>
				
				<xsl:apply-templates mode="titlepage.mode"/>
			</table>
		</div>
    </xsl:template>

    <xsl:template match="revhistory/revision" mode="titlepage.mode">
        <xsl:variable name="revnumber" select="revnumber" />
        <xsl:variable name="revdate" select="date" />
        <xsl:variable name="revauthor" select="authorinitials" />
        <xsl:variable name="revremark" select="revremark|revdescription" />
        <tr>
            <td align="left">
                <p>
                    <xsl:choose>
                        <xsl:when test="$revnumber">
                            <xsl:apply-templates select="$revnumber[1]" mode="titlepage.mode" />
                        </xsl:when>
                        <xsl:otherwise>
                             
                        </xsl:otherwise>
                    </xsl:choose>
                </p>
            </td>
            <td align="left">
                <p>
                    <xsl:choose>
                        <xsl:when test="$revdate[1]">
                            <xsl:apply-templates select="$revdate[1]" mode="titlepage.mode" />
                        </xsl:when>
                        <xsl:otherwise>
                             
                        </xsl:otherwise>
                    </xsl:choose>
                </p>
            </td>
            <td align="left">
                <p>
                    <xsl:choose>
                        <xsl:when test="$revauthor">
                            <xsl:for-each select="$revauthor">
                                <xsl:apply-templates select="." mode="titlepage.mode" />
                                <xsl:if test="position() != last()">
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                             
                        </xsl:otherwise>
                    </xsl:choose>
                </p>
            </td>
            <td align="left">
                <p>
                    <xsl:choose>
                        <xsl:when test="$revremark">
                            <xsl:apply-templates select="$revremark[1]" mode="titlepage.mode" />
                        </xsl:when>
                        <xsl:otherwise>
                             
                        </xsl:otherwise>
                    </xsl:choose>
                </p>
            </td>
        </tr>
    </xsl:template>
    
    <xsl:template name="productname.mainpage">
        <p class="{name(.)}">
            This document is part of the
            <xsl:apply-templates mode="titlepage.mode" />
            <xsl:call-template name="gentext.space" />
            <xsl:if test="//bookinfo//productnumber">
                <xsl:apply-templates select="//bookinfo//productnumber" />
            </xsl:if>
            <xsl:call-template name="gentext.space" />
            release.
        </p>
        <br />
    </xsl:template>
    
    <xsl:template name="author.mainpage">
        <div class="{name(.)}">
            <b class="{name(.)}">Contact person:</b>
            <xsl:call-template name="gentext.space" />
            <xsl:call-template name="person.name" />
            <xsl:call-template name="gentext.space" />
            <xsl:apply-templates mode="titlepage.mode"
                select="./email" />
        </div>
    </xsl:template>
    
    <xsl:template name="legalnotice.mainpage">
        <xsl:variable name="id">
            <xsl:call-template name="object.id" />
        </xsl:variable>
        <hr />
        <div class="copyright">
            <a name="{$id}" />
            <xsl:apply-templates mode="titlepage.mode" />
        </div>
    </xsl:template>

</xsl:stylesheet>