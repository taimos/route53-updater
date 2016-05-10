FROM java:8-alpine
MAINTAINER Taimos GmbH

COPY entrypoint.sh /opt/entrypoint.sh
RUN chmod +x /opt/entrypoint.sh

COPY route53-updater.jar /opt/route53-updater.jar

ENTRYPOINT ["/opt/entrypoint.sh"]
