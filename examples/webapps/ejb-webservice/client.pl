#!/usr/bin/perl -w

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
