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

TOMEE.components.Tree = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var elements = (function() {
        var container = $('<div></div>');
        var ul = $('<ul></ul>');
        ul.addClass('tree');

        container.append(ul);

        return {
            container: container,
            ul: ul
        };
    })();


    var load = function (parent, data, getText, getChildren) {
        if($.isArray(data)) {
            for(var i = 0; i < data.length; i++) {
                load(parent, data[i], getText, getChildren);
            }

            return;
        }

        var children = getChildren(data);

        var li = $('<li></li>');
        li.append(getText(data));
        if(children) {
            li.addClass('closed');
        } else {
            li.addClass('leaf');
        }
        parent.append(li);
        var a = 0;
    };

    return {
        getEl:function () {
            return elements.container;
        },

        load:function(data, getText, getChildren) {
            var loadData = data;
            if(!$.isArray(loadData)) {
                loadData = [loadData];
            }

            elements.ul.empty();
            load(elements.ul, loadData, getText, getChildren);
        }
    };
};