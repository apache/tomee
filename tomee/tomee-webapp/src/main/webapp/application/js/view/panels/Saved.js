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

TOMEE.Saved = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var saved = TOMEE.components.Panel({
        isCollapsiblePanel: true,
        title:TOMEE.I18N.get('application.saved.objects'),
        extraStyles: {
            height:'200px'
        },
        bbar:[
            {
                tag:'a',
                cls:'btn',
                html:TOMEE.I18N.get('application.saved.objects.load'),
                listeners: {
                    'click': function() {
                        channel.send('application.saved.objects.load', {});
                    }
                }
            }
        ]
    });

    var table = TOMEE.components.Table({
        channel:channel
    });

    saved.getContentEl().append(table.getEl());

    return {
        getTitle:function () {
            return TOMEE.I18N.get('application.saved.objects');
        },
        getEl:function () {
            return saved.getEl();
        },
        load:function (data) {
            var arr = TOMEE.utils.toArray(data, function (key, obj) {
                return {
                    name:key,
                    value:obj
                };
            });

            table.load(arr, function (bean) {
                return [bean.name, bean.value];
            });
        }
    };
};