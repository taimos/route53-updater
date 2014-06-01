# Route53 updater

This tool updates Route53 RecordSets based on local information.

## Usage
Just issue ``java -jar route53-updater --domain <val> --host <val>`` on the console and it will create or update a CNAME for ``host.domain.`` which aliases to the public DNS name of the current instance. This is retrieved via instance metadata. Credentials to update Route53 are obtained using IAM profiles.

For further options run ``java -jar route53-updater`` without arguments. 

## Installation

On Linux bases systems you can call

``wget https://github.com/taimos/route53-updater/releases/download/v1.3/route53-updater.jar``

