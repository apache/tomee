#!/usr/bin/perl -w

#use SOAP::Lite 'trace', 'debug';
use SOAP::Lite;

my $ID = $ARGV[0];

my $namespace = 'http://superbiz.org/wsdl';

my $service = SOAP::Lite-> uri($namespace) 
    ->proxy('http://localhost:8080/Movies') 
    ->on_action (sub { return '' } ); 

my $method = SOAP::Data->name("ns1:deleteMovieId") 
    ->attr({'xmlns:ns1' => $namespace}); 

my @params = (SOAP::Data->name('arg0'=> $ID));


my $response =  $service->call($method=>@params);

