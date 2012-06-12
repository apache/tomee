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

TOMEE.JndiClass = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var panel = TOMEE.components.Panel({
        title:TOMEE.I18N.get('application.jdni.class'),
        parent:cfg.parent,
        extraStyles:{
            width:'500px',
            height:'200px'
        }
    });

    var buildList = function(parent, obj) {
        var li = $('<li></li>');
        for(var prop in obj) {
            (function(key, value) {
                if(TOMEE.utils.isPrimitive(value)) {
                    li.append(key + ': ' + value);

                } else if(value instanceof Array) {
                    li.append(key);
                    var ul = $('<ul></ul>');

                    for(var i = 0; i < value.length; i++) {
                        buildList(ul, value[i]);
                    }

                    li.append(ul);
                } else {

                    li.append(key);
                    var ul = $('<ul></ul>');

                    for(var inner in value) {
                        buildList(ul, value[inner]);
                    }

                    li.append(ul);
                }
            })(prop, obj[prop]);
        }
        parent.append(li);
    };

    return {
        show:function (params) {
            var el = panel.getContentEl();
            el.empty();

            var innerDiv = $('<ul></ul>');
            var cls = params.data.cls;
            buildList(innerDiv, cls);

            panel.getContentEl().append(innerDiv);

            panel.showAt({
                modal:true
            });
        }
    };
};