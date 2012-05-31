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
            var jndi_tree_div_id = TOMEE.Sequence.next();

            $('body').append(TOMEE.utils.stringFormat(data, {
                'app_name':TOMEE.I18N.get('application.name'),
                'footer':TOMEE.I18N.get('application.footer'),
                'jndi_tree_div_id': jndi_tree_div_id
            }));

            var tree = TOMEE.components.Tree({
                channel: channel
            });

            var treeEl = tree.getEl();
            $('#' + jndi_tree_div_id).append(treeEl);

            tree.load([
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() },
                { text: TOMEE.Sequence.next(), children: [] },
                { text: TOMEE.Sequence.next() }
            ], function(data) {
                return data.text;
            }, function(data) {
                return data.children;
            });

            var a = 0;



        });

    return {

    };
};