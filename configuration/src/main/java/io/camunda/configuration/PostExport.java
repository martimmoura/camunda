/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.configuration;

import io.camunda.configuration.UnifiedConfigurationHelper.BackwardsCompatibilityMode;
import java.util.Map;
import java.util.Set;

public class PostExport {

  private static final int DEFAULT_BATCH_SIZE = 100;
  private static final int DEFAULT_DELAY_BETWEEN_RUNS = 2000;
  private static final int DEFAULT_MAX_DELAY_BETWEEN_RUNS = 60000;
  private static final boolean DEFAULT_IGNORE_MISSING_DATA = false;

  private static final Map<String, String> LEGACY_BROKER_PROPERTIES =
      Map.of(
          "batch-size",
          "zeebe.broker.exporters.camundaexporter.args.postExport.batchSize",
          "delay-between-runs",
          "zeebe.broker.exporters.camundaexporter.args.postExport.delayBetweenRuns",
          "max-delay-between-runs",
          "zeebe.broker.exporters.camundaexporter.args.postExport.maxDelayBetweenRuns",
          "ignore-missing-data",
          "zeebe.broker.exporters.camundaexporter.args.postExport.ignoreMissingData");

  private final String prefix;

  private int batchSize = DEFAULT_BATCH_SIZE;
  private int delayBetweenRuns = DEFAULT_DELAY_BETWEEN_RUNS;
  private int maxDelayBetweenRuns = DEFAULT_MAX_DELAY_BETWEEN_RUNS;
  private boolean ignoreMissingData = DEFAULT_IGNORE_MISSING_DATA;

  public PostExport(final String databaseName) {
    prefix = "camunda.data.secondary-storage.%s.post-export".formatted(databaseName);
  }

  public int getBatchSize() {
    return UnifiedConfigurationHelper.validateLegacyConfiguration(
        prefix + ".batch-size",
        batchSize,
        Integer.class,
        BackwardsCompatibilityMode.SUPPORTED_ONLY_IF_VALUES_MATCH,
        Set.of(LEGACY_BROKER_PROPERTIES.get("batch-size")));
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public int getDelayBetweenRuns() {
    return UnifiedConfigurationHelper.validateLegacyConfiguration(
        prefix + ".delay-between-runs",
        delayBetweenRuns,
        Integer.class,
        BackwardsCompatibilityMode.SUPPORTED_ONLY_IF_VALUES_MATCH,
        Set.of(LEGACY_BROKER_PROPERTIES.get("delay-between-runs")));
  }

  public void setDelayBetweenRuns(final int delayBetweenRuns) {
    this.delayBetweenRuns = delayBetweenRuns;
  }

  public int getMaxDelayBetweenRuns() {
    return UnifiedConfigurationHelper.validateLegacyConfiguration(
        prefix + ".max-delay-between-runs",
        maxDelayBetweenRuns,
        Integer.class,
        BackwardsCompatibilityMode.SUPPORTED_ONLY_IF_VALUES_MATCH,
        Set.of(LEGACY_BROKER_PROPERTIES.get("max-delay-between-runs")));
  }

  public void setMaxDelayBetweenRuns(final int maxDelayBetweenRuns) {
    this.maxDelayBetweenRuns = maxDelayBetweenRuns;
  }

  public boolean isIgnoreMissingData() {
    return UnifiedConfigurationHelper.validateLegacyConfiguration(
        prefix + ".ignore-missing-data",
        ignoreMissingData,
        Boolean.class,
        BackwardsCompatibilityMode.SUPPORTED_ONLY_IF_VALUES_MATCH,
        Set.of(LEGACY_BROKER_PROPERTIES.get("ignore-missing-data")));
  }

  public void setIgnoreMissingData(final boolean ignoreMissingData) {
    this.ignoreMissingData = ignoreMissingData;
  }
}
