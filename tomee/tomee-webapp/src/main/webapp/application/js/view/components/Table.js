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

TOMEE.components.Table = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var elements = (function () {
        var container = $('<div></div>');

        var table = $('<table></table>');
        table.addClass('table');
        table.addClass('table-striped');
        table.addClass('table-bordered');
        table.addClass('table-condensed');

        var headerLine = (function () {
            var tr = $('<tr></tr>');
            var thead = $('<thead></thead>');

            thead.append(tr);
            table.append(thead);

            return tr;
        })();

        var createTd = function(colData) {
            var td = $('<td></td>');
            td.append(colData);
            return td;
        };
        var myColumns = TOMEE.utils.getArray(cfg.columns);
        for(var i = 0; i < myColumns.length; i++) {
            headerLine.append(createTd(myColumns[i]));
        }

        var body = $('<tbody></tbody>');
        table.append(body);

        container.append(table);

        return {
            container:container,
            headerLine:headerLine,
            body:body
        };
    })();


    var createRow = function (rowData) {
        var row = $('<tr></tr>');

        var column = null;
        for (var i = 0; i < rowData.length; i++) {
            column = $('<td></td>');
            column.append(rowData[i]);

            row.append(column);
        }

        return row;
    };


    var load = function (data, getRowData) {
        elements.body.empty();

        var loadData = TOMEE.utils.getArray(data);

        for (var i = 0; i < loadData.length; i++) {
            elements.body.append(
                createRow(
                    TOMEE.utils.getArray(getRowData(loadData[i]))
                )
            );
        }
    };

    return {
        getEl:function () {
            return elements.container;
        },

        load:load
    };
};