#!/bin/bash

#
# Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
# one or more contributor license agreements. See the NOTICE file distributed
# with this work for additional information regarding copyright ownership.
# Licensed under the Camunda License 1.0. You may not use this file
# except in compliance with the Camunda License 1.0.
#

BACKUP_ID=$1

printf "#### Step 1 - Check prerequisites\n"

# check for running empty database
if [ $(docker ps | grep rdbms-postgres-1 | wc -l | xargs) -eq 0 ]; then
  printf "No running database found. Please start the database first.\n"
  exit 1
fi

if [ $(docker exec rdbms-postgres-1 psql -U camunda -d camunda -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'databasechangelog'" | sed '3q;d' | xargs)  -ne 0 ]; then
  printf "Database schema not empty.\n"
  exit 1
fi

printf "#### Step 2 - Restoring Backup %s\n" $BACKUP_ID

docker cp ./backup_${BACKUP_ID}.dump rdbms-postgres-1:/tmp/backup.dump
docker exec rdbms-postgres-1 psql -U camunda -d camunda -f /tmp/backup.dump

printf "\n"
printf "\n"
printf "#### Step 3 - Sanity check after restore\n"
numProcessInstances=$(docker exec rdbms-postgres-1 psql -U camunda -d camunda -c "SELECT COUNT(*) FROM process_instance;" | sed '3q;d' | xargs)
printf "Number of process-instances: %s\n" "$numProcessInstances"
numFlowNodeInstances=$(docker exec rdbms-postgres-1 psql -U camunda -d camunda -c "SELECT COUNT(*) FROM flow_node_instance;" | sed '3q;d' | xargs)
printf "Number of flow-node-instances: %s\n" "$numFlowNodeInstances"

printf "#### Step 3 - Execute Zeebe restore\n"
printf "Execute the following in your camunda project in your IDE\n"
printf "io.camunda.zeebe.restore.RestoreApp --backupId=%s\n" $BACKUP_ID
