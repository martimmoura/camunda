#!/bin/bash
set -eux

curlOptions="-fL -m10"

function _resume {
  curl ${curlOptions} -XPOST 'http://np-test-backups-zeebe-gateway:9600/actuator/exporting/resume'
  echo "Resumed Zeebe export"
}

# ensure we resume exporting no matter what on exit
trap _resume EXIT

backupId="$(date '+%Y%m%d%H%M%S')"
echo "Taking Camunda backup [${backupId}]..."

echo "Taking an Operate backup..."
curl ${curlOptions} -XPOST -H'Content-Type: application/json' -H'Accept: application/json' \
  -d "{ \"backupId\": ${backupId} }" \
  'http://np-test-backups-operate:9600/actuator/backups'
until [ "$(curl ${curlOptions} "http://np-test-backups-operate:9600/actuator/backups/${backupId}" | jq -r '.state')" = "COMPLETED" ]; do
    printf '.'
    sleep 1
done
echo "Operate backup completed!"

echo "Taking a Tasklist backup..."
curl ${curlOptions} -XPOST -H'Content-Type: application/json' -H'Accept: application/json' \
  -d "{ \"backupId\": ${backupId} }" \
  'http://np-test-backups-tasklist:9600/actuator/backups'
until [ "$(curl ${curlOptions} "http://np-test-backups-tasklist:9600/actuator/backups/${backupId}" | jq -r '.state')" = "COMPLETED" ]; do
    printf '.'
    sleep 1
done
echo "Tasklist backup completed!"

echo "Soft-pausing Zeebe..."
curl -XPOST 'http://np-test-backups-zeebe-gateway:9600/actuator/exporting/pause?soft=true'

echo "Taking a Zeebe ES backup..."
curl ${curlOptions} -XPUT -H'Content-Type: application/json' -H'Accept: application/json' \
  -d '{ "indices": "zeebe-record*", "feature_states": ["none"] }' \
  "http://np-test-backups-elasticsearch:9200/_snapshot/zeebe/camunda_zeebe_records_backup_${backupId}"
until [ "$(curl ${curlOptions} "http://np-test-backups-elasticsearch:9200/_snapshot/zeebe/camunda_zeebe_records_backup_${backupId}/_status" | jq -r '.snapshots[0].state')" = "SUCCESS" ]; do
    printf '.'
    sleep 1
done
echo "Zeebe ES backup completed!"

echo "Taking a Zeebe backup..."
curl ${curlOptions} -XPOST -H'Content-Type: application/json' -H'Accept: application/json' \
  -d "{ \"backupId\": ${backupId} }" \
  'http://np-test-backups-zeebe-gateway:9600/actuator/backups'
until [ "$(curl ${curlOptions} "http://np-test-backups-zeebe-gateway:9600/actuator/backups/${backupId}" | jq -r '.state')" = "COMPLETED" ]; do
    printf '.'
    sleep 1
done
echo "Zeebe backup completed!"

curl ${curlOptions} -XPOST 'http://np-test-backups-zeebe-gateway:9600/actuator/exporting/resume'
echo "Resumed Zeebe export"

echo "Camunda 8 backup [${backupId}] completed!"
