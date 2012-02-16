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

TOMEE.ApplicationHomePanelJndi = function (cfg) {
    "use strict";

    var channel = cfg.channel;
    var model = cfg.model;

    var elements = (function () {
        var tbodyUid = TOMEE.Sequence.next();
        var tpl = [
            '<div class="row">',
            '<legend>' + TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi.title') + '</legend>',

            '<table class="table table-striped table-bordered table-condensed">',
            '    <thead>',
            '        <tr>',
            '            <th>' + TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi.module') + '</th>',
            '            <th>' + TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi.path') + '</th>',
            '            <th>' + TOMEE.ApplicationI18N.get('app.home.menu.tools.jndi.resource') + '</th>',
            '        </tr>',
            '    </thead>',
            '    <tbody id="' + tbodyUid + '"/>',
            '</table>',

            '</div>'
        ];

        //create the element
        var all = $(tpl.join(''));
        var tbody = all.find("#" + tbodyUid);
        return {
            all: all,
            tbody: tbody
        };
    })();

    /**
     *
     * @param bean
     */
    var addRow = function (bean) {
        var row = [
            '        <tr>',
            '            <td>' + bean.module + '</td>',
            '            <td>' + bean.name + '</td>',
            '            <td>' + bean.value + '</td>',
            '        </tr>'
        ].join('');
        elements.tbody.append($(row));
    };

    var loadData = function () {
        //remove the current rows if any
        elements.tbody.empty();

        //The user should give a "getData" method that iterates over
        //the objects that will be used to populate the grid
        model.iterateJndiBeans(function (bean) {
            addRow(bean);
        });
    };


    return {
        getEl: function () {
            return elements.all;
        },
        loadData: loadData,
        getMyModel: function () {
            return model;
        }
    };
};