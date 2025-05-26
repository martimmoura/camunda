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
import io.camunda.zeebe.protocol.record.intent.BatchOperationIntent;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.protocol.record.value.BatchOperationLifecycleManagementRecordValue;
import io.camunda.zeebe.util.DateUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchOperationLifecycleManagementHandler
    extends AbstractBatchOperationHandler<BatchOperationLifecycleManagementRecordValue> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(BatchOperationLifecycleManagementHandler.class);

  private static final Set<Intent> EXPORTABLE_INTENTS =
      Set.of(
          BatchOperationIntent.CANCELED, BatchOperationIntent.PAUSED, BatchOperationIntent.RESUMED);

  public BatchOperationLifecycleManagementHandler(final String indexName) {
    super(indexName);
  }

  @Override
  public ValueType getHandledValueType() {
    return ValueType.BATCH_OPERATION_LIFECYCLE_MANAGEMENT;
  }

  @Override
  public boolean handlesRecord(final Record<BatchOperationLifecycleManagementRecordValue> record) {
    return EXPORTABLE_INTENTS.contains(record.getIntent());
  }

  @Override
  public void updateEntity(
      final Record<BatchOperationLifecycleManagementRecordValue> record,
      final BatchOperationEntity entity) {
    if (record.getIntent().equals(BatchOperationIntent.CANCELED)) {
      entity
          .setEndDate(DateUtil.toOffsetDateTime(record.getTimestamp()))
          .setState(BatchOperationState.CANCELED);
    } else if (record.getIntent().equals(BatchOperationIntent.PAUSED)) {
      entity.setEndDate(null).setState(BatchOperationState.PAUSED);
    } else if (record.getIntent().equals(BatchOperationIntent.RESUMED)) {
      entity.setEndDate(null).setState(BatchOperationState.ACTIVE);
    }
  }

  @Override
  public void flush(final BatchOperationEntity entity, final BatchRequest batchRequest)
      throws PersistenceException {
    final Map<String, Object> updateFields = new HashMap<>();
    updateFields.put(BatchOperationTemplate.STATE, entity.getState());
    updateFields.put(BatchOperationTemplate.END_DATE, entity.getEndDate());
    batchRequest.update(indexName, entity.getId(), updateFields);

    LOGGER.trace("Updated batch operation {} with fields {}", entity.getId(), updateFields);
  }

  @Override
  public Class<BatchOperationEntity> getEntityType() {
    return BatchOperationEntity.class;
  }
}
