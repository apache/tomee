#!/bin/sh -e

update-alternatives --remove tomee /etc/init.d/tomee-${classifier}
update-rc.d -f tomee-${classifier} remove
