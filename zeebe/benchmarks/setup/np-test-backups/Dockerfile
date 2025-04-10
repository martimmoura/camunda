FROM alpine:latest
RUN apk add --update-cache \
    curl \
    bash \
    jq \
  && rm -rf /var/cache/apk/*
COPY ./backup.sh /backup.sh

ENTRYPOINT ["/bin/bash", "/backup.sh" ]
