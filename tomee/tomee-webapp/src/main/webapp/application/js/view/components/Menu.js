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

TOMEE.components.Menu = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var myBody = $('body');
    myBody.bind('click', function() {
        map.main.detach();
        data = {};
    });

    var data = {};

    var map = TOMEE.el.getElMap({
        elName:'main',
        tag:'ul',
        cls:'dropdown-menu',
        attributes:{
            style:'display: block; position:absolute; left:100px; top:100px; z-index:5000;'
        }
    });

    (function (commands, ul) {
        var command = null;
        for (var i = 0; i < commands.length; i++) {
            command = commands[i];

            ul.append(TOMEE.el.getElMap({
                elName:'main',
                tag:'li',
                children:[
                    {
                        tag:'a',
                        attributes:{
                            href:'#'
                        },
                        html:command.text
                    }
                ],
                listeners:{
                    'click':function () {
                        command.callback(data);
                    }
                }
            }).main);
        }
    })(cfg.commands, map.main);




    return {
        showAt:function (config) {
            var main = map.main;

            main.css('left', TOMEE.el.getLocationValue(config.left));
            main.css('top', TOMEE.el.getLocationValue(config.top));

            data = config.data;

            myBody.append(main);
        }
    };
};