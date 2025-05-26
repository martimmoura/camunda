/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.clients.transformers.aggregation.result;

import io.camunda.search.aggregation.result.BatchOperationAggregationResult;
import io.camunda.search.clients.core.AggregationResult;
import java.util.Collections;
import java.util.Map;

public class BatchOperationAggregationResultTransformer
    implements AggregationResultTransformer<BatchOperationAggregationResult> {

  @Override
  public BatchOperationAggregationResult apply(final Map<String, AggregationResult> aggregations) {

    return new BatchOperationAggregationResult(Collections.emptyList());
  }
}
