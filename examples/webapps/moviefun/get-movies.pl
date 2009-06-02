#!/usr/bin/perl -w

#use SOAP::Lite 'trace', 'debug';
use SOAP::Lite;

my $namespace = 'http://superbiz.org/wsdl';

my $service = SOAP::Lite-> uri($namespace) 
    ->proxy('http://localhost:8080/Movies') 
    ->on_action (sub { return '' } ); 

my $method = SOAP::Data->name("ns1:getMovies") 
    ->attr({'xmlns:ns1' => $namespace}); 

#my @params = (
#    SOAP::Data->name('arg0'=>3), 
#    SOAP::Data->name('arg1'=>4)); 

my $response =  $service->call($method=>@params);

for my $movie ($response->valueof('//return')) {
    while ( my ($key, $value) = each(%$movie) ) {
	print "$key => $value\n";
    }
    print "\n";
}





