/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.exporter.handlers.batchoperation;

import io.camunda.exporter.handlers.ExportHandler;
import io.camunda.webapps.schema.entities.operation.BatchOperationEntity;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.value.BatchOperationRelated;
import java.util.List;

abstract class AbstractBatchOperationHandler<R extends BatchOperationRelated>
    implements ExportHandler<BatchOperationEntity, R> {

  protected static final String ID_PATTERN = "%s_%s";
  protected final String indexName;

  public AbstractBatchOperationHandler(final String indexName) {
    this.indexName = indexName;
  }

  @Override
  public Class<BatchOperationEntity> getEntityType() {
    return BatchOperationEntity.class;
  }

  @Override
  public List<String> generateIds(final Record<R> record) {
    return List.of(
        String.format(
            ID_PATTERN, record.getValue().getBatchOperationKey(), record.getPartitionId()));
  }

  @Override
  public BatchOperationEntity createNewEntity(final String id) {
    return new BatchOperationEntity().setId(id);
  }

  @Override
  public String getIndexName() {
    return indexName;
  }
}
