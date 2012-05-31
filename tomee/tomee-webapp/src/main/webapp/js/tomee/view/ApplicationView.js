/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

TOMEE.ApplicationView = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    $.ajax({
        url:'js/tomee/view/body.html',
        dataType:'text'
    }).success(function (data, textStatus, jqXHR) {
            var navItemUlId = TOMEE.Sequence.next();
            var navBarId = TOMEE.Sequence.next();
            var footerDivId = TOMEE.Sequence.next();
            var bodyDivId = TOMEE.Sequence.next();

            var html = TOMEE.utils.stringFormat(data, {
                'app_name':TOMEE.I18N.get('application.name'),
                'footer':TOMEE.I18N.get('application.footer'),
                'nav_items_ul_id':navItemUlId,
                'navbar navbar-fixed-top id':navBarId,
                'footer_id':footerDivId,
                'body div id': bodyDivId
            });

            //add html to the body
            $('body').append(html);

//TODO: implement me!
//            var bodyDivHeight = $(window).height() - 100;
//
//
//            $('#' + bodyDivId).height(bodyDivHeight);
//
//            var navItemUl = $('#' + navItemUlId);

        });

    return {

    };
};