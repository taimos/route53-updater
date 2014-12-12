#!/bin/bash

# Usage
#
# export DNS_HOST=foo
# export DNS_DOMAIN=example.com
# curl https://raw.githubusercontent.com/taimos/route53-updater/v1.4/cloud-init/ec2-public.sh | bash

# Route 53 Updater

yum install -y wget

wget -O /root/route53-updater.jar https://github.com/taimos/route53-updater/releases/download/v1.4/route53-updater.jar

java -jar /root/route53-updater.jar --domain $DNS_DOMAIN --host $DNS_HOST
