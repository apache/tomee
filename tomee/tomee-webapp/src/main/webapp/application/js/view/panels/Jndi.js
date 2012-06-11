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

TOMEE.Jndi = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var jndiMenu = TOMEE.components.Menu({
        commands:[
            {
                text:TOMEE.I18N.get('application.jdni.lookup'),
                callback:function (data) {
                    var panel = TOMEE.components.Panel({
                        title:TOMEE.I18N.get('application.jdni.class'),
                        extraStyles:{
                            width:'500px',
                            height:'200px'
                        }
                    });
                    panel.showAt({
                        modal:true
                    });
                }
            }
        ]
    });


    var jndi = TOMEE.components.Panel({
        title:TOMEE.I18N.get('application.jdni')
    });

    var tree = TOMEE.components.Tree({
        key:'jndi',
        channel:channel,
        getText:function (data) {
            return data.name;
        },
        childrenPropertyName:'children'
    });

    var treeEl = tree.getEl();
    jndi.getContentEl().append(treeEl);

    return {
        loadJndi:function (data) {
            tree.load(data.parentEl, data.names);
        },
        getEl:function () {
            return jndi.getEl();
        },
        jndiContextMenu:function (opts) {
            jndiMenu.showAt({
                left:opts.left,
                top:opts.top,
                data:opts.data
            });
        }
    };
};