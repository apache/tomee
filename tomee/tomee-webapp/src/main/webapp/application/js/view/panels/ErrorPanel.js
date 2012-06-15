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

TOMEE.ErrorPanel = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var showParams = null;

    var panel = TOMEE.components.Panel({
        title:'-',
        parent:cfg.parent,
        extraStyles:{
            width:'700px',
            height:'300px'
        },
        bbar:[
            {
                tag:'a',
                cls:'btn',
                html:TOMEE.I18N.get('application.error.close'),
                listeners:{
                    'click':function () {
                        panel.close(true);
                    }
                }
            }
        ]
    });

    return {
        show:function (params) {
            var content = panel.getContentEl();
            content.empty();

            var error = jQuery.parseJSON(params.jqXHR.responseText);
            panel.setTitle(error.exception_type);

            var pre = $('<pre></pre>');
            pre.append(error.stackTrace);
            pre.css('border', '0px');
            pre.css('background-color', 'white');
            pre.css('margin', '0px');
            content.append(pre);

            panel.showAt({
                modal:true
            });
        }
    };
};