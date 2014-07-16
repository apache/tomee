#!/usr/bin/env perl -w
#============================================================
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#============================================================

use SOAP::Lite;

my $namespace = 'http://superbiz.org/wsdl';

my $service = SOAP::Lite-> uri($namespace) 
    ->proxy('http://localhost:8080/Calculator') 
    ->on_action (sub { return '' } ); 

my $method = SOAP::Data->name("ns1:multiply") 
    ->attr({'xmlns:ns1' => $namespace}); 

my @params = (
    SOAP::Data->name('arg0'=>3), 
    SOAP::Data->name('arg1'=>4)); 

print $service->call($method=>@params)->result; 
