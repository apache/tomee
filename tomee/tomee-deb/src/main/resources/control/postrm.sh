#!/bin/sh

update-rc.d -f tomee-${classifier} remove
update-alternatives --remove tomee /etc/init.d/tomee-${classifier}