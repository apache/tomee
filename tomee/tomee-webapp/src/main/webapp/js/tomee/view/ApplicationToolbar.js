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

TOMEE.ApplicationToolbar = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var elMapToolbar = TOMEE.el.getElMap({
        elName:'main',
        tag:'div',
        cls:'navbar navbar-fixed-top',
        children:[
            {
                tag:'div',
                cls:'navbar-inner',
                children:[
                    {
                        tag:'div',
                        children:[
                            {
                                elName: 'appName',
                                tag:'a',
                                cls:'brand',
                                attributes:{
                                    href:'#',
                                    style:'padding-left: 20px; margin-left: 0px;'
                                },
                                html:TOMEE.I18N.get('application.name')
                            },
                            {
                                tag:'div',
                                cls:'btn-group pull-right',
                                children:[
                                    {
                                        tag:'a',
                                        cls:'btn dropdown-toggle',
                                        attributes:{
                                            'data-toggle':'dropdown',
                                            href:'#'
                                        },
                                        children:[
                                            {
                                                tag:'i',
                                                cls:'icon-user'
                                            },
                                            {
                                                tag:'span',
                                                elName:'userNameSpan',
                                                attributes:{
                                                    style:'padding-left: 5px; padding-right: 5px;'
                                                }
                                            },
                                            {
                                                tag:'span',
                                                cls:'caret'
                                            }
                                        ]
                                    },
                                    {
                                        tag:'ul',
                                        cls:'dropdown-menu',
                                        attributes:{
                                            style:'right: 5px;'
                                        },
                                        children:[
                                            {
                                                tag:'li',
                                                children:[
                                                    {
                                                        elName:'signoutLink',
                                                        tag:'a',
                                                        attributes:{
                                                            href:'#'
                                                        },
                                                        html:TOMEE.I18N.get('application.logout')
                                                    }
                                                ]
                                            }
                                        ]

                                    }
                                ]
                            },
                            {
                                tag:'div',
                                cls:'nav-collapse',
                                children:[
                                    {
                                        elName: 'tabs',
                                        tag:'ul',
                                        cls:'nav',
                                        children:[
                                            {
                                                tag:'li',
                                                cls:'active',
                                                children:[
                                                    {
                                                        tag:'a',
                                                        attributes:{
                                                            href:'#'
                                                        },
                                                        html:TOMEE.I18N.get('application.home')
                                                    }
                                                ]
                                            },
                                            {
                                                tag:'li',
                                                children:[
                                                    {
                                                        tag:'a',
                                                        attributes:{
                                                            href:'#'
                                                        },
                                                        html:TOMEE.I18N.get('application.log')
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]

    });


    elMapToolbar.userNameSpan.text(TOMEE.I18N.get('application.guest'));

    elMapToolbar.appName.bind('click', function () {
        channel.send('application.name.click', {});
    });

    elMapToolbar.signoutLink.bind('click', function () {
        channel.send('application.logout', {});
    });

    elMapToolbar.tabs.delegate('a', 'click', function(event) {
        elMapToolbar.tabs.find('li').removeClass('active');
        var parent = $(event.currentTarget.parentElement);
        parent.addClass('active');
    });

    return {
        getEl: function() {
            return elMapToolbar.main;
        },
        setLoggedUser:function (name) {
            elMapToolbar.userNameSpan.text(name);
        }

    };
};