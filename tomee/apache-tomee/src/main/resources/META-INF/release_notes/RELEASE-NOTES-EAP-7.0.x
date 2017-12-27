= TomEE EAP 7.0.5-TT.1

== Downloads

* TomEE EAP 7.0.5-TT.1 Plume TAR.GZ -> https://s3.amazonaws.com/support-delivery/apache-tomee-plume-7.0.5-TT.1.tar.gz
* TomEE EAP 7.0.5-TT.1 Plume ZIP -> https://s3.amazonaws.com/support-delivery/apache-tomee-plume-7.0.5-TT.1.zip
* TomEE EAP 7.0.5-TT.1 Plus TAG.GZ -> https://s3.amazonaws.com/support-delivery/apache-tomee-plus-7.0.5-TT.1.tar.gz
* TomEE EAP 7.0.5-TT.1 Pluz ZIP -> https://s3.amazonaws.com/support-delivery/apache-tomee-plus-7.0.5-TT.1.zip
* TomEE EAP 7.0.5-TT.1 WebProfile TAG.GZ -> https://s3.amazonaws.com/support-delivery/apache-tomee-webprofile-7.0.5-TT.1.tar.gz
* TomEE EAP 7.0.5-TT.1 WebProfile ZIP -> https://s3.amazonaws.com/support-delivery/apache-tomee-webprofile-7.0.5-TT.1.zip

== Change log

=== Changes in TomEE EAP 7.0.5-TT.1

This release has the following changes:

* TOMEE-2145 fix double deploy issue when deploying an EAR from the webapps directory
* TOMEE-2146 system usage configuration for AMQ broker
* Update CXF to 3.1.14
* CVE-2017-12624 - Apache CXF web services that process attachments are vulnerable to Denial of Service (DoS) attacks. Message attachment headers that are greater than 300 characters will be rejected by default. This value is configurable via the property "attachment-max-header-size".
