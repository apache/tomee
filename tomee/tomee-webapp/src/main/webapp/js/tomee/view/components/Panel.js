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

TOMEE.components.Panel = function (cfg) {
    "use strict";

    var channel = cfg.channel;
    var avoidOverflow = TOMEE.utils.getSafe(cfg.avoidOverflow, false);

    var map = TOMEE.el.getElMap({
        elName: 'main',
        tag: 'div',
        children: [{
            tag: 'div',
            children:[{
                tag: 'div',
                cls: 'well t-panel',
                children:[{
                    tag: 'h3',
                    attributes:{
                        style: 'position: relative; background-color: #d3d3d3; padding-left: 5px'
                    },
                    html: TOMEE.utils.getSafe(cfg.title, '-')
                }, {
                    elName: 'content',
                    tag: 'div',
                    attributes:{
                        style: 'height: 250px; position: relative; overflow: auto;'
                    },
                    createCallback: function(el) {
                        if(avoidOverflow) {
                            el.css('overflow', '');
                        }
                    }
                }]
            }]
        }]
    });


    var extraStyles = cfg.extraStyles;
    if(extraStyles) {
        (function() {
            var content = map['content'];

            for(var key in extraStyles) {
                content.css(key, extraStyles[key]);
            }
        })();
    }



    return {
        getEl:function () {
            return map.main;
        },
        getContentEl: function() {
            return map.content;
        }
    };
};