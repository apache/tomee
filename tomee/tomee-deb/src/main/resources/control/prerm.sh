#!/bin/sh -e

service tomee-${classifier} stop || true

rm /usr/share/tomee-${classifier}-${tomeeVersion}/conf
rm -Rf /var/lib/tomee-${classifier}-${tomeeVersion}/*
rm -Rf /var/log/tomee-${classifier}-${tomeeVersion}/*
