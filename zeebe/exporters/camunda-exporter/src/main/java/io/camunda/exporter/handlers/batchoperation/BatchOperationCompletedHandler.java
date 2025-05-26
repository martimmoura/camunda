/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.exporter.handlers.batchoperation;

import io.camunda.exporter.exceptions.PersistenceException;
import io.camunda.exporter.store.BatchRequest;
import io.camunda.webapps.schema.descriptors.template.BatchOperationTemplate;
import io.camunda.webapps.schema.entities.operation.BatchOperationEntity;
import io.camunda.webapps.schema.entities.operation.BatchOperationEntity.BatchOperationState;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.BatchOperationExecutionIntent;
import io.camunda.zeebe.protocol.record.value.BatchOperationExecutionRecordValue;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchOperationCompletedHandler
    extends AbstractBatchOperationHandler<BatchOperationExecutionRecordValue> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(BatchOperationCompletedHandler.class);

  public BatchOperationCompletedHandler(final String indexName) {
    super(indexName);
  }

  @Override
  public ValueType getHandledValueType() {
    return ValueType.BATCH_OPERATION_EXECUTION;
  }

  @Override
  public boolean handlesRecord(final Record<BatchOperationExecutionRecordValue> record) {
    return record.getIntent().equals(BatchOperationExecutionIntent.COMPLETED);
  }

  @Override
  public void updateEntity(
      final Record<BatchOperationExecutionRecordValue> record, final BatchOperationEntity entity) {
    entity.setState(BatchOperationState.COMPLETED);
  }

  @Override
  public void flush(final BatchOperationEntity entity, final BatchRequest batchRequest)
      throws PersistenceException {
    final Map<String, Object> updateFields = new HashMap<>();
    updateFields.put(BatchOperationTemplate.STATE, entity.getState());
    batchRequest.update(indexName, entity.getId(), updateFields);

    LOGGER.trace("Updated batch operation {} with fields {}", entity.getId(), updateFields);
  }
}
