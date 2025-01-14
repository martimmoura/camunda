#!/bin/bash

#
# Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
# one or more contributor license agreements. See the NOTICE file distributed
# with this work for additional information regarding copyright ownership.
# Licensed under the Camunda License 1.0. You may not use this file
# except in compliance with the Camunda License 1.0.
#

BACKUP_ID=$1

printf "#### Step 1 - Stop the exporter\n"

curl -X POST http://localhost:9600/actuator/exporting/pause

sleep 2 # wait for flushes to complete

printf "\n"
printf "\n"
printf "#### Step 2 - Backup the RDBMS database\n"
printf "Sanity check before backup:\n"
numProcessInstances=$(docker exec rdbms-postgres-1 psql -U camunda -d camunda -c "SELECT COUNT(*) FROM process_instance;" | sed '3q;d' | xargs)
printf "Number of process-instances: %s\n" "$numProcessInstances"
numFlowNodeInstances=$(docker exec rdbms-postgres-1 psql -U camunda -d camunda -c "SELECT COUNT(*) FROM flow_node_instance;" | sed '3q;d' | xargs)
printf "Number of flow-node-instances: %s\n" "$numFlowNodeInstances"

docker exec rdbms-postgres-1 pg_dump -U camunda camunda > backup_${BACKUP_ID}.dump

printf "\n"
printf "\n"
printf "#### Step 3 - Backup Zeebe data\n"

curl -X POST http://localhost:9600/actuator/backups -H "Content-Type: application/json" -d "{\"backupId\": $BACKUP_ID}"
printf "\n"

for i in {1..60};
do
  if [ $i -eq 60 ]; then
    printf "Backup did not complete in 60s\n"
    exit 1
  fi
  status=$(curl -s -X GET http://localhost:9600/actuator/backups/$BACKUP_ID | jq -r '.state')
  printf 'Backup %s status: %s\n' "$BACKUP_ID" "$status"

  if [ "$status" == "COMPLETED" ]; then
    break
  fi

  sleep 1
done

printf "\n"
printf "\n"
printf "#### Step 4 - Resume the broker\n"

curl -X POST http://localhost:9600/actuator/exporting/resume

printf "\n"
printf "\n"
printf "#### Backup finished\n"
