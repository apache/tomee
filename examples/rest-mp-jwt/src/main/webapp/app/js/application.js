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

    var deps = [
        'app/js/view/container',
        'app/js/view/application',
        'app/js/view/application-table-paginator',
        'app/js/view/movie',
        'lib/underscore',
        'app/js/model/movies',
        'app/js/model/movie',
        'app/js/i18n',
        'lib/less', 'lib/backbone', 'lib/jquery', 'lib/bootstrap'
    ];
    define(deps, function (containerView, applicationView, paginator, MovieView, underscore, moviesList, MovieModel) {
        var max = 5;
        var appState = {
            page: null,
            fieldName: null,
            fieldValue: null
        };
        containerView.render();

        $.ajaxSetup({ cache: false });

        function loadPage(pageNumber, fieldName, fieldValue) {
            var data = {
                max: max,
                first: ((pageNumber - 1) * max)
            };
            if (fieldName && fieldValue) {
                data.field = fieldName;
                data.searchTerm = fieldValue;
            }
            applicationView.setFilter(fieldName, fieldValue);
            moviesList.fetch({
                data: data,
                success: function (result) {
                    applicationView.addRows(result.models);

                    $.ajax({
                        url: window.ux.ROOT_URL + 'rest/movies/count/',
                        method: 'GET',
                        dataType: 'json',
                        data: {
                            field: appState.fieldName,
                            searchTerm: appState.fieldValue
                        },
                        success: function (total) {
                            var count = Math.ceil(total / max);
                            paginator.setCount(count);
                            applicationView.setPaginator(count);
                        }
                    });
                }
            });
        }

        function start() {
            //Starting the backbone router.
            var Router = Backbone.Router.extend({
                routes: {
                    '': 'showApplication',
                    'application': 'showApplication',
                    'application/:page': 'showApplication',
                    'application/:page/:field/:value': 'showApplication'
                },

                showApplication: function (page, fieldName, fieldValue) {
                    var me = this;
                    appState.page = page;
                    appState.fieldName = fieldName;
                    appState.fieldValue = fieldValue;
                    containerView.showView(applicationView);
                    if (!page || !underscore.isNumber(Number(page))) {
                        me.showApplication(1);
                    } else {
                        loadPage(Number(page), fieldName, fieldValue);
                        if (fieldName) {
                            me.navigate('application/' + page + '/' + fieldName + '/' + fieldValue, {
                                trigger: false
                            });
                        } else {
                            me.navigate('application/' + page, {
                                trigger: false
                            });
                        }
                    }
                }
            });
            var router = new Router();

            applicationView.on('load-sample', function () {
                $.ajax({
                    url: window.ux.ROOT_URL + 'rest/load/',
                    method: 'POST',
                    dataType: 'json',
                    data: {},
                    success: function (data) {
                        router.showApplication();
                    }
                });
            });

            applicationView.on('delete', function (data) {
                data.model.destroy({
                    success: function () {
                        router.showApplication(appState.page, appState.fieldName, appState.fieldValue);
                    }
                });
            });

            function showMovieWindow(model) {
                var view = new MovieView({
                    model: model
                });
                view.render();
                view.on('save-model', function (data) {
                    data.model.save({}, {
                        success: function () {
                            view.remove();
                            loadPage(appState.page, appState.fieldName, appState.fieldValue);
                        }
                    });
                });
                $('body').append(view.$el);
                view.$el.modal({});
            }

            applicationView.on('add', function () {
                showMovieWindow(new MovieModel({}));
            });

            applicationView.on('edit', function (data) {
                showMovieWindow(data.model);
            });

            applicationView.on('filter', function (data) {
                router.navigate('application/1/' + data.filterType + '/' + data.filterValue, {
                    trigger: true
                });
            });

            applicationView.on('clear-filter', function (data) {
                router.navigate('application/1', {
                    trigger: true
                });
            });

            paginator.on('go-to-page', function (data) {
                var page = data.number;
                if (page === 'last') {
                    page = paginator.getCount();
                }
                router.showApplication(page, appState.fieldName, appState.fieldValue);
            });

            //Starting the backbone history.
            Backbone.history.start({
                pushState: true,
                root: window.ux.ROOT_URL // This value is set by <c:url>
            });

            return {
                getRouter: function () {
                    return router;
                }
            };
        }

        return {
            start: start
        };
    });
}());