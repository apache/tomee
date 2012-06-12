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

TOMEE.JndiClass = function (cfg) {
    "use strict";

    var channel = cfg.channel;

    var panel = TOMEE.components.Panel({
        title:TOMEE.I18N.get('application.jdni.class'),
        parent:cfg.parent,
        extraStyles:{
            width:'500px',
            height:'200px'
        }
    });

    return {
        show:function (params) {
            var el = panel.getContentEl();
            el.empty();

            var cls = params.data.cls;

            /*
            Data format
            {
                cls: {
                    type: 'String',
                        //CONTEXT, BEAN, OTHER
                        componentType: 'String',
                        //STATEFUL, STATELESS, SINGLETON, BMP_ENTITY, CMP_ENTITY, MESSAGE_DRIVEN, MANAGED
                        beanClass: 'impl class name',
                        interfaces: ['InterfaceA', 'InterfaceB'],
                        methods: [{
                        name: 'methodName',
                        returns: 'Class type',
                        parameters: ['ClassA', 'ClassB']
                    }, {
                        name: 'methodName',
                        returns: 'Class type',
                        parameters: ['ClassA', 'ClassB']
                    }]
                }
            }
            */
            var getFieldCfg = function (bean, getLabel, getValue) {
                var fieldId = TOMEE.Sequence.next('cls_property');
                var fieldConfig = {
                    tag:'div',
                    cls:'control-group',
                    children:[
                        {
                            tag:'label',
                            cls:'control-label',
                            attributes:{
                                'for':fieldId
                            },
                            html:getLabel(bean)
                        },
                        {
                            tag:'div',
                            cls:'controls',
                            children:[
                                {
                                    tag:'input',
                                    cls:'input input-xlarge',
                                    attributes:{
                                        style: 'margin-bottom: 5px;',
                                        'type':'text',
                                        'id':fieldId,
                                        'value':getValue(bean)
                                    }
                                }
                            ]

                        }
                    ]
                };
                return fieldConfig;
            };

            var fields = [];
            fields.push(getFieldCfg(cls, function (bean) {
                return 'type';
            }, function (bean) {
                return bean['type'];
            }));
            fields.push(getFieldCfg(cls, function (bean) {
                return 'componentType';
            }, function (bean) {
                return bean['componentType'];
            }));

            var elBottomBar = TOMEE.el.getElMap({
                elName:'main',
                tag:'form',
                cls:'well form-inline',
                attributes:{
                    style:'height: 27px;margin-bottom: 0px;padding-top: 1px;padding-left: 1px;padding-bottom: 1px;padding-right: 1px;'
                },
                children:[
                    {
                        tag:'div',
                        cls:'pull-right',
                        children:[
                            {
                                elName:'fileSelector',
                                tag:'select',
                                attributes:{
                                    style:'margin-right: 2px;'
                                }
                            },
                            {
                                elName:'loadBtn',
                                tag:'button',
                                cls:'btn',
                                html:TOMEE.I18N.get('application.log.load')
                            }
                        ]
                    }
                ]
            });

            var elements = TOMEE.el.getElMap({
                elName:'main',
                tag:'div',

                children:[
                    {
                        tag:'div',
                        attributes:{
                            style:'padding: 5px'
                        },
                        children:[
                            {
                                tag:'form',
                                cls:'form-horizontal',
                                children:[
                                    {
                                        tag:'fieldset',
                                        children:fields
                                    }
                                ]
                            }
                        ]
                    }
                ]
            });

            elements.main.append(elBottomBar.main);
            panel.getContentEl().append(elements.main);
            panel.showAt({
                modal:true
            });
        }
    };
};