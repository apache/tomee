<% out.print("<!DOCTYPE html>"); %>
<!--

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

-->

<!-- $Rev: 597221 $ $Date: 2007-11-21 22:51:05 +0100 (Wed, 21 Nov 2007) $ -->

<html>
<head>
    <meta charset="utf-8">
    <title>TomEE</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="default.css" rel="stylesheet">
    <link href="lib/bootstrap/2.0.4/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }

        .sidebar-nav {
            padding: 9px 0;
        }
    </style>
    <link href="lib/bootstrap/2.0.4/css/bootstrap-responsive.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</head>

<body>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <a class="brand" href="http://openejb.apache.org">TomEE</a>

            <div class="nav-collapse">
                <ul class="nav">
                    <li><a href="index.jsp">Index</a></li>
                    <li><a href="viewjndi.jsp">JNDI</a></li>
                    <li><a href="viewejb.jsp">EJB</a></li>
                    <li><a href="viewclass.jsp">Class</a></li>
                    <li><a href="invokeobj.jsp">Invoke</a></li>
                    <li class="active"><a href="viewconsole.jsp.jsp">Console</a></li>
                </ul>

            </div>
            <!--/.nav-collapse -->
        </div>
    </div>
</div>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <p>
                <textarea id='scriptCodeTXT' class="span8 input-xlarge" id="textarea" rows="25">
                </textarea><BR>
                <a class="btn btn-primary" href="#" onclick="TOMEE.executeScript();">Run</a>
            </p>

        </div>
    </div>

    <div id="resultsDIV" class="row-fluid"></div>

    <hr>

    <footer>
        <p>Copyright &copy; 2012 The Apache Software Foundation, Licensed under the Apache License, Version 2.0. Apache
            and the Apache feather logo are trademarks of The Apache Software Foundation.</p>
    </footer>
</div>
<!-- /container -->


<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="lib/jquery/jquery-1.7.2.min.js"></script>
<script src="lib/bootstrap/2.0.4/js/bootstrap.js"></script>

<script type="text/javascript">
    var TOMEE = (function () {

        var executeScript = function () {

            var request = $.ajax({
                type: 'POST',
                dataType: 'text',
                data: {
                    'scriptCode': $('#scriptCodeTXT').val()
                },
                url: '/tomee/ws/console',
                success: function (data) {
                    var value = data;
                    if(!value || value === '') {
                        value = 'done'
                    }
                    var el = $('<div class="well"><p>' + data + '</p></div>');
                    $('#resultsDIV').prepend(el);
                },
                error: function (data) {
                    var bodyHtml = /<body.*?>([\s\S]*)<\/body>/.exec(data.responseText)[1];
                    var el = $('<div class="well"><p>' + bodyHtml + '</p></div>');
                    $('#resultsDIV').prepend(el);
                }
            });
        };

        return {
            executeScript: executeScript
        }
    })();
</script>

</body>
</html>

