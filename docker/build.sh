#!/usr/bin/env bash

VERSION=${1:?missing version}

wget -O route53-updater.jar https://github.com/taimos/route53-updater/releases/download/v${VERSION}/route53-updater.jar

docker build -t taimos/route53-updater:${VERSION} .

docker tag taimos/route53-updater:${VERSION} taimos/route53-updater:latest

rm -f route53-updater.jar