#!/bin/sh -e

update-rc.d -f tomee-${classifier} remove
update-alternatives --remove tomee /etc/init.d/tomee-${classifier}