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
<!DOCTYPE xsl:stylesheet [
  <!ENTITY comment.block.parents "parent::answer|parent::appendix|parent::article|parent::bibliodiv|
                                  parent::bibliography|parent::blockquote|parent::caution|parent::chapter|
                                  parent::glossary|parent::glossdiv|parent::important|parent::index|
                                  parent::indexdiv|parent::listitem|parent::note|parent::orderedlist|
                                  parent::partintro|parent::preface|parent::procedure|parent::qandadiv|
                                  parent::qandaset|parent::question|parent::refentry|parent::refnamediv|
                                  parent::refsect1|parent::refsect2|parent::refsect3|parent::refsection|
                                  parent::refsynopsisdiv|parent::sect1|parent::sect2|parent::sect3|parent::sect4|
                                  parent::sect5|parent::section|parent::setindex|parent::sidebar|
                                  parent::simplesect|parent::taskprerequisites|parent::taskrelated|
                                  parent::tasksummary|parent::warning">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink='http://www.w3.org/1999/xlink'
                xmlns:suwl="http://nwalsh.com/xslt/ext/com.nwalsh.saxon.UnwrapLinks"
                exclude-result-prefixes="xlink suwl"
                version='1.0'>

    <xsl:template match="database">
    	<xsl:call-template name="inline.monoseq"/>
    </xsl:template>
    
    <xsl:template match="replaceable" priority="1">
    	<xsl:call-template name="inline.monoseq"/>
    </xsl:template>

</xsl:stylesheet>

