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

(function () {
    'use strict';

    var deps = ['app/js/templates', 'app/js/view/application-table-row', 'app/js/view/application-table-paginator',
        'lib/underscore', 'app/js/i18n', 'lib/backbone'];
    define(deps, function (templates, TableRowView, paginator, underscore, i18n) {

        var View = Backbone.View.extend({
            tagName: 'div',
            className: 'ux-application',

            loadDataLink: $(templates.getValue('load-data-link', {})),

            filterOption: 'title',

            events: {
                'click .ux-filter': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    var selected = $(me.$el.find('.ux-selected-filter').get(0));
                    var myLink = $(evt.target);
                    me.filterOption = myLink.attr('href');
                    selected.html(i18n.get(me.filterOption, {}));
                },
                'click .ux-filter-action': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    var filterValue = $(me.$el.find('.ux-filter-field').get(0)).val();
                    filterValue = $.trim(filterValue);
                    if (filterValue !== '') {
                        me.trigger('filter', {
                            filterType: me.filterOption,
                            filterValue: filterValue
                        });
                    }
                },
                'click .ux-clear-filter-action': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('clear-filter', {});
                },
                'click .ux-application': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('navigate', {
                        path: 'application'
                    });
                },
                'click .ux-add-btn': function (evt) {
                    evt.preventDefault();
                    var me = this;
                    me.trigger('add', {});
                }
            },

            setFilter: function (fieldName, fieldValue) {
                var field = fieldName;
                var value = fieldValue;
                if (!fieldName || $.trim(fieldName) === '') {
                    field = 'title';
                    value = '';
                }
                var me = this;
                me.filterOption = field;
                $(me.$el.find('.ux-selected-filter').get(0)).html(i18n.get(me.filterOption, {}));
                $(me.$el.find('.ux-filter-field').get(0)).val(value);
            },

            render: function () {
                var me = this;
                if (!this.options.isRendered) {
                    me.$el.empty();
                    me.$el.append(templates.getValue('application', {}));
                    me.loadDataLink.on('click', function (evt) {
                        evt.preventDefault();
                        me.trigger('load-sample', {});
                    });
                    me.options.isRendered = true;
                }
                return this;
            },

            addRows: function (rows) {
                var me = this;
                var tbody = $(me.$el.find('tbody').get(0));
                tbody.empty();
                paginator.$el.detach();
                me.loadDataLink.detach();
                underscore.each(rows, function (model) {
                    var row = new TableRowView({
                        model: model
                    });
                    row.on('delete', function (data) {
                        me.trigger('delete', data);
                    });
                    row.on('edit', function (data) {
                        me.trigger('edit', data);
                    });
                    tbody.append(row.render().$el);
                });
            },

            setPaginator: function (count) {
                var me = this;
                paginator.$el.detach();
                me.loadDataLink.detach();
                var table = $(me.$el.find('.table').get(0));
                if (count) {
                    table.after(paginator.$el);
                } else {
                    table.after(me.loadDataLink);
                }
            }
        });
        return new View();
    });
}());
