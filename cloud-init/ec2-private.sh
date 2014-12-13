#!/bin/bash

# Usage
#
# export DNS_HOST=foo
# export DNS_DOMAIN=example.com
# curl https://raw.githubusercontent.com/taimos/route53-updater/v1.5/cloud-init/ec2-private.sh | bash

# Route 53 Updater

yum install -y wget

wget -O /root/route53-updater.jar https://github.com/taimos/route53-updater/releases/download/v1.5/route53-updater.jar

java -jar /root/route53-updater.jar --private --domain $DNS_DOMAIN --host $DNS_HOST

PRIVATE_IP=`curl http://169.254.169.254/latest/meta-data/local-ipv4`

hostname $DNS_HOST.$DNS_DOMAIN
cat > /etc/hosts << HOSTS
127.0.0.1  localhost
$PRIVATE_IP $DNS_HOST.$DNS_DOMAIN $DNS_HOST
HOSTS
