# Route53 updater

This tool updates Route53 RecordSets based on local information.

## Usage
Just issue ``route53-updater <domain> <host>`` on the console and it will create or update a CNAME for ``host.domain.`` which aliases to the public DNS name of the current instance. This is retrieved via instance metadata. Credentials to update Route53 are obtained using IAM profiles.

## Installation

On systems using yum you can call

``rpm -Uvh https://github.com/taimos/route53-updater/releases/download/v1.0.0/route53-updater-1.0.0-1.noarch.rpm``

